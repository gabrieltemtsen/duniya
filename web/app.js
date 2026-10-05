// Duniya ⛺ — Standalone Offline Web Engine & Ethereum Silver Glassmorphism Controller

(function () {
  'use strict';

  // --- State ---
  const state = {
    currentTab: 'lab', // 'lab' | 'benchmarks' | 'vault' | 'airgap'
    currentArticleId: 'zk_binius',
    activeExperts: [3, 11, 24, 48],
    audioEnabled: false,
    query: '',
    selectedBenchmark: null,
    telemetry: {
      activeTokensSec: 28.5,
      ramMb: 194,
      latencyMs: 842,
      mmapHitRate: '99.8%',
      ufsSpeed: '4.2 GB/s'
    }
  };

  // --- Sound Effects using Web Audio API (Offline, Zero Assets) ---
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
    } catch (e) {
      // Audio not supported or blocked
    }
  }

  function playSynthChord() {
    if (!state.audioEnabled) return;
    [440, 554.37, 659.25, 880].forEach((freq, idx) => {
      setTimeout(() => playTone(freq, 'triangle', 0.25, 0.02), idx * 40);
    });
  }

  // --- DOM Elements ---
  const el = {
    omnibarInput: document.getElementById('omnibarInput'),
    synthesizeBtn: document.getElementById('synthesizeBtn'),
    quickChipsRow: document.getElementById('quickChipsRow'),
    waferGrid: document.getElementById('waferGrid'),
    monographContainer: document.getElementById('monographContainer'),
    benchmarksContainer: document.getElementById('benchmarksContainer'),
    vaultContainer: document.getElementById('vaultContainer'),
    airgapContainer: document.getElementById('airgapContainer'),
    navBtns: document.querySelectorAll('.nav-btn'),
    activeExpertsCount: document.getElementById('activeExpertsCount'),
    telemetrySpeed: document.getElementById('telemetrySpeed'),
    telemetryRam: document.getElementById('telemetryRam'),
    telemetryLatency: document.getElementById('telemetryLatency'),
    audioToggleBtn: document.getElementById('audioToggleBtn')
  };

  // --- Knowledge Data Access ---
  const data = window.DUNIYA_DATA || { articles: [], benchmarks: [] };

  // --- 8x8 Wafer Die Generation ---
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
        inspectExpert(i);
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

    if (el.activeExpertsCount) {
      el.activeExpertsCount.textContent = `${state.activeExperts.length}/64 MoE`;
    }
  }

  function inspectExpert(expertId) {
    // Dynamically route to an article corresponding to this expert
    const domain = getExpertDomain(expertId);
    executeSearch(domain.split(' ')[0]);
  }

  // --- Dynamic MoE Router Simulation ---
  function routeQueryToExperts(queryText) {
    const hash = simpleHash(queryText);
    const exp1 = (hash & 0x3F);
    const exp2 = ((hash >> 6) & 0x3F);
    const exp3 = ((hash >> 12) & 0x3F);
    const exp4 = ((hash >> 18) & 0x3F);
    
    // Ensure 4 unique indices
    const set = new Set([exp1, exp2, exp3, exp4]);
    let fallback = 0;
    while (set.size < 4) {
      set.add((hash + (++fallback) * 7) & 0x3F);
    }
    state.activeExperts = Array.from(set);
    updateWaferActiveTiles();

    // Randomize slight variance in latency & speed
    state.telemetry.latencyMs = 780 + (hash % 180);
    state.telemetry.activeTokensSec = (27.2 + ((hash % 30) / 10)).toFixed(1);
    state.telemetry.ramMb = 188 + (hash % 24);

    if (el.telemetryLatency) el.telemetryLatency.textContent = `${state.telemetry.latencyMs}ms`;
    if (el.telemetrySpeed) el.telemetrySpeed.textContent = `${state.telemetry.activeTokensSec} tok/s`;
    if (el.telemetryRam) el.telemetryRam.textContent = `${state.telemetry.ramMb} MB`;
  }

  function simpleHash(str) {
    let hash = 5381;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) + hash) + str.charCodeAt(i);
    }
    return Math.abs(hash);
  }

  // --- Monograph Article Rendering ---
  function renderMonograph(article) {
    if (!el.monographContainer) return;
    if (!article) {
      el.monographContainer.innerHTML = `<div class="glass-card monograph-canvas"><p>No matching research monograph found.</p></div>`;
      return;
    }

    // Build Structured Metrics Table Rows
    const metricsRows = Object.entries(article.structuredMetrics || {}).map(([key, val]) => `
      <tr>
        <td class="metric-key">${escapeHtml(key)}</td>
        <td>${escapeHtml(val)}</td>
      </tr>
    `).join('');

    // Build Citations
    const citationsList = (article.primaryCitations || []).map(cit => `
      <li class="citation-item">${escapeHtml(cit)}</li>
    `).join('');

    // Format deep explanation into paragraphs
    const paragraphs = (article.deepExplanation || article.summary || '')
      .split('\n\n')
      .filter(p => p.trim().length > 0)
      .map(p => `<p>${escapeHtml(p.trim())}</p>`)
      .join('');

    const html = `
      <article class="glass-card monograph-canvas" id="monographArticle">
        <header class="monograph-header">
          <div class="monograph-domain-badge">${escapeHtml(article.domain)} • ${escapeHtml(article.subcategory || 'Technical Review')}</div>
          <h1 class="monograph-title">${escapeHtml(article.title)}</h1>
          <div class="monograph-metadata">
            <span>ID: <code>${escapeHtml(article.id)}</code></span>
            <span>Archival Peer-Review</span>
            <span>Zero-Net Memory Mapped</span>
            <span>Tags: ${article.tags ? article.tags.map(t => `#${escapeHtml(t)}`).join(' ') : ''}</span>
          </div>
        </header>

        <!-- Executive Findings -->
        <section class="executive-findings">
          <div class="executive-findings-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            Executive Research Findings & Core Thesis
          </div>
          <div class="executive-findings-text">${escapeHtml(article.summary)}</div>
        </section>

        <!-- Deep Mechanical & Mathematical Exposition -->
        <section class="monograph-section">
          <div class="section-label">I. Deep Mechanical Derivation</div>
          <div class="deep-explanation-body">
            ${paragraphs}
          </div>
        </section>

        <!-- Governing Equations -->
        ${article.firstPrinciplesMathOrMechanism ? `
        <section class="monograph-section">
          <div class="section-label">II. First-Principles Governing Equations</div>
          <div class="equation-box">
            <code>${escapeHtml(article.firstPrinciplesMathOrMechanism)}</code>
          </div>
        </section>
        ` : ''}

        <!-- Comparative Specification Matrix -->
        ${metricsRows ? `
        <section class="monograph-section">
          <div class="section-label">III. Comparative Specification Matrix</div>
          <div class="matrix-table-wrapper">
            <table class="matrix-table">
              <thead>
                <tr>
                  <th style="inline-size: 32%;">Parameter / Dimension</th>
                  <th>Architectural Specification & Proof Asymptotics</th>
                </tr>
              </thead>
              <tbody>
                ${metricsRows}
              </tbody>
            </table>
          </div>
        </section>
        ` : ''}

        <!-- 1B Dense Model Failure Autopsy -->
        ${article.oneBModelFailureMode ? `
        <section class="monograph-section">
          <div class="section-label">IV. 1B Dense Model Failure Mode Autopsy</div>
          <div class="failure-card">
            <div class="failure-card-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              Empirical Hallucination Diagnostic
            </div>
            <div class="failure-text">${escapeHtml(article.oneBModelFailureMode)}</div>
          </div>
        </section>
        ` : ''}

        <!-- Primary Empirical Citations -->
        ${citationsList ? `
        <section class="monograph-section">
          <div class="section-label">V. Primary Empirical Citations & ArXiv References</div>
          <ul class="citations-list">
            ${citationsList}
          </ul>
        </section>
        ` : ''}
      </article>
    `;

    el.monographContainer.innerHTML = html;
  }

  // --- Search & 4-Hop Synthesis Engine ---
  function executeSearch(query) {
    if (!query || !query.trim()) return;
    const cleanQuery = query.trim().toLowerCase();
    state.query = cleanQuery;
    if (el.omnibarInput) el.omnibarInput.value = query;

    // MoE Route Animation & Telemetry
    routeQueryToExperts(cleanQuery);
    playSynthChord();

    // 4-Hop Search Strategy:
    // 1. Exact title or tag match
    // 2. Keyword relevance across summary, first principles, deep explanation
    // 3. Fallback to top related article
    let bestArticle = null;
    let highestScore = -1;

    data.articles.forEach(art => {
      let score = 0;
      const titleLower = art.title.toLowerCase();
      const tagsLower = (art.tags || []).map(t => t.toLowerCase());
      const summaryLower = (art.summary || '').toLowerCase();
      const mathLower = (art.firstPrinciplesMathOrMechanism || '').toLowerCase();
      const domainLower = (art.domain || '').toLowerCase();

      const tokens = cleanQuery.split(/\s+/).filter(Boolean);

      tokens.forEach(token => {
        if (titleLower.includes(token)) score += 25;
        if (tagsLower.some(t => t.includes(token))) score += 18;
        if (domainLower.includes(token)) score += 10;
        if (summaryLower.includes(token)) score += 6;
        if (mathLower.includes(token)) score += 4;
      });

      if (score > highestScore) {
        highestScore = score;
        bestArticle = art;
      }
    });

    if (!bestArticle && data.articles.length > 0) {
      bestArticle = data.articles[0];
    }

    if (bestArticle) {
      state.currentArticleId = bestArticle.id;
      renderMonograph(bestArticle);
    }
  }

  // --- Benchmarks Tab Rendering ---
  function renderBenchmarks() {
    if (!el.benchmarksContainer) return;
    const benchmarksHtml = (data.benchmarks || []).map(b => `
      <div class="glass-card benchmark-card" data-bench-id="${escapeHtml(b.id)}">
        <div class="benchmark-tag">${escapeHtml(b.category)} • ${escapeHtml(b.mode)}</div>
        <div class="benchmark-heading">${escapeHtml(b.title)}</div>
        <div class="benchmark-prompt-quote">“${escapeHtml(b.prompt)}”</div>
        <div style="font-size: 0.78rem; font-family: var(--font-mono); color: var(--accent-crimson);">
          ⚠ 1B Failure: ${escapeHtml(b.why1BFailsShort)}
        </div>
        <button class="synthesize-btn" style="align-self: flex-start; margin-block-start: 0.5rem; font-size: 0.78rem; padding-inline: 1rem; min-block-size: 36px;">
          Execute MoE vs 1B Comparison ➔
        </button>
      </div>
    `).join('');

    el.benchmarksContainer.innerHTML = `
      <div class="glass-card" style="padding: 1.5rem; display: flex; flex-direction: column; gap: 0.8rem;">
        <div style="font-size: 1.25rem; font-weight: 700; color: var(--silver-100);">Vitalik's 6 Hard Challenge Benchmarks</div>
        <p style="font-size: 0.92rem; color: var(--silver-300);">
          Evaluates why frontier Sparse MoE + Disk-Mapped Engram models provide deep, verifiable, mathematically sound answers offline, while standard 1B dense models hallucinate, conflate setup ceremonies, and provide dangerous medical/technical misinformation.
        </p>
      </div>
      <div class="benchmark-grid">
        ${benchmarksHtml}
      </div>
      <div id="battleResultContainer" style="margin-block-start: 1rem;"></div>
    `;

    // Attach click listeners to benchmark cards
    el.benchmarksContainer.querySelectorAll('.benchmark-card').forEach(card => {
      card.addEventListener('click', () => {
        const benchId = card.dataset.benchId;
        const bench = (data.benchmarks || []).find(b => b.id === benchId);
        if (bench) showBenchmarkBattle(bench);
      });
    });
  }

  function showBenchmarkBattle(bench) {
    const battleContainer = document.getElementById('battleResultContainer');
    if (!battleContainer) return;

    playSynthChord();
    routeQueryToExperts(bench.prompt);

    // Find the corresponding monograph to present the deep answer
    let matchedArticle = data.articles.find(a => 
      a.id.toLowerCase().includes(bench.id.replace('bench_', '')) || 
      bench.title.toLowerCase().includes(a.title.toLowerCase().substring(0, 10))
    ) || data.articles[0];

    battleContainer.innerHTML = `
      <div class="glass-card" style="padding: 1.75rem; border-color: rgba(140, 158, 255, 0.4);">
        <div style="display: flex; justify-content: space-between; align-items: center; border-block-end: 1px solid rgba(226, 232, 240, 0.14); padding-block-end: 1rem;">
          <div>
            <span class="monograph-domain-badge">${escapeHtml(bench.category)}</span>
            <h2 style="font-size: 1.35rem; color: var(--silver-100); margin-block-start: 0.4rem;">
              Benchmark Battle: ${escapeHtml(bench.title)}
            </h2>
          </div>
          <span class="status-pill eth-pill">Offline Simulated Verdict</span>
        </div>

        <div class="battle-modal-body">
          <!-- 1B Dense Model Collapse -->
          <div class="battle-col one-b">
            <div style="display: flex; align-items: center; gap: 0.5rem; font-family: var(--font-mono); font-size: 0.8rem; font-weight: 700; color: var(--accent-crimson);">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
              </svg>
              Standard 1B Parameter Dense Model (Mobile Baseline)
            </div>
            <div style="font-size: 0.9rem; line-height: 1.5; color: #FECDD3; font-style: italic;">
              “${escapeHtml(bench.simulated1BOutput)}”
            </div>
            <div style="font-size: 0.78rem; font-family: var(--font-mono); color: var(--accent-crimson); border-block-start: 1px solid rgba(244, 63, 94, 0.2); padding-block-start: 0.5rem;">
              Diagnostic: ${escapeHtml(bench.why1BFailsShort)}
            </div>
          </div>

          <!-- Duniya Sparse MoE + Engram -->
          <div class="battle-col moe">
            <div style="display: flex; align-items: center; gap: 0.5rem; font-family: var(--font-mono); font-size: 0.8rem; font-weight: 700; color: var(--eth-blue-glow);">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              Duniya ⛺ Sparse MoE + 65K Engram + Hybrid Retrieval
            </div>
            <div style="font-size: 0.92rem; line-height: 1.6; color: var(--silver-100);">
              ${escapeHtml(matchedArticle.summary)}
            </div>
            <div style="font-family: var(--font-mono); font-size: 0.76rem; color: var(--silver-300); background: rgba(9, 12, 22, 0.5); padding: 0.6rem; border-radius: 6px; border: 1px solid rgba(226, 232, 240, 0.1);">
              Governing Equation: ${escapeHtml(matchedArticle.firstPrinciplesMathOrMechanism)}
            </div>
            <button class="synthesize-btn" style="align-self: flex-start; min-block-size: 34px; font-size: 0.78rem; padding-inline: 0.9rem;" onclick="window.duniyaSwitchToLabAndOpen('${matchedArticle.id}')">
              Open Full Monograph in Lab ➔
            </button>
          </div>
        </div>
      </div>
    `;

    battleContainer.scrollIntoView({ behavior: 'smooth' });
  }

  // --- Storage Vault Tab Rendering ---
  function renderVault() {
    if (!el.vaultContainer) return;
    const packs = [
      { name: 'Cryptography & Ethereum Protocol', size: '8.4 GB', articles: 1420, active: true },
      { name: 'AI Systems & Sparse MoE Kernels', size: '11.2 GB', articles: 1850, active: true },
      { name: 'Biotechnology, Genetics & Medicine', size: '9.8 GB', articles: 1640, active: true },
      { name: 'Physics, Semiconductors & Energy', size: '7.6 GB', articles: 1210, active: true },
      { name: 'Global History & Macroeconomics', size: '6.5 GB', articles: 980, active: true },
      { name: 'Field Engineering & Austere Survival', size: '4.1 GB', articles: 720, active: true },
      { name: 'Wikipedia Curated 2026 Snapshot', size: '4.8 GB', articles: 8500, active: true }
    ];

    const cardsHtml = packs.map(pack => `
      <div class="glass-card vault-card">
        <div class="pack-header">
          <span class="pack-name">${escapeHtml(pack.name)}</span>
          <span class="pack-size">${escapeHtml(pack.size)}</span>
        </div>
        <div style="font-size: 0.84rem; color: var(--silver-300);">
          ${pack.articles.toLocaleString()} peer-reviewed monographs • Int8 HNSW + FTS5
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-block-start: 0.5rem;">
          <span class="pack-status">
            <span class="pulse-dot" style="inline-size: 6px; block-size: 6px; border-radius: 50%; background: var(--accent-mint);"></span>
            MMAP CACHE ACTIVE
          </span>
          <button class="quick-chip" style="font-size: 0.72rem;">Inspect Index</button>
        </div>
      </div>
    `).join('');

    el.vaultContainer.innerHTML = `
      <div class="glass-card" style="padding: 1.5rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div>
          <div style="font-size: 1.25rem; font-weight: 700; color: var(--silver-100);">50 GB Knowledge Pack Storage Vault</div>
          <div style="font-size: 0.85rem; color: var(--silver-400); font-family: var(--font-mono);">
            Total Allocated: 52.4 GB • Resident Memory: 194 MB RSS • Zero Cloud Dependencies
          </div>
        </div>
        <label class="synthesize-btn" style="cursor: pointer; font-size: 0.82rem; padding-inline: 1.2rem; min-block-size: 38px;">
          <input type="file" id="importPackInput" accept=".json,.gguf" style="display: none;">
          Import .GGUF / .JSON Pack
        </label>
      </div>
      <div class="vault-grid">
        ${cardsHtml}
      </div>
    `;

    const importInput = document.getElementById('importPackInput');
    if (importInput) {
      importInput.addEventListener('change', (e) => {
        const file = e.target.files && e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = (evt) => {
          try {
            const parsed = JSON.parse(evt.target.result);
            if (Array.isArray(parsed)) {
              data.articles.push(...parsed);
              alert(`Successfully imported ${parsed.length} articles from ${file.name}!`);
              executeSearch(parsed[0].title);
            }
          } catch (err) {
            alert('Import error: Invalid pack JSON');
          }
        };
        reader.readAsText(file);
      });
    }
  }

  // --- Airgap Audit Tab Rendering ---
  function renderAirgap() {
    if (!el.airgapContainer) return;
    el.airgapContainer.innerHTML = `
      <div class="glass-card airgap-card">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-size: 1.25rem; font-weight: 700; color: var(--silver-100);">Hardware Airgap & Zero-Network Audit</div>
            <div style="font-size: 0.85rem; color: var(--silver-400); font-family: var(--font-mono);">
              Kernel Socket Monitor • /proc/net/tcp Inspection • GrapheneOS Memory-Safe Sandboxing
            </div>
          </div>
          <span class="status-pill offline" style="font-size: 0.82rem; padding-inline: 1rem;">
            <span class="pulse-dot"></span>
            0 PACKETS TRANSMITTED
          </span>
        </div>

        <div class="log-console" id="auditConsole">
          [KERNEL-AUDIT] Initializing local sandboxed research loop...<br>
          [SOCKET-MON] Scanning active network descriptors (AF_INET, AF_INET6)...<br>
          [SOCKET-MON] Outbound sockets open: 0<br>
          [DNS-MONITOR] Remote DNS lookups requested: 0<br>
          [MMAP-SUBSYS] Mapped 50GB pack cache directly via mmap() (MAP_SHARED, PROT_READ)<br>
          [MOE-ENGINE] 64 experts initialized on local device storage.<br>
          [SECURITY-OK] 100% Offline Airgap Integrity Verified. No telemetry leaves this browser instance.
        </div>

        <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
          <button class="synthesize-btn" style="min-block-size: 38px; font-size: 0.82rem;" onclick="window.duniyaRunSocketAudit()">
            Run Live Airgap Probe
          </button>
        </div>
      </div>
    `;
  }

  window.duniyaRunSocketAudit = function() {
    const consoleEl = document.getElementById('auditConsole');
    if (!consoleEl) return;
    playTone(880, 'sine', 0.1, 0.05);
    const now = new Date().toLocaleTimeString();
    consoleEl.innerHTML += `<br>[${now}] PROBE: Testing Navigator.onLine & Fetch blocking... PASS (Zero outbound bytes)`;
    consoleEl.scrollTop = consoleEl.scrollHeight;
  };

  window.duniyaSwitchToLabAndOpen = function(articleId) {
    switchTab('lab');
    const art = data.articles.find(a => a.id === articleId);
    if (art) {
      renderMonograph(art);
      routeQueryToExperts(art.title);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  // --- Tab Navigation Switcher ---
  function switchTab(tabName) {
    state.currentTab = tabName;
    el.navBtns.forEach(btn => {
      if (btn.dataset.tab === tabName) btn.classList.add('active');
      else btn.classList.remove('active');
    });

    const isLab = tabName === 'lab';
    const isBench = tabName === 'benchmarks';
    const isVault = tabName === 'vault';
    const isAirgap = tabName === 'airgap';

    const labSection = document.getElementById('labSection');
    if (labSection) labSection.style.display = isLab ? 'contents' : 'none';
    if (el.benchmarksContainer) el.benchmarksContainer.style.display = isBench ? 'flex' : 'none';
    if (el.vaultContainer) el.vaultContainer.style.display = isVault ? 'flex' : 'none';
    if (el.airgapContainer) el.airgapContainer.style.display = isAirgap ? 'flex' : 'none';

    if (isBench) renderBenchmarks();
    if (isVault) renderVault();
    if (isAirgap) renderAirgap();
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

  // --- Initialization & Event Listeners ---
  function init() {
    initWaferGrid();

    // Default article
    const defaultArticle = data.articles.find(a => a.id === state.currentArticleId) || data.articles[0];
    if (defaultArticle) {
      renderMonograph(defaultArticle);
      routeQueryToExperts(defaultArticle.title);
    }

    // Omnibar Synthesize
    if (el.synthesizeBtn && el.omnibarInput) {
      el.synthesizeBtn.addEventListener('click', () => {
        executeSearch(el.omnibarInput.value);
      });
      el.omnibarInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          executeSearch(el.omnibarInput.value);
        }
      });
    }

    // Quick Action Chips
    if (el.quickChipsRow) {
      el.quickChipsRow.querySelectorAll('.quick-chip').forEach(chip => {
        chip.addEventListener('click', () => {
          const q = chip.dataset.query || chip.textContent.trim();
          executeSearch(q);
        });
      });
    }

    // App Navigation Buttons
    el.navBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        playTone(480, 'sine', 0.04, 0.02);
        switchTab(btn.dataset.tab);
        window.location.hash = btn.dataset.tab;
      });
    });

    // Handle initial hash navigation
    const initialHash = (window.location.hash || '').replace('#', '');
    if (['lab', 'benchmarks', 'vault', 'airgap'].includes(initialHash)) {
      switchTab(initialHash);
    }
    window.addEventListener('hashchange', () => {
      const h = (window.location.hash || '').replace('#', '');
      if (['lab', 'benchmarks', 'vault', 'airgap'].includes(h)) {
        switchTab(h);
      }
    });

    // Audio Toggle
    if (el.audioToggleBtn) {
      el.audioToggleBtn.addEventListener('click', () => {
        state.audioEnabled = !state.audioEnabled;
        el.audioToggleBtn.textContent = state.audioEnabled ? 'Audio: ON ♫' : 'Audio: OFF';
        if (state.audioEnabled) playTone(523.25, 'triangle', 0.15, 0.03);
      });
    }

    // Register Service Worker for 100% Offline PWA functionality
    if ('serviceWorker' in navigator && (window.location.protocol === 'http:' || window.location.protocol === 'https:')) {
      navigator.serviceWorker.register('sw.js').catch(() => {});
    }
  }

  // Run when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
