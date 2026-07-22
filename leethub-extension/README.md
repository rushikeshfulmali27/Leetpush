# LeetHub AI — Chrome Extension

## Loading the Extension (Developer Mode)

1. Open Chrome and go to `chrome://extensions`
2. Enable **Developer mode** (top-right toggle)
3. Click **"Load unpacked"**
4. Select the `leethub-extension/` folder
5. The LeetHub AI icon will appear in your toolbar

## Authentication

The extension needs your JWT token to communicate with the backend:

**Option A — Dashboard Login (Recommended)**
1. Log in at http://localhost:5173
2. Open the extension popup → click **"Enter API Token"**
3. Open DevTools on the dashboard page → Application → Local Storage → copy the `jwt` value
4. Paste it into the extension popup

**Option B — Direct OAuth**
1. Click **"Sign in with GitHub"** in the popup — this opens the backend OAuth flow
2. After login, the backend redirects to the dashboard with the token in the URL
3. Copy the token from the URL and paste it into the extension

## How It Works

1. Navigate to any LeetCode problem: `https://leetcode.com/problems/...`
2. Write and submit your solution normally
3. When the result shows **"Accepted"**, the extension automatically:
   - Extracts your code, language, difficulty, and performance metrics
   - Sends the data to the LeetHub AI backend
   - The backend generates an AI explanation and pushes all files to GitHub
4. A toast notification confirms the sync

## Files

| File | Purpose |
|------|---------|
| `manifest.json` | Extension configuration (Manifest V3) |
| `content.js`    | Runs on LeetCode, detects submissions |
| `background.js` | Service worker, calls the backend API |
| `popup.html`    | Extension popup UI |
| `popup.js`      | Popup logic, stats and history |
| `icons/`        | Extension icons (add PNG files here) |
