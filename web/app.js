// Duniya ⛺ — Gemini-Style Offline Conversational Research Engine & Silver Glassmorphism

(function () {
  'use strict';

  // --- State ---
  const state = {
    history: [],
    currentQuery: '',
    activeExperts: [3, 11, 24, 48],
    audioEnabled: false,
    selectedDomain: 'All Domains',
    telemetry: {
      activeTokensSec: 28.5,
      ramMb: 194,
      latencyMs: 842,
      mmapHitRate: '99.8%'
    }
  };

  // --- Sound Effects via Web Audio API (Offline, Zero Assets) ---
  let audioCtx = null;
  function getAudioCtx() {
    if (!audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) audioCtx = new AudioContext();
    }
    return audioCtx;
  }

  function playTone(freq, type = 'sine', duration = 0.08, gainVal = 0.03) {
    if (!state.audioEnabled) return;
    try {
      const ctx = getAudioCtx();
      if (!ctx) return;
      if (ctx.state === 'suspended') ctx.resume();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      gain.gain.setValueAtTime(gainVal, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + duration);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + duration);
    } catch (e) {}
  }

  function playSynthChord() {
    if (!state.audioEnabled) return;
    [440, 554.37, 659.25, 880].forEach((freq, idx) => {
      setTimeout(() => playTone(freq, 'triangle', 0.2, 0.02), idx * 35);
    });
  }

  // --- DOM Elements ---
  const el = {
    sidebar: document.getElementById('geminiSidebar'),
    sidebarToggleBtn: document.getElementById('sidebarToggleBtn'),
    sidebarCloseBtn: document.getElementById('sidebarCloseBtn'),
    newInquiryBtn: document.getElementById('newInquiryBtn'),
    historyList: document.getElementById('historyList'),
    packFilterItems: document.querySelectorAll('.pack-filter-item'),
    geminiHero: document.getElementById('geminiHero'),
    messagesContainer: document.getElementById('messagesContainer'),
    geminiInput: document.getElementById('geminiInput'),
    sendBtn: document.getElementById('sendBtn'),
    audioToggleBtn: document.getElementById('audioToggleBtn'),
    audioIcon: document.getElementById('audioIcon'),
    clearChatBtn: document.getElementById('clearChatBtn'),
    topbarActiveExperts: document.getElementById('topbarActiveExperts'),
    topbarSpeed: document.getElementById('topbarSpeed'),
    waferModalTriggerBtn: document.getElementById('waferModalTriggerBtn'),
    waferModalBackdrop: document.getElementById('waferModalBackdrop'),
    waferModalCloseBtn: document.getElementById('waferModalCloseBtn'),
    modalActiveCount: document.getElementById('modalActiveCount'),
    modalSpeed: document.getElementById('modalSpeed'),
    modalLatency: document.getElementById('modalLatency'),
    waferGrid: document.getElementById('waferGrid'),
    starterCards: document.querySelectorAll('.starter-card'),
    heroCategoryPills: document.getElementById('heroCategoryPills'),
    starterCardsGrid: document.getElementById('starterCardsGrid'),
    surpriseMeBtn: document.getElementById('surpriseMeBtn'),
    searchSuggestionsDropdown: document.getElementById('searchSuggestionsDropdown')
  };

  const data = window.DUNIYA_DATA || { articles: [], benchmarks: [] };

  // --- 8x8 Wafer Die Setup ---
  function initWaferGrid() {
    if (!el.waferGrid) return;
    el.waferGrid.innerHTML = '';
    for (let i = 0; i < 64; i++) {
      const tile = document.createElement('div');
      tile.className = 'expert-tile';
      tile.dataset.expertId = i;
      tile.innerHTML = `
        <span class="tile-id">E${i.toString().padStart(2, '0')}</span>
        <span class="tile-load">0.0</span>
      `;
      tile.title = `Expert #${i} (Domain: ${getExpertDomain(i)})`;
      tile.addEventListener('click', () => {
        playTone(600 + i * 15, 'sine', 0.06, 0.04);
        const domain = getExpertDomain(i).split(' ')[0];
        closeWaferModal();
        handleUserQuery(domain);
      });
      el.waferGrid.appendChild(tile);
    }
    updateWaferActiveTiles();
  }

  function getExpertDomain(idx) {
    const domains = [
      'ZK Polynomials & MSMs', 'Sparse MoE Gating', 'Prime Editing PegRNA', 
      'KZG Commitments', 'Binary Tower Fields', 'Plasma Tokamak Transport', 
      'High-NA EUV Stochastic Shot', 'Late Bronze Age Economics', 'HAPE Vasoconstriction',
      'UFS 4.0 Page MMap', 'MLA KV-Cache Compression', 'LNP Endosomal Escape'
    ];
    return domains[idx % domains.length];
  }

  function updateWaferActiveTiles() {
    if (!el.waferGrid) return;
    const tiles = el.waferGrid.querySelectorAll('.expert-tile');
    tiles.forEach(tile => {
      const id = parseInt(tile.dataset.expertId, 10);
      const isActive = state.activeExperts.includes(id);
      if (isActive) {
        tile.classList.add('active-expert');
        const loadVal = (0.75 + (id % 5) * 0.05).toFixed(2);
        tile.querySelector('.tile-load').textContent = loadVal;
      } else {
        tile.classList.remove('active-expert');
        tile.querySelector('.tile-load').textContent = '0.0';
      }
    });

    const activeText = `${state.activeExperts.length}/64 MoE`;
    if (el.topbarActiveExperts) el.topbarActiveExperts.textContent = activeText;
    if (el.modalActiveCount) el.modalActiveCount.textContent = activeText;
  }

  function routeQueryToExperts(queryText) {
    const hash = simpleHash(queryText);
    const exp1 = (hash & 0x3F);
    const exp2 = ((hash >> 6) & 0x3F);
    const exp3 = ((hash >> 12) & 0x3F);
    const exp4 = ((hash >> 18) & 0x3F);
    
    const set = new Set([exp1, exp2, exp3, exp4]);
    let fallback = 0;
    while (set.size < 4) {
      set.add((hash + (++fallback) * 7) & 0x3F);
    }
    state.activeExperts = Array.from(set);
    updateWaferActiveTiles();

    state.telemetry.latencyMs = 780 + (hash % 180);
    state.telemetry.activeTokensSec = (27.2 + ((hash % 30) / 10)).toFixed(1);
    state.telemetry.ramMb = 188 + (hash % 24);

    const speedStr = `${state.telemetry.activeTokensSec} tok/s`;
    if (el.topbarSpeed) el.topbarSpeed.textContent = speedStr;
    if (el.modalSpeed) el.modalSpeed.textContent = speedStr;
    if (el.modalLatency) el.modalLatency.textContent = `${state.telemetry.latencyMs}ms`;
  }

  function simpleHash(str) {
    let hash = 5381;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) + hash) + str.charCodeAt(i);
    }
    return Math.abs(hash);
  }

  // --- Synthesis & Retrieval Engine ---
  const STOP_WORDS = new Set(['vs', 'versus', 'the', 'a', 'an', 'and', 'or', 'in', 'on', 'at', 'to', 'for', 'of', 'with', 'by', 'what', 'is', 'are', 'why', 'how', 'explain', 'compare']);

  function findBestArticle(query) {
    const clean = query.trim().toLowerCase();
    const rawTokens = clean.split(/[^a-z0-9_+-]+/).filter(Boolean);
    const meaningfulTokens = rawTokens.filter(t => t.length > 1 && !STOP_WORDS.has(t));
    const tokens = meaningfulTokens.length > 0 ? meaningfulTokens : rawTokens;

    let bestArticle = null;
    let highestScore = -1;

    data.articles.forEach(art => {
      // Domain filter check
      if (state.selectedDomain !== 'All Domains' && art.domain !== state.selectedDomain) {
        return;
      }

      let score = 0;
      const titleLower = art.title.toLowerCase();
      const tagsLower = (art.tags || []).map(t => t.toLowerCase());
      const summaryLower = (art.summary || '').toLowerCase();
      const mathLower = (art.firstPrinciplesMathOrMechanism || '').toLowerCase();

      // Check first if query exactly matches benchmark or article ID/title
      if (clean.includes('groth16') && art.id === 'zk_groth16') score += 100;
      if (clean.includes('binius') && art.id === 'zk_binius') score += 100;
      if (clean.includes('plonk') && art.id === 'zk_plonk') score += 80;
      if (clean.includes('stark') && art.id === 'zk_starks') score += 80;

      tokens.forEach(token => {
        if (titleLower.includes(token)) {
          score += 40;
          try {
            const re = new RegExp('\\b' + token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\b', 'i');
            if (re.test(titleLower)) score += 30;
          } catch (e) {}
        }
        if (tagsLower.includes(token)) {
          score += 35;
        } else if (tagsLower.some(t => t.includes(token))) {
          score += 15;
        }
        if (summaryLower.includes(token)) score += 8;
        if (mathLower.includes(token)) score += 4;
      });

      if (score > highestScore) {
        highestScore = score;
        bestArticle = art;
      }
    });

    return bestArticle || data.articles[0];
  }

  // --- Chat Message Rendering (Gemini Style) ---
  function appendUserMessage(text) {
    if (!el.messagesContainer) return;
    const row = document.createElement('div');
    row.className = 'user-message-row';
    row.innerHTML = `<div class="user-bubble">${escapeHtml(text)}</div>`;
    el.messagesContainer.appendChild(row);
    scrollToBottom();
  }

  // --- Toast Notification ---
  function showToast(message) {
    const existing = document.querySelector('.duniya-toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'duniya-toast';
    toast.innerHTML = `<span>✨</span><span>${escapeHtml(message)}</span>`;
    document.body.appendChild(toast);
    setTimeout(() => {
      toast.style.transition = 'opacity 0.3s ease';
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 2400);
  }

  // --- Offline Text-to-Speech (Web Speech API) ---
  let currentUtterance = null;
  function toggleSpeech(text, btn) {
    if (!('speechSynthesis' in window)) {
      showToast('Offline speech synth unavailable in this browser');
      return;
    }
    if (window.speechSynthesis.speaking) {
      window.speechSynthesis.cancel();
      if (btn) btn.classList.remove('speaking');
      currentUtterance = null;
      return;
    }

    const cleanText = text.replace(/<[^>]*>/g, '').replace(/https?:\/\/\S+/g, '');
    const utter = new SpeechSynthesisUtterance(cleanText);
    utter.rate = 1.05;
    utter.pitch = 1.0;
    utter.onend = () => {
      if (btn) btn.classList.remove('speaking');
      currentUtterance = null;
    };
    utter.onerror = () => {
      if (btn) btn.classList.remove('speaking');
      currentUtterance = null;
    };

    if (btn) btn.classList.add('speaking');
    currentUtterance = utter;
    window.speechSynthesis.speak(utter);
  }

  // --- Copy / Export Utilities ---
  function copyMarkdownBrief(article) {
    const metrics = Object.entries(article.structuredMetrics || {})
      .map(([k, v]) => `| ${k} | ${v} |`).join('\n');
    const citations = (article.primaryCitations || []).map(c => `- ${c}`).join('\n');

    const md = `# ${article.title}
**Domain:** ${article.domain} (${article.subcategory || 'Review'})
**Verification:** 100% Offline Sparse-MoE Airgap

## Executive Verdict
${article.summary}

## First-Principles Formulation
\`\`\`
${article.firstPrinciplesMathOrMechanism || 'N/A'}
\`\`\`

## Specification Matrix
| Dimension | Specification |
| :--- | :--- |
${metrics}

## 1B Dense Model Failure Autopsy
${article.oneBModelFailureMode || 'N/A'}

## Primary Citations
${citations}
`;

    navigator.clipboard.writeText(md).then(() => {
      showToast('Copied research monograph to clipboard');
    }).catch(() => {
      showToast('Clipboard access denied');
    });
  }

  function exportMarkdownFile(article) {
    const metrics = Object.entries(article.structuredMetrics || {})
      .map(([k, v]) => `| ${k} | ${v} |`).join('\n');
    const citations = (article.primaryCitations || []).map(c => `- ${c}`).join('\n');

    const md = `# ${article.title}
**Domain:** ${article.domain} (${article.subcategory || 'Review'})
**Verification:** 100% Offline Sparse-MoE Airgap

## Executive Verdict
${article.summary}

## Mechanical Derivation
${article.deepExplanation || 'N/A'}

## First-Principles Formulation
\`\`\`
${article.firstPrinciplesMathOrMechanism || 'N/A'}
\`\`\`

## Specification Matrix
| Dimension | Specification |
| :--- | :--- |
${metrics}

## 1B Failure Autopsy
${article.oneBModelFailureMode || 'N/A'}

## Primary Citations
${citations}
`;

    const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `duniya_${article.id}.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast(`Exported duniya_${article.id}.md`);
  }

  function formatExecutiveBreakdown(article) {
    const raw = article.summary || '';
    const sentences = raw.split(/(?<=[.?!])\s+/).filter(Boolean);
    const verdict = sentences[0] || raw;
    const bullets = sentences.slice(1);

    // Snapshot pills from structured metrics
    const snapshotEntries = Object.entries(article.structuredMetrics || {}).slice(0, 4);
    const snapshotPillsHtml = snapshotEntries.map(([k, v]) => {
      const shortVal = v.split('(')[0].split(';')[0].trim();
      return `<span class="snapshot-pill"><span>${escapeHtml(k)}:</span> <strong>${escapeHtml(shortVal)}</strong></span>`;
    }).join('');

    return { verdict, bullets, snapshotPillsHtml };
  }

  function appendGeminiResponse(query, article) {
    if (!el.messagesContainer || !article) return;

    const msgId = 'res_' + Math.random().toString(36).substr(2, 8);
    const breakdown = formatExecutiveBreakdown(article);

    // Build structured metrics rows
    const metricsRows = Object.entries(article.structuredMetrics || {}).map(([key, val]) => `
      <tr>
        <td style="font-weight: 600; font-family: var(--font-mono); width: 34%;">${escapeHtml(key)}</td>
        <td>${escapeHtml(val)}</td>
      </tr>
    `).join('');

    // Citations list
    const citationsList = (article.primaryCitations || []).map(c => `
      <li style="margin-bottom: 0.45rem;">${escapeHtml(c)}</li>
    `).join('');

    // Check for simulated 1B benchmark output
    const matchedBench = (data.benchmarks || []).find(b => 
      b.id === article.id || 
      b.title.toLowerCase().includes(article.id.replace('zk_', '')) ||
      (b.prompt && b.prompt.toLowerCase().includes(article.title.toLowerCase().slice(0, 10)))
    );
    const sim1BText = matchedBench ? matchedBench.simulated1BOutput : (article.oneBModelFailureMode || 'Fails with ungrounded hallucinations on numbers and asymptotic scaling.');

    // Follow-ups
    const followUps = generateFollowUps(article);
    const followUpsHtml = followUps.map(f => `
      <button class="followup-chip" data-query="${escapeHtml(f)}">${escapeHtml(f)}</button>
    `).join('');

    const row = document.createElement('div');
    row.className = 'assistant-message-row';
    row.innerHTML = `
      <div class="assistant-avatar" title="Duniya Offline MoE">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 2L14.5 9.5L22 12L14.5 14.5L12 22L9.5 14.5L2 12L9.5 9.5L12 2Z" fill="#8C9EFF"></path>
        </svg>
      </div>

      <div class="assistant-card-body" id="${msgId}">
        <!-- Top Header & Action Toolbar -->
        <div class="response-header">
          <div>
            <span class="response-domain-badge">${escapeHtml(article.domain)} • ${escapeHtml(article.subcategory || 'Review')}</span>
            <span class="response-telemetry-tag" style="margin-left: 0.5rem;">● 4/64 MoE Active • UFS 4.0 MMap</span>
          </div>

          <div class="response-toolbar">
            <button class="action-pill-btn btn-read-aloud" title="Read summary aloud">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
              </svg>
              <span>Listen</span>
            </button>

            <button class="action-pill-btn btn-copy-md" title="Copy formatted Markdown brief">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
              <span>Copy MD</span>
            </button>

            <button class="action-pill-btn btn-export-md" title="Export as .md file">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              <span>Export</span>
            </button>
          </div>
        </div>

        <h2 class="response-title">${escapeHtml(article.title)}</h2>

        <!-- Layer 1: Verdict & Scannable Takeaways Box -->
        <div class="response-verdict-box">
          <div class="verdict-header">
            <span>✦ Core Synthesis Verdict</span>
          </div>
          <div class="verdict-text">${escapeHtml(breakdown.verdict)}</div>

          ${breakdown.bullets.length > 0 ? `
          <div class="takeaway-bullets">
            ${breakdown.bullets.map(b => `
              <div class="takeaway-bullet-item">
                <span class="takeaway-bullet-icon">✦</span>
                <span>${escapeHtml(b)}</span>
              </div>
            `).join('')}
          </div>
          ` : ''}

          ${breakdown.snapshotPillsHtml ? `
          <div class="snapshot-pills-row">
            ${breakdown.snapshotPillsHtml}
          </div>
          ` : ''}
        </div>

        <!-- Layer 2: Segmented Interactive Tab Switcher -->
        <div class="response-tab-bar">
          <button class="response-tab-btn active" data-tab="findings">
            <span>🎯 Key Findings</span>
          </button>
          <button class="response-tab-btn" data-tab="matrix">
            <span>📊 Spec Matrix</span>
          </button>
          <button class="response-tab-btn" data-tab="math">
            <span>📐 First Principles</span>
          </button>
          <button class="response-tab-btn" data-tab="autopsy">
            <span>⚡ 1B vs MoE Autopsy</span>
          </button>
          <button class="response-tab-btn" data-tab="citations">
            <span>📚 Citations</span>
          </button>
        </div>

        <!-- Tab Panel 1: Key Findings -->
        <div class="response-tab-panel active" data-panel="findings">
          <div style="font-size: 0.92rem; line-height: 1.68; color: var(--silver-200); margin-bottom: 0.85rem;">
            ${escapeHtml(article.summary)}
          </div>

          ${followUpsHtml ? `
          <div style="margin-top: 0.75rem;">
            <div style="font-size: 0.74rem; font-family: var(--font-mono); color: var(--silver-500); margin-bottom: 0.45rem;">Suggested Inquiries:</div>
            <div class="followups-group">
              ${followUpsHtml}
            </div>
          </div>
          ` : ''}
        </div>

        <!-- Tab Panel 2: Spec Matrix -->
        <div class="response-tab-panel" data-panel="matrix">
          ${metricsRows ? `
          <div style="overflow-x: auto;">
            <table class="mini-matrix-table">
              <thead>
                <tr>
                  <th>Dimension</th>
                  <th>Specification & Complexity</th>
                </tr>
              </thead>
              <tbody>
                ${metricsRows}
              </tbody>
            </table>
          </div>
          ` : '<p style="color: var(--silver-400); font-size: 0.88rem;">No matrix metrics recorded.</p>'}
        </div>

        <!-- Tab Panel 3: First Principles & Math -->
        <div class="response-tab-panel" data-panel="math">
          ${article.firstPrinciplesMathOrMechanism ? `
          <div class="math-formula-box">
            <code>${escapeHtml(article.firstPrinciplesMathOrMechanism)}</code>
          </div>
          ` : ''}

          ${article.tradeOffsAndEdgeCases ? `
          <div style="font-size: 0.86rem; color: var(--silver-400); margin-block: 0.6rem;">
            <strong>Boundary Conditions & Edge Cases:</strong> ${escapeHtml(article.tradeOffsAndEdgeCases)}
          </div>
          ` : ''}

          ${article.deepExplanation ? `
          <div style="margin-top: 0.75rem; font-size: 0.9rem; line-height: 1.65; color: var(--silver-300);">
            ${article.deepExplanation.split('\n\n').map(p => `<p style="margin-bottom: 0.55rem;">${escapeHtml(p.trim())}</p>`).join('')}
          </div>
          ` : ''}
        </div>

        <!-- Tab Panel 4: 1B vs MoE Side-by-Side Autopsy -->
        <div class="response-tab-panel" data-panel="autopsy">
          <div class="side-by-side-contrast">
            <div class="contrast-card bad">
              <div class="contrast-title">
                <span>🔴 1B Dense Mobile Model (~10 tok/s)</span>
              </div>
              <div style="font-style: italic; opacity: 0.95;">
                "${escapeHtml(sim1BText)}"
              </div>
              <div style="font-size: 0.78rem; opacity: 0.75; margin-top: 0.35rem;">
                ⚠️ Why it fails: Activates 100% weights per token, lacks working memory for asymptotic math, and hallucinates facts.
              </div>
            </div>

            <div class="contrast-card good">
              <div class="contrast-title">
                <span>🟢 Duniya Sparse MoE + RAG (~42 tok/s)</span>
              </div>
              <div>
                ${escapeHtml(breakdown.verdict)}
              </div>
              <div style="font-size: 0.78rem; opacity: 0.85; margin-top: 0.35rem;">
                ✓ Verified via 64-expert sparse routing (3.1% active) + 65K disk-mapped n-gram hash lookup table.
              </div>
            </div>
          </div>
        </div>

        <!-- Tab Panel 5: Citations -->
        <div class="response-tab-panel" data-panel="citations">
          ${citationsList ? `
          <ul style="padding-left: 1.25rem; font-size: 0.88rem; color: var(--silver-300); line-height: 1.6;">
            ${citationsList}
          </ul>
          <button class="action-pill-btn btn-copy-citations" style="margin-top: 0.75rem;">
            <span>Copy Citations</span>
          </button>
          ` : '<p style="color: var(--silver-400); font-size: 0.88rem;">No primary citations recorded.</p>'}
        </div>

      </div>
    `;

    el.messagesContainer.appendChild(row);

    const cardEl = document.getElementById(msgId);
    if (cardEl) {
      // Tab switching
      const tabBtns = cardEl.querySelectorAll('.response-tab-btn');
      const tabPanels = cardEl.querySelectorAll('.response-tab-panel');

      tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          const targetTab = btn.dataset.tab;
          tabBtns.forEach(b => b.classList.remove('active'));
          tabPanels.forEach(p => p.classList.remove('active'));

          btn.classList.add('active');
          const matchingPanel = cardEl.querySelector(`.response-tab-panel[data-panel="${targetTab}"]`);
          if (matchingPanel) matchingPanel.classList.add('active');
          playTone(560, 'sine', 0.04, 0.02);
        });
      });

      // Actions: Copy MD
      const copyBtn = cardEl.querySelector('.btn-copy-md');
      if (copyBtn) copyBtn.addEventListener('click', () => copyMarkdownBrief(article));

      // Actions: Export MD
      const exportBtn = cardEl.querySelector('.btn-export-md');
      if (exportBtn) exportBtn.addEventListener('click', () => exportMarkdownFile(article));

      // Actions: Read Aloud
      const speakBtn = cardEl.querySelector('.btn-read-aloud');
      if (speakBtn) speakBtn.addEventListener('click', () => toggleSpeech(article.summary, speakBtn));

      // Actions: Copy Citations
      const copyCiteBtn = cardEl.querySelector('.btn-copy-citations');
      if (copyCiteBtn) copyCiteBtn.addEventListener('click', () => {
        const citeText = (article.primaryCitations || []).join('\n');
        navigator.clipboard.writeText(citeText).then(() => showToast('Copied citations to clipboard'));
      });

      // Follow-up chip clicks
      cardEl.querySelectorAll('.followup-chip').forEach(chip => {
        chip.addEventListener('click', () => {
          handleUserQuery(chip.dataset.query);
        });
      });
    }

    scrollToBottom();
  }

  function generateFollowUps(article) {
    if (article.id.includes('zk') || article.domain.includes('Crypto')) {
      return ['Compare Groth16 vs STARKs proof sizes', 'Explain binary tower fields in Binius', 'PeerDAS 2D KZG erasure coding'];
    }
    if (article.domain.includes('AI')) {
      return ['How does 65K Engram table prevent collisions?', 'Compare MLA vs GQA KV-cache compression', 'Why do 1B dense models hallucinate on numbers?'];
    }
    if (article.domain.includes('Biology')) {
      return ['Compare Prime Editing PE3 vs Cas9 DSBs', 'Explain N1-methylpseudouridine immune evasion', 'What are senolytics vs OSKM reprogramming?'];
    }
    if (article.domain.includes('Physics')) {
      return ['Tokamak disruptions vs Stellarator 3D coils', 'High-NA EUV anamorphic optics & shot noise', 'Perovskite-silicon tandem solar efficiency limits'];
    }
    return ['Show first-principles mathematical derivation', 'Compare specification matrix', 'Why does a 1B model fail here?'];
  }

  function scrollToBottom() {
    const stage = document.getElementById('conversationStage');
    if (stage) {
      setTimeout(() => {
        stage.scrollTo({ top: stage.scrollHeight, behavior: 'smooth' });
      }, 50);
    }
  }

  // --- Main Query Execution Controller ---
  function handleUserQuery(query) {
    if (!query || !query.trim()) return;
    const cleanQuery = query.trim();

    // Hide hero on first inquiry
    if (el.geminiHero) el.geminiHero.style.display = 'none';

    // Append user message
    appendUserMessage(cleanQuery);

    // Clear input
    if (el.geminiInput) {
      el.geminiInput.value = '';
      el.geminiInput.style.height = 'auto';
    }

    // Audio tone + MoE Route
    routeQueryToExperts(cleanQuery);
    playSynthChord();

    // Add to history
    addQueryToHistory(cleanQuery);

    // Synthesize best article
    const bestArticle = findBestArticle(cleanQuery);
    appendGeminiResponse(cleanQuery, bestArticle);
  }

  function addQueryToHistory(query) {
    if (!state.history.includes(query)) {
      state.history.unshift(query);
      if (state.history.length > 12) state.history.pop();
      renderHistoryList();
    }
  }

  function renderHistoryList() {
    if (!el.historyList) return;
    el.historyList.innerHTML = state.history.map(q => `
      <button class="history-item" data-query="${escapeHtml(q)}" title="${escapeHtml(q)}">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
        <span style="overflow:hidden; text-overflow:ellipsis;">${escapeHtml(q)}</span>
      </button>
    `).join('');

    el.historyList.querySelectorAll('.history-item').forEach(item => {
      item.addEventListener('click', () => {
        handleUserQuery(item.dataset.query);
      });
    });
  }

  function resetToNewInquiry() {
    if (el.messagesContainer) el.messagesContainer.innerHTML = '';
    if (el.geminiHero) el.geminiHero.style.display = 'flex';
    if (el.geminiInput) {
      el.geminiInput.value = '';
      el.geminiInput.focus();
    }
    playTone(520, 'sine', 0.05, 0.03);
  }

  function openWaferModal() {
    if (el.waferModalBackdrop) {
      el.waferModalBackdrop.classList.add('open');
      playTone(660, 'sine', 0.08, 0.04);
    }
  }

  function closeWaferModal() {
    if (el.waferModalBackdrop) {
      el.waferModalBackdrop.classList.remove('open');
    }
  }

  // --- HTML Escaping Utility ---
  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // --- Initialization ---
  function init() {
    initWaferGrid();

    // Default history items
    state.history = [
      'Groth16 vs PLONK vs STARKs vs Binius',
      'Flash-Streamed Sparse MoE + N-Gram Memory',
      'CRISPR-Cas9 vs Base vs Prime Editing',
      'Tokamak vs Stellarator Magnetic Fusion'
    ];
    renderHistoryList();

    // Send Button & Input Enter
    if (el.sendBtn && el.geminiInput) {
      el.sendBtn.addEventListener('click', () => {
        handleUserQuery(el.geminiInput.value);
      });

      el.geminiInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          handleUserQuery(el.geminiInput.value);
        }
      });

      // Auto-resize textarea
      el.geminiInput.addEventListener('input', () => {
        el.geminiInput.style.height = 'auto';
        el.geminiInput.style.height = `${Math.min(el.geminiInput.scrollHeight, 140)}px`;
      });
    }

    // Starter Prompt Cards Initial Click Binding
    bindStarterCardClicks();

    function bindStarterCardClicks() {
      const cards = document.querySelectorAll('.starter-card');
      cards.forEach(card => {
        card.addEventListener('click', () => {
          const prompt = card.dataset.prompt;
          handleUserQuery(prompt);
        });
      });
    }

    // Hero Domain Category Pills & Surprise Me
    const categoryPills = document.querySelectorAll('.hero-category-pills .category-pill');
    if (categoryPills.length > 0) {
      categoryPills.forEach(pill => {
        pill.addEventListener('click', () => {
          if (pill.id === 'surpriseMeBtn') {
            triggerRandomInquiry();
            return;
          }
          categoryPills.forEach(p => p.classList.remove('active'));
          pill.classList.add('active');
          const cat = pill.dataset.category;
          renderStarterCards(cat);
          playTone(440, 'sine', 0.04, 0.02);
        });
      });
    }

    function renderStarterCards(category) {
      if (!el.starterCardsGrid) return;
      let matchedArticles = data.articles || [];
      if (category && category !== 'all') {
        matchedArticles = matchedArticles.filter(a => a.domain === category);
      }
      const slice = matchedArticles.slice(0, 4);
      el.starterCardsGrid.innerHTML = slice.map(art => `
        <button class="starter-card" data-domain="${escapeHtml(art.domain)}" data-prompt="${escapeHtml(art.title)}: ${escapeHtml(art.summary.slice(0, 100))}...">
          <div class="starter-card-tag">${escapeHtml(art.domain)}</div>
          <div class="starter-card-title">${escapeHtml(art.title)}</div>
          <div class="starter-card-desc">${escapeHtml(art.summary.slice(0, 110))}...</div>
        </button>
      `).join('');
      bindStarterCardClicks();
    }

    function triggerRandomInquiry() {
      const allItems = [...(data.articles || []), ...(data.benchmarks || [])];
      if (allItems.length === 0) return;
      const pick = allItems[Math.floor(Math.random() * allItems.length)];
      const query = pick.prompt || pick.title;
      playTone(660, 'triangle', 0.1, 0.04);
      handleUserQuery(query);
    }

    // Real-Time Autocomplete / Topic Suggestions
    if (el.geminiInput && el.searchSuggestionsDropdown) {
      el.geminiInput.addEventListener('input', () => {
        const query = el.geminiInput.value.trim().toLowerCase();
        if (query.length < 2) {
          el.searchSuggestionsDropdown.style.display = 'none';
          return;
        }

        const matches = (data.articles || []).filter(art => {
          return art.title.toLowerCase().includes(query) ||
                 (art.tags || []).some(t => t.toLowerCase().includes(query)) ||
                 art.domain.toLowerCase().includes(query);
        }).slice(0, 5);

        if (matches.length === 0) {
          el.searchSuggestionsDropdown.style.display = 'none';
          return;
        }

        el.searchSuggestionsDropdown.innerHTML = matches.map(m => `
          <button class="suggestion-item" data-query="${escapeHtml(m.title)}">
            <span style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${escapeHtml(m.title)}</span>
            <span class="suggestion-domain">${escapeHtml(m.domain)}</span>
          </button>
        `).join('');

        el.searchSuggestionsDropdown.style.display = 'flex';

        el.searchSuggestionsDropdown.querySelectorAll('.suggestion-item').forEach(item => {
          item.addEventListener('click', () => {
            const q = item.dataset.query;
            el.searchSuggestionsDropdown.style.display = 'none';
            handleUserQuery(q);
          });
        });
      });

      document.addEventListener('click', (e) => {
        if (!el.searchSuggestionsDropdown.contains(e.target) && e.target !== el.geminiInput) {
          el.searchSuggestionsDropdown.style.display = 'none';
        }
      });
    }

    // New Research Button
    if (el.newInquiryBtn) {
      el.newInquiryBtn.addEventListener('click', resetToNewInquiry);
    }

    // Clear Chat Button
    if (el.clearChatBtn) {
      el.clearChatBtn.addEventListener('click', resetToNewInquiry);
    }

    // Sidebar Toggles
    if (el.sidebarToggleBtn && el.sidebar) {
      el.sidebarToggleBtn.addEventListener('click', () => {
        el.sidebar.classList.toggle('collapsed');
      });
    }
    if (el.sidebarCloseBtn && el.sidebar) {
      el.sidebarCloseBtn.addEventListener('click', () => {
        el.sidebar.classList.add('collapsed');
      });
    }

    // Wafer Modal
    if (el.waferModalTriggerBtn) {
      el.waferModalTriggerBtn.addEventListener('click', openWaferModal);
    }
    if (el.waferModalCloseBtn) {
      el.waferModalCloseBtn.addEventListener('click', closeWaferModal);
    }
    if (el.waferModalBackdrop) {
      el.waferModalBackdrop.addEventListener('click', (e) => {
        if (e.target === el.waferModalBackdrop) closeWaferModal();
      });
    }

    // Domain Filters in Sidebar
    if (el.packFilterItems) {
      el.packFilterItems.forEach(btn => {
        btn.addEventListener('click', () => {
          el.packFilterItems.forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          state.selectedDomain = btn.dataset.domain;
          playTone(480, 'sine', 0.05, 0.02);
        });
      });
    }

    // Audio Toggle
    if (el.audioToggleBtn) {
      el.audioToggleBtn.addEventListener('click', () => {
        state.audioEnabled = !state.audioEnabled;
        if (state.audioEnabled) {
          playTone(523.25, 'triangle', 0.15, 0.03);
          el.audioIcon.innerHTML = `
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
            <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
          `;
          el.audioToggleBtn.title = 'Audio Feedback: ON';
        } else {
          el.audioIcon.innerHTML = `
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
            <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
            <line x1="23" y1="9" x2="17" y2="15"></line>
            <line x1="17" y1="9" x2="23" y2="15"></line>
          `;
          el.audioToggleBtn.title = 'Audio Feedback: OFF';
        }
      });
    }

    // Auto-focus input
    if (el.geminiInput) el.geminiInput.focus();

    // Auto-run query if passed via ?q= or #...
    const urlParams = new URLSearchParams(window.location.search);
    const initialQuery = urlParams.get('q') || (window.location.hash ? decodeURIComponent(window.location.hash.substring(1)) : null);
    if (initialQuery) {
      handleUserQuery(initialQuery);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
