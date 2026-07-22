/**
 * LeetHub AI — Popup Script
 */

const API_BASE = 'http://localhost:8080/api/v1';

// ─── DOM references ────────────────────────────────────────────────────────────
const authSection   = document.getElementById('auth-section');
const mainSection   = document.getElementById('main-section');
const statusDot     = document.getElementById('statusDot');
const loginBtn      = document.getElementById('loginBtn');
const tokenBtn      = document.getElementById('tokenBtn');
const tokenInput    = document.getElementById('tokenInput');
const tokenField    = document.getElementById('tokenField');
const saveTokenBtn  = document.getElementById('saveTokenBtn');
const logoutBtn     = document.getElementById('logoutBtn');
const historyList   = document.getElementById('historyList');
const emptyHistory  = document.getElementById('emptyHistory');
const statTotal     = document.getElementById('statTotal');
const statToday     = document.getElementById('statToday');
const statStreak    = document.getElementById('statStreak');

// ─── Initialise ────────────────────────────────────────────────────────────────
chrome.runtime.sendMessage({ type: 'GET_STATUS' }, ({ isAuthenticated, syncHistory }) => {
  if (isAuthenticated) {
    showMainView(syncHistory);
    loadStats();
  } else {
    showAuthView();
  }
});

// ─── Auth view ─────────────────────────────────────────────────────────────────
function showAuthView() {
  authSection.classList.add('visible');
  mainSection.classList.remove('visible');
  statusDot.classList.add('offline');
  logoutBtn.style.display = 'none';
}

loginBtn.addEventListener('click', () => {
  chrome.tabs.create({ url: `${API_BASE.replace('/api/v1', '')}/oauth2/authorization/github` });
});

tokenBtn.addEventListener('click', () => {
  tokenInput.style.display = tokenInput.style.display === 'none' ? 'block' : 'none';
});

saveTokenBtn.addEventListener('click', () => {
  const token = tokenField.value.trim();
  if (!token) return;
  chrome.runtime.sendMessage({ type: 'SAVE_TOKEN', token }, () => {
    loadStats();
    showMainView([]);
  });
});

// ─── Main view ─────────────────────────────────────────────────────────────────
function showMainView(syncHistory) {
  authSection.classList.remove('visible');
  mainSection.classList.add('visible');
  statusDot.classList.remove('offline');
  logoutBtn.style.display = 'block';
  renderHistory(syncHistory);
}

logoutBtn.addEventListener('click', () => {
  chrome.storage.local.remove(['jwt', 'syncHistory'], () => showAuthView());
});

// ─── History render ───────────────────────────────────────────────────────────
function renderHistory(history) {
  if (!history || history.length === 0) {
    emptyHistory.style.display = 'block';
    return;
  }
  emptyHistory.style.display = 'none';

  // Remove previous items (keep emptyHistory)
  Array.from(historyList.querySelectorAll('.history-item')).forEach(el => el.remove());

  history.slice(0, 8).forEach(item => {
    const badgeClass = `badge-${item.difficulty?.toLowerCase() ?? 'medium'}`;
    const div = document.createElement('div');
    div.className = 'history-item';
    div.innerHTML = `
      <div>
        <div class="history-title">${item.title ?? 'Unknown'}</div>
        <div class="history-meta">${item.language} · ${formatDate(item.syncedAt)}</div>
      </div>
      <span class="badge ${badgeClass}">${item.difficulty ?? ''}</span>
    `;
    historyList.appendChild(div);
  });
}

// ─── Load analytics stats ──────────────────────────────────────────────────────
async function loadStats() {
  try {
    const jwt = await getToken();
    if (!jwt) return;

    const res = await fetch(`${API_BASE}/analytics/summary`, {
      headers: { Authorization: `Bearer ${jwt}` },
    });
    if (!res.ok) return;

    const body = await res.json();
    const data = body.data ?? body;
    statTotal.textContent  = data.totalSolved ?? '—';
    statToday.textContent  = data.thisWeekSolved ?? '—';
    statStreak.textContent = (data.currentStreak ?? '—') + (data.currentStreak !== undefined ? '🔥' : '');
  } catch (e) {
    console.error('[LeetHub AI Popup] Stats load failed:', e);
  }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function getToken() {
  return new Promise(resolve =>
    chrome.storage.local.get('jwt', ({ jwt }) => resolve(jwt ?? null))
  );
}

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch { return ''; }
}
