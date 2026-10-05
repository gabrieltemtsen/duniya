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
    starterCards: document.querySelectorAll('.starter-card')
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

  function appendGeminiResponse(query, article) {
    if (!el.messagesContainer || !article) return;

    // Build structured metrics rows for the accordion
    const metricsRows = Object.entries(article.structuredMetrics || {}).map(([key, val]) => `
      <tr>
        <td style="font-weight: 600; font-family: var(--font-mono);">${escapeHtml(key)}</td>
        <td>${escapeHtml(val)}</td>
      </tr>
    `).join('');

    // Primary Citations
    const citationsList = (article.primaryCitations || []).map(c => `
      <li style="margin-bottom: 0.35rem;">${escapeHtml(c)}</li>
    `).join('');

    // Generate Follow-up chips
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

      <div class="assistant-card-body">
        <div class="response-header">
          <span class="response-domain-badge">${escapeHtml(article.domain)} • ${escapeHtml(article.subcategory || 'Review')}</span>
          <span class="response-telemetry-tag">● 4/64 MoE Active • UFS 4.0 MMap</span>
        </div>

        <h2 class="response-title">${escapeHtml(article.title)}</h2>

        <!-- Scannable Clean Summary -->
        <div class="response-executive-summary">
          ${escapeHtml(article.summary)}
        </div>

        <!-- Deep Derivation Accordion -->
        ${article.deepExplanation ? `
        <details class="gemini-accordion">
          <summary>📖 Technical Explanation & Mechanical Derivation</summary>
          <div class="accordion-content">
            ${article.deepExplanation.split('\n\n').map(p => `<p style="margin-bottom: 0.6rem;">${escapeHtml(p.trim())}</p>`).join('')}
          </div>
        </details>
        ` : ''}

        <!-- Governing Equations Accordion -->
        ${article.firstPrinciplesMathOrMechanism ? `
        <details class="gemini-accordion">
          <summary>📐 First-Principles Governing Equations</summary>
          <div class="accordion-content">
            <div class="math-formula-box">
              <code>${escapeHtml(article.firstPrinciplesMathOrMechanism)}</code>
            </div>
            ${article.tradeOffsAndEdgeCases ? `<p style="font-size: 0.84rem; color: var(--silver-400);"><strong>Boundary Conditions & Edge Cases:</strong> ${escapeHtml(article.tradeOffsAndEdgeCases)}</p>` : ''}
          </div>
        </details>
        ` : ''}

        <!-- Comparative Specification Matrix Accordion -->
        ${metricsRows ? `
        <details class="gemini-accordion">
          <summary>📊 Comparative Specification Matrix</summary>
          <div class="accordion-content" style="padding: 0.5rem 0.75rem;">
            <table class="mini-matrix-table">
              <thead>
                <tr>
                  <th style="width: 35%;">Dimension</th>
                  <th>Specification & Complexity</th>
                </tr>
              </thead>
              <tbody>
                ${metricsRows}
              </tbody>
            </table>
          </div>
        </details>
        ` : ''}

        <!-- 1B Model Failure Autopsy Accordion -->
        ${article.oneBModelFailureMode ? `
        <details class="gemini-accordion">
          <summary>⚠️ Why 1B Dense Models Fail on This Inquiry</summary>
          <div class="accordion-content">
            <div class="failure-autopsy-box">
              <strong>1B Failure Autopsy:</strong> ${escapeHtml(article.oneBModelFailureMode)}
            </div>
          </div>
        </details>
        ` : ''}

        <!-- Peer-Reviewed Citations Accordion -->
        ${citationsList ? `
        <details class="gemini-accordion">
          <summary>📚 Peer-Reviewed Citations & References</summary>
          <div class="accordion-content">
            <ul style="padding-left: 1.25rem; font-size: 0.85rem;">
              ${citationsList}
            </ul>
          </div>
        </details>
        ` : ''}

        <!-- Follow-up Queries -->
        ${followUpsHtml ? `
        <div style="margin-top: 0.25rem;">
          <div style="font-size: 0.74rem; font-family: var(--font-mono); color: var(--silver-500); margin-bottom: 0.4rem;">Suggested Inquiries:</div>
          <div class="followups-group">
            ${followUpsHtml}
          </div>
        </div>
        ` : ''}
      </div>
    `;

    el.messagesContainer.appendChild(row);

    // Attach click listeners to follow-up suggestion chips
    row.querySelectorAll('.followup-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        handleUserQuery(chip.dataset.query);
      });
    });

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

    // Starter Prompt Cards
    if (el.starterCards) {
      el.starterCards.forEach(card => {
        card.addEventListener('click', () => {
          const prompt = card.dataset.prompt;
          handleUserQuery(prompt);
        });
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
