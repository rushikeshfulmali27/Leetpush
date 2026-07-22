/**
 * LeetHub AI — Token Bridge
 * 
 * This content script runs on the LeetHub frontend (localhost:5173).
 * It watches for the JWT access token in localStorage and automatically
 * syncs it to the Chrome extension's storage so the extension can
 * authenticate with the backend without requiring a separate login flow.
 */
(() => {
  'use strict';

  const POLL_INTERVAL = 3000; // Check every 3 seconds
  let lastToken = null;

  function syncToken() {
    const token = localStorage.getItem('accessToken');
    if (token && token !== lastToken) {
      lastToken = token;
      try {
        chrome.runtime.sendMessage({ type: 'SAVE_TOKEN', token }, (response) => {
          if (chrome.runtime.lastError) {
            console.warn('[LeetHub AI Bridge] Could not sync token:', chrome.runtime.lastError.message);
            return;
          }
          if (response?.success) {
            console.log('[LeetHub AI Bridge] JWT synced to extension');
          }
        });
      } catch (err) {
        // Extension context not available
      }
    }
  }

  // Sync immediately
  syncToken();

  // Also poll periodically in case the user logs in while the page is open
  setInterval(syncToken, POLL_INTERVAL);

  // Listen for storage events (token changes from other tabs)
  window.addEventListener('storage', (e) => {
    if (e.key === 'accessToken') {
      syncToken();
    }
  });
})();
