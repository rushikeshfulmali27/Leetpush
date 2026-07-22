/**
 * LeetHub AI — Background Service Worker (Manifest V3)
 * 
 * Responsibilities:
 *  1. Receive SYNC_SOLUTION messages from the content script
 *  2. Retrieve the JWT from chrome.storage.local
 *  3. POST the solution payload to the LeetHub AI backend
 *  4. Store recent sync results in storage for the popup
 *  5. Handle offline queue and retry logic
 */

// Load API base URL from environment or config
const API_BASE = (() => {
  // Use localhost in development, leetpush in production
  const isDev = !('update_url' in chrome.runtime.getManifest());
  return isDev ? 'http://localhost:8080/api/v1' : 'https://api.leetpush.com/api/v1';
})();

// ─── Message handler ──────────────────────────────────────────────────────────
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'SYNC_SOLUTION') {
    syncSolution(message.payload)
      .then(result => sendResponse({ success: true, result }))
      .catch(err  => {
        console.error('[LeetHub AI BG] Sync failed:', err);
        sendResponse({ success: false, error: err.message });
      });
    return true; // keep channel open for async response
  }

  if (message.type === 'GET_STATUS') {
    getStoredStatus().then(status => sendResponse(status));
    return true;
  }

  if (message.type === 'SAVE_TOKEN') {
    chrome.storage.local.set({ jwt: message.token }, () => {
      sendResponse({ success: true });
    });
    return true;
  }

  if (message.type === 'PROCESS_OFFLINE_QUEUE') {
    processOfflineQueue()
      .then(result => sendResponse({ success: true, result }))
      .catch(err => sendResponse({ success: false, error: err.message }));
    return true;
  }
});

// ─── Core sync logic with retry ───────────────────────────────────────────────
async function syncSolution(payload) {
  const jwt = await getToken();
  if (!jwt) {
    throw new Error('Not authenticated. Please log in from the extension popup.');
  }

  let response;
  try {
    response = await fetchWithRetry(`${API_BASE}/sync/submit`, {
      method:  'POST',
      headers: {
        'Content-Type':  'application/json',
        'Authorization': `Bearer ${jwt}`,
      },
      body: JSON.stringify(payload),
    });
  } catch (err) {
    // Distinguish auth errors from actual network errors
    if (err.message === 'Unauthorized') {
      // Clear the stale token so GET_STATUS shows unauthenticated
      chrome.storage.local.remove('jwt');
      throw new Error('Authentication expired. Please visit LeetHub dashboard to refresh your token.');
    }
    // Queue for offline sync
    console.warn('[LeetHub AI BG] Network error, queueing submission:', err);
    await queueSubmissionOffline(payload);
    throw new Error('Network error. Solution queued for sync when connection is restored.');
  }

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    if (response.status === 401) {
      throw new Error('Authentication failed. Please log in again.');
    }
    throw new Error(body.message ?? `HTTP ${response.status}`);
  }

  const data = await response.json();

  // Store the result for the popup badge
  await appendSyncHistory({
    title:      payload.title,
    difficulty: payload.difficulty,
    language:   payload.language,
    syncId:     data.data?.syncId,
    status:     'PENDING',
    syncedAt:   new Date().toISOString(),
  });

  return data;
}

// ─── Retry logic with exponential backoff ─────────────────────────────────────
async function fetchWithRetry(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await Promise.race([
        fetch(url, options),
        new Promise((_, reject) =>
          setTimeout(() => reject(new Error('Request timeout')), 10000)
        )
      ]);
      if (response.ok) return response;
      if (response.status === 401 || response.status === 403) {
        throw new Error('Unauthorized');
      }
      if (i < maxRetries - 1) {
        const delay = Math.pow(2, i) * 1000; // exponential backoff
        await new Promise(r => setTimeout(r, delay));
      }
    } catch (err) {
      if (i === maxRetries - 1) throw err;
      const delay = Math.pow(2, i) * 1000;
      await new Promise(r => setTimeout(r, delay));
    }
  }
}

// ─── Offline queue support ────────────────────────────────────────────────────
async function queueSubmissionOffline(payload) {
  return new Promise(resolve => {
    chrome.storage.local.get('offlineQueue', ({ offlineQueue }) => {
      const queue = offlineQueue ?? [];
      queue.push({
        payload,
        timestamp: Date.now(),
        id: 'offline_' + Date.now()
      });
      chrome.storage.local.set({ offlineQueue: queue }, resolve);
    });
  });
}

async function processOfflineQueue() {
  return new Promise(resolve => {
    chrome.storage.local.get(['offlineQueue', 'jwt'], async ({ offlineQueue, jwt }) => {
      if (!offlineQueue || offlineQueue.length === 0 || !jwt) {
        resolve({ processed: 0 });
        return;
      }

      let processed = 0;
      const failedItems = [];

      for (const item of offlineQueue) {
        try {
          await syncSolution(item.payload);
          processed++;
        } catch (err) {
          console.error('Failed to sync offline item:', err);
          failedItems.push(item);
        }
      }

      // Update queue with failed items
      chrome.storage.local.set({ offlineQueue: failedItems }, () => {
        resolve({ processed, remaining: failedItems.length });
      });
    });
  });
}

// ─── Storage helpers ──────────────────────────────────────────────────────────
function getToken() {
  return new Promise(resolve => {
    chrome.storage.local.get('jwt', ({ jwt }) => resolve(jwt ?? null));
  });
}

async function getStoredStatus() {
  return new Promise(resolve => {
    chrome.storage.local.get(['jwt', 'syncHistory', 'offlineQueue'], (storage) => {
      resolve({
        isAuthenticated: !!storage.jwt,
        syncHistory: storage.syncHistory ?? [],
        offlineQueue: storage.offlineQueue ?? [],
      });
    });
  });
}

async function appendSyncHistory(entry) {
  return new Promise(resolve => {
    chrome.storage.local.get('syncHistory', ({ syncHistory }) => {
      const history = syncHistory ?? [];
      history.unshift(entry);
      // Keep last 50 entries
      chrome.storage.local.set({ syncHistory: history.slice(0, 50) }, resolve);
    });
  });
}

// ─── Periodic sync of offline queue when connection restored ──────────────────
chrome.runtime.onStartup.addListener(() => {
  console.log('[LeetHub AI BG] Service worker started');
  processOfflineQueue().catch(err => console.error('Error processing offline queue:', err));
});

// Listen for connection changes
chrome.runtime.onConnect.addListener((port) => {
  if (port.name === 'keep-alive') {
    port.onDisconnect.addListener(() => {
      processOfflineQueue().catch(err => console.error('Error processing offline queue:', err));
    });
  }
});
