/**
 * LeetHub AI — Content Script
 * Runs on: https://leetcode.com/problems/*
 *
 * Detects a successful LeetCode submission by polling the submission result
 * panel, then extracts all solution metadata and sends it to the background
 * service worker for syncing.
 */

(() => {
  'use strict';

  // ─── Constants ────────────────────────────────────────────────────────────
  const POLL_INTERVAL_MS   = 2000;
  const MAX_POLLS          = 30;   // Give up after 60 s
  const SYNCED_SUBMISSIONS = new Set(); // Prevent duplicate sync within session

  // ─── Helpers ──────────────────────────────────────────────────────────────
  const getMetaContent = (name) =>
    document.querySelector(`meta[name="${name}"]`)?.content ?? null;

  const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

  // Extracts the code from the editor (Monaco)
  const extractCode = () => {
    const lines = document.querySelectorAll('.view-line');
    if (lines.length > 0) {
      return Array.from(lines).map(l => l.textContent).join('\n');
    }
    // Fallback: CodeMirror
    const cm = document.querySelector('.CodeMirror');
    if (cm?.CodeMirror) return cm.CodeMirror.getValue();
    return null;
  };

  // Extracts the selected language from the language dropdown
  const extractLanguage = () => {
    const sel = document.querySelector('[data-cy="lang-select"] button')
      ?? document.querySelector('.ant-select-selection-item');
    return sel?.textContent?.trim() ?? 'unknown';
  };

  // Extracts difficulty from the problem page
  const extractDifficulty = () => {
    const el = document.querySelector('[data-difficulty]')
      ?? document.querySelector('.text-difficulty-easy, .text-difficulty-medium, .text-difficulty-hard');
    if (el) return el.textContent.trim().toUpperCase();
    // Try meta tag
    const meta = getMetaContent('description');
    if (meta?.includes('Easy'))   return 'EASY';
    if (meta?.includes('Medium')) return 'MEDIUM';
    if (meta?.includes('Hard'))   return 'HARD';
    return 'MEDIUM';
  };

  // Extracts tags from the page
  const extractTags = () => {
    const tagEls = document.querySelectorAll('a.topic-tag__1jni, [data-cy="question-topics"] a');
    return Array.from(tagEls).map(t => t.textContent.trim()).filter(Boolean);
  };

  // Parses runtime and memory from submission result
  const parsePerformance = () => {
    const text = document.body.innerText;
    const runtimeMatch = text.match(/Runtime[:\s]+(\d+)\s*ms/i);
    const memoryMatch  = text.match(/Memory[:\s]+([\d.]+)\s*MB/i);
    return {
      runtimeMs:         runtimeMatch ? parseInt(runtimeMatch[1])                         : null,
      memoryKb:          memoryMatch  ? Math.round(parseFloat(memoryMatch[1]) * 1024)     : null,
      runtimePercentile: text.match(/Beats\s+([\d.]+)%.*runtime/i)?.[1] + '%'             ?? null,
      memoryPercentile:  text.match(/Beats\s+([\d.]+)%.*memory/i)?.[1]  + '%'             ?? null,
    };
  };

  // ─── Problem metadata ─────────────────────────────────────────────────────
  const extractProblemMetadata = () => {
    const url   = window.location.href;
    // Slug from URL: /problems/two-sum/
    const slugMatch = url.match(/\/problems\/([^/]+)\//);
    const slug  = slugMatch?.[1] ?? '';

    // Title from page heading
    const titleEl = document.querySelector('[data-cy="question-title"]')
      ?? document.querySelector('.text-title-large a')
      ?? document.querySelector('h4[data-cy="question-title"]');
    const title = titleEl?.textContent?.trim() ?? slug.replace(/-/g, ' ');

    // Problem number from title or meta
    const numMatch = document.title.match(/^(\d+)\./);
    const leetcodeId = numMatch?.[1] ?? slug;

    return { slug, title, leetcodeId };
  };

  // ─── Submission detection ──────────────────────────────────────────────────
  const isAccepted = () => {
    const successSelectors = [
      '[data-e2e-locator="submission-result"]',
      '.success__3Ai7',
      'span.text-green-s',
    ];
    for (const sel of successSelectors) {
      const el = document.querySelector(sel);
      if (el && el.textContent.includes('Accepted')) return true;
    }
    // Check for "Accepted" text in result area
    const result = document.querySelector('[class*="result"]');
    return result?.textContent?.includes('Accepted') ?? false;
  };

  // ─── Core: poll and sync ──────────────────────────────────────────────────
  const trySync = async (submissionKey) => {
    if (SYNCED_SUBMISSIONS.has(submissionKey)) return;

    for (let i = 0; i < MAX_POLLS; i++) {
      await sleep(POLL_INTERVAL_MS);

      if (!isAccepted()) continue;

      // Accepted — gather all data
      const { slug, title, leetcodeId } = extractProblemMetadata();
      const code = extractCode();
      const language = extractLanguage();
      const difficulty = extractDifficulty();
      const tags = extractTags();
      const perf = parsePerformance();

      // Extracts problem description
      const descEl = document.querySelector('[data-track-load="description_content"]') 
        ?? document.querySelector('.x-raw-html')
        ?? document.querySelector('.content__u3I1')
        ?? document.querySelector('[data-cy="question-content"]')
        ?? document.querySelector('.elfjS'); // LeetCode's latest class for description
      const problemDescription = descEl ? descEl.innerHTML.trim() : '';

      if (!code) {
        console.warn('[LeetHub AI] Could not extract code');
        continue;
      }

      const payload = {
        leetcodeId,
        title,
        titleSlug: slug,
        difficulty,
        language,
        code,
        problemDescription,
        tags,
        runtimeMs:         perf.runtimeMs,
        memoryKb:          perf.memoryKb,
        runtimePercentile: perf.runtimePercentile,
        memoryPercentile:  perf.memoryPercentile,
        submittedAt:       new Date().toISOString(),
      };

      console.log('[LeetHub AI] Submission detected, syncing:', payload);
      SYNCED_SUBMISSIONS.add(submissionKey);

      // Send to service worker (with retry for service worker wake-up)
      const sendWithRetry = (retriesLeft) => {
        try {
          chrome.runtime.sendMessage({ type: 'SYNC_SOLUTION', payload }, (response) => {
            if (chrome.runtime.lastError) {
              console.error('[LeetHub AI] Error sending to background:', chrome.runtime.lastError);
              if (retriesLeft > 0) {
                console.log('[LeetHub AI] Retrying message send...');
                setTimeout(() => sendWithRetry(retriesLeft - 1), 1000);
                return;
              }
              showToast('LeetHub AI: Extension error. Try reloading this page.', 'error');
              return;
            }
            if (response?.success) {
              showToast('LeetHub AI: Solution syncing to GitHub...');
            } else {
              showToast('LeetHub AI: ' + (response?.error || 'Sync failed. Check extension settings.'), 'error');
            }
          });
        } catch (err) {
          console.error('[LeetHub AI] sendMessage threw:', err);
          if (retriesLeft > 0) {
            setTimeout(() => sendWithRetry(retriesLeft - 1), 1000);
          } else {
            showToast('LeetHub AI: Extension context lost. Please reload this page.', 'error');
          }
        }
      };
      sendWithRetry(2);

      return;
    }
  };

  // ─── Toast notification ────────────────────────────────────────────────────
  const showToast = (msg, type = 'success') => {
    const existing = document.getElementById('leethub-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.id = 'leethub-toast';
    toast.textContent = msg;
    toast.style.cssText = `
      position: fixed; bottom: 24px; right: 24px; z-index: 99999;
      padding: 12px 18px; border-radius: 8px; font-size: 14px; font-weight: 500;
      color: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.4);
      background: ${type === 'error' ? '#ef4444' : '#10b981'};
      animation: slideIn 0.3s ease;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
  };

  // ─── Submit button interception ────────────────────────────────────────────
  const attachSubmitListener = () => {
    const submitBtn = document.querySelector('[data-e2e-locator="console-submit-button"]')
      ?? document.querySelector('button[data-cy="submit-code"]')
      ?? document.querySelector('[role="button"][jsname][aria-label*="Submit"]');

    if (!submitBtn || submitBtn.dataset.leethubAttached) return;
    submitBtn.dataset.leethubAttached = 'true';

    submitBtn.addEventListener('click', () => {
      const key = `${window.location.pathname}_${Date.now()}`;
      setTimeout(() => trySync(key), 1000);
    }, { capture: true });

    console.log('[LeetHub AI] Submit button intercepted');
  };

  // ─── Observe DOM for button appearance (SPA) ──────────────────────────────
  const observer = new MutationObserver(() => attachSubmitListener());
  observer.observe(document.body, { childList: true, subtree: true });
  attachSubmitListener();

  // Notify background script that content script is ready
  try {
    chrome.runtime.sendMessage({ type: 'CONTENT_SCRIPT_READY' });
  } catch (err) {
    // Extension context might not be available in some scenarios
  }
})();
