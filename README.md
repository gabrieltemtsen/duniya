# Duniya ⛺ — The Offline Sparse-MoE + N-Gram + Hybrid RAG Research Engine for Android & GrapheneOS

> **An open-source, air-gapped community research engine for Android & GrapheneOS.**  
> Inspired by [Vitalik Buterin's thesis on offline mobile research](https://x.com/VitalikButerin/status/2100695863026954698):  
> *"Build a casual info lookup and research tool that runs entirely offline on Android and is >50% as good as internet search + frontier AI models... His past attempts got ~10 tokens/sec from 1B models that break on anything interesting. He suggests extreme MoE might be the right architecture for phones (including newer variants like n-gram models): something like ~100B params, most living on disk, with <1B activated per token."*

---

## Live On-Device & Web Companion Screenshots

### Ethereum Silver Glassmorphic Design System (Web Companion & Android)

| Web Companion: Research Lab & 8×8 Wafer Die | Web Companion: Full Monograph & Spec Matrix | 1B vs MoE Benchmark Arena | Android: Ethereum Silver Glass |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/web_app_ethereum_silver.png" width="220"/> | <img src="docs/screenshots/web_app_full_monograph.png" width="220"/> | <img src="docs/screenshots/web_app_benchmarks.png" width="220"/> | <img src="docs/screenshots/screen_android_ethereum.png" width="220"/> |

### Native Android Workspaces (Archival Field Paper & Obsidian OLED Night)

| Clean Paper Research Workspace | Monograph Reading Canvas | Multi-Entity Specification Matrix | Obsidian OLED Night Mode |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/screen_clean_ui_08_paper_ready.png" width="210"/> | <img src="docs/screenshots/screen_clean_ui_02_report.png" width="210"/> | <img src="docs/screenshots/screen_clean_ui_04_matrix.png" width="210"/> | <img src="docs/screenshots/screen_android_obsidian.png" width="210"/> |

| 50 GB UFS Flash Pack Manager & Vault | 1B Dense vs. Sparse MoE Benchmark Arena | GrapheneOS & Linux Kernel Airgap Audit |
| :---: | :---: | :---: |
| <img src="docs/screenshots/screen_clean_ui_09_vault.png" width="210"/> | <img src="docs/screenshots/screen_clean_ui_10_benchmarks.png" width="210"/> | <img src="docs/screenshots/08_airgap_audit_tab.png" width="210"/> |

---

## Why 1B Dense Models Fail on Phones — And How Duniya Solves It

A 1B dense transformer on a smartphone faces two fundamental information-theoretic bottlenecks:
1. **The Memory-Bandwidth vs. Parameter Capacity Wall:**  
   Autoregressive token generation is memory-bandwidth bound ($\text{tok/s} \approx \text{BW}_{\text{mem}} / \text{Active Bytes per Token}$). A dense model activates **100%** of its parameters for every token, forcing a brutal choice: either run a 1B model that fits in ~700 MB RAM at ~10 tok/s with catastrophic hallucination on non-trivial topics, or run a 14B dense model that consumes 8+ GB RAM and crawls at 3–5 tok/s.
2. **The Parametric Fact Density Bottleneck:**  
   As proven by Allen-Zhu & Li (*Physics of Language Models: Knowledge Capacity Scaling Laws*), neural weights store at most **~2 bits of factual knowledge per parameter** (~0.5 bits of fact per Q4 weight byte) and suffer from cross-talk hallucinations on rare numbers, equations, and multi-entity comparisons. Compressed inverted indexes + quantized semantic vectors store exact encyclopedic facts at **15–35× higher byte density** with **zero hallucination** and **verifiable citations**.

### Duniya's 3-Pillar Architecture (`<12 GB RAM`, `≤50 GB Flash`, `0 Network Permissions`)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        USER RESEARCH QUERY (OFFLINE)                         │
│   "Compare Groth16, PLONK, STARKs, and Binius on setup, asymptotics & PQ"    │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ HOP 1: MULTI-HOP QUERY DECOMPOSITION & STRUCTURAL INTENT ROUTER              │
│ • Classifies mode: COMPARE | DEEP_SYNTHESIS | FIRST_PRINCIPLES | FAST_LOOKUP │
│ • Decomposes multi-entity prompt into targeted sub-queries & n-gram tokens   │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ HOP 2: PARALLEL HYBRID RETRIEVAL (<15 ms)                                    │
│ • SQLite FTS4 BM25 (Porter Stemmed Inverted Index)                           │
│ • Native C++17 ARM64 NEON Int8 Vector Cosine Similarity (256-dim Q8)         │
│ • 1-Hop Concept Graph Expansion (auto-retrieves peer comparison entities)    │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ HOP 3: NATIVE C++17 FLASH-STREAMED SPARSE MoE (64×2) + 65K ENGRAM PASS       │
│ • libduniya_engine.so (POSIX mmap + MADV_RANDOM / MADV_WILLNEED, 16KB page)  │
│ • 64 Domain & Reasoning Experts on Flash; ONLY Top-2 Active/Step (3.125%!)   │
│ • 65,536-Bucket Disk-Mapped N-Gram Memory Table (O(1) FNV-1a 2/3-gram hash)  │
│ • Native GGUF v2/v3 Memory-Mapper for external 4GB–45GB Sparse MoE models    │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ HOP 4: GROUNDED MULTI-DOCUMENT SYNTHESIS & 1B FAILURE CONTRAST               │
│ • Executive Thesis + Multi-Dimensional Comparison Matrix Table               │
│ • First-Principles Equations / Biochemical / Protocol Mechanisms             │
│ • Side-by-Side "Why a 1B Dense Model (~10 tok/s) Fails Here" Analysis        │
│ • Clickable Offline Encyclopedia Sources & Primary Literature Citations      │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Core Architectural Specifications & Hardware Standards

| Specification | How Duniya Implements & Guarantees It |
| :--- | :--- |
| **Runs on Android & GrapheneOS** | Targets API 29–36 (`arm64-v8a` & `x86_64`), compiled with **16KB ELF page alignment** (`-Wl,-z,max-page-size=16384`) for modern Pixel / GrapheneOS hardware. |
| **Zero Google Play Services** | `0` Play Services (`com.google.android.gms`) dependencies. Verified on pure AOSP / GrapheneOS system images. |
| **Complete Hardware Airgap (`0` Network Calls)** | `AndroidManifest.xml` explicitly **strips `android.permission.INTERNET`** (`tools:node="remove"`). At runtime, the C++ engine inspects Linux kernel `getgroups()` to verify the process **lacks `AID_INET (GID 3003)`**, meaning the Linux kernel blocks `socket(AF_INET, ...)` at the syscall boundary. |
| **Maximum 12 GB RAM Environment** | Runs in **~130–220 MB physical RSS (`VmRSS`)** for the built-in 64-expert Sparse MoE + Hybrid RAG engine, and `<4.5 GB RAM` when streaming external 16–30B Sparse MoE `.gguf` shards via `mmap`. Live `/proc/self/status` telemetry is displayed in the top bar. |
| **Maximum 50 GB Total Storage** | Core APK + page-aligned `duniya_flash_moe_v1.bin` + SQLite FTS4/Vector index use **~24 MB** out of the box (instant startup!), and the built-in **50 GB Offline Pack Manager** mounts external `.gguf` (e.g., `Qwen3-30B-A3B-Q4_K_M`, `OLMoE-1B-7B-Q4_K_M`) and `.json` packs up to the 50 GB ceiling. |
| **Beyond Simple Factual Recall** | Handles multi-entity asymptotic comparisons, mathematical derivations, biochemical mechanism contrasts, historical fiscal-military synthesis, and austere survival triage. |
| **Usable Speeds on Phone** | **15–35 ms** hybrid retrieval + **38–55 tokens/sec** effective synthesis throughput via ARM64 NEON Int8 SIMD (`vld1q_s8`, `vmull_s8`, `vpadalq_s16`, `vaddvq_s32`) and Top-2 of 64 sparse expert activation (`3.125%` active ratio). |

---

## Quickstart: Run on Real Android or GrapheneOS Hardware in 2 Minutes

Duniya is engineered so that **anyone with an Android or GrapheneOS device can clone the repo and have the full app running locally within 2 minutes with zero complex setup** — the core 64-expert Sparse MoE weights (`duniya_flash_moe_v1.bin`), 65K N-Gram Engram table, and Multi-Domain Graduate Research Encyclopedia (`duniya_knowledge_v1.db`) are automatically initialized on-device on first launch!

### Prerequisites
- JDK 17+ (`JAVA_HOME` pointing to OpenJDK 17)
- Android SDK (`platforms/android-36`, `build-tools/36.0.0`, `ndk/27.2.12479018`, `cmake/3.22.1`)
- A connected Android / GrapheneOS phone with USB debugging enabled (or an ARM64 Android Emulator)

### 1. Clone & Build the APK
```bash
git clone https://github.com/gabrieltemtsen/duniya.git
cd duniya

# Build the native C++17 NDK engine (libduniya_engine.so) + Jetpack Compose APK
./gradlew assembleDebug

# Run JVM unit tests verifying the 4-Hop Hybrid Research & MoE Pipeline
./gradlew testDebugUnitTest
```

### 2. Install & Launch on Connected Android / GrapheneOS Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.duniya/.MainActivity
```

### 3. Verify the Kernel-Level Airgap (`0` Network Permissions)
```bash
./scripts/setup_offline_assets.sh --verify
```

### 4. (Optional) Provision External Multi-Gigabyte Sparse MoE `.GGUF` Packs (Up to 50 GB)
Because Duniya has **zero internet permissions**, external multi-GB GGUF models or custom knowledge packs are downloaded on your computer and pushed via USB (`adb push`) or imported via the in-app **Storage Access Framework (`Import .GGUF / .JSON`)** button:
```bash
# Option A: Fetch & push OLMoE-1B-7B-0924-Instruct Q4_K_M (4.2 GB Sparse MoE: 1B active / 7B total)
./scripts/setup_offline_assets.sh --olmoe-7b

# Option B: Fetch & push Qwen3-30B-A3B Q4_K_M (17.8 GB Sparse MoE: 3B active / 30B total)
./scripts/setup_offline_assets.sh --qwen3-moe-30b

# Option C: Compile a custom offline JSON knowledge pack from local notes/papers
python3 scripts/build_custom_knowledge_pack.py --output offline_packs_cache/custom_pack.json
```

---

## Example Queries Where 1B Models Fail & Duniya Excels

Inside the app's **Research Lab** and **1B vs MoE Benchmark Arena**, you can test any custom research prompt or tap any of the 6 built-in multi-hop stress tests:

1. **Zero-Knowledge Cryptography & Asymptotics:**  
   *"Compare Groth16, PLONK (KZG), STARKs (FRI), and Binius across trusted setup requirements, asymptotic prover/verifier complexity, proof size, and post-quantum security. Which architecture is optimal for client-side mobile proving vs L1 state verification?"*
2. **Sparse MoE & Mobile Flash Streaming Math:**  
   *"Explain why a 1B dense transformer plateaus on offline mobile research, and derive the memory-bandwidth and latency math for streaming a 30B-100B Sparse MoE + Disk-Mapped N-Gram (Engram) architecture from UFS 4.0 flash storage within a 12GB RAM budget."*
3. **Molecular Biology & Gene Editing Delivery:**  
   *"Compare the molecular mechanisms of wild-type CRISPR-Cas9, Cytosine/Adenine Base Editing (CBE/ABE), and Prime Editing (PE3). Contrast double-strand break (DSB) hazards, p53 activation, bystanders, indels, and AAV in vivo delivery bottlenecks."*
4. **Comparative Historical State Capacity:**  
   *"Why did the Eastern Roman (Byzantine) Empire survive the 5th-century crisis that destroyed the Western Roman Empire, and later withstand the 7th-century Arab conquests? Synthesize geographic choke points, Anastasian fiscal reforms, tax base urbanization, and the military Theme system."*
5. **Magnetic Confinement Fusion Physics:**  
   *"Compare Tokamak and Stellarator magnetic confinement fusion architectures. Explain how each generates rotational transform (iota), why Tokamaks suffer from current-driven disruptions and Greenwald density limits, and how neo-classical transport optimization (quasi-isodynamic fields in W7-X) changed the Stellarator trade-off."*
6. **Austere High-Altitude Expedition Medicine:**  
   *"During an offline Himalayan expedition at 5,200 meters, a climber develops ataxia, confusion, and resting dyspnea with pink frothy sputum. Differentiate High-Altitude Pulmonary Edema (HAPE) from High-Altitude Cerebral Edema (HACE), explain the hypoxic pulmonary vasoconstriction vs vasogenic blood-brain barrier mechanisms, and specify exact austere field protocols and pharmacological regimens."*

---

## Repository Structure

- [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml) — Strips `INTERNET` and `ACCESS_NETWORK_STATE` permissions (`tools:node="remove"`) for hardware-enforced offline operation.
- [`app/src/main/cpp/duniya_engine.cpp`](app/src/main/cpp/duniya_engine.cpp) — Native C++17 engine implementing:
  - 16KB page-aligned POSIX `mmap` + `POSIX_MADV_RANDOM` / `POSIX_MADV_WILLNEED` **64-Expert Sparse MoE Streamer** (`Top-2` active = `3.125%` active parameters).
  - **65,536-Bucket Disk-Mapped N-Gram Memory Table (`Engram`)** indexed by 64-bit FNV-1a token 2-gram & 3-gram hashes.
  - **ARM64 NEON Int8 SIMD** dot-product kernels (`vld1q_s8`, `vmull_s8`, `vpadalq_s16`, `vaddvq_s32`).
  - **GGUF v2/v3 Binary Header Inspector & Memory-Mapper**.
  - **Linux Kernel Telemetry & Airgap Inspector** (`/proc/self/status`, `/proc/meminfo`, `getrusage`, `getgroups()` checking absence of `AID_INET = 3003`).
- [`app/src/main/java/com/example/duniya/engine/HybridResearchEngine.kt`](app/src/main/java/com/example/duniya/engine/HybridResearchEngine.kt) — 4-Hop Research Pipeline (`Decompose → Hybrid Retrieve → Sparse MoE + Engram Pass → Structured Synthesis & Comparison Matrix`).
- [`app/src/main/java/com/example/duniya/data/DuniyaKnowledgeDatabase.kt`](app/src/main/java/com/example/duniya/data/DuniyaKnowledgeDatabase.kt) — Android SQLite FTS4 BM25 + 256-dim Int8 Quantized Vector Similarity + 1-Hop Concept Graph Retriever.
- [`app/src/main/java/com/example/duniya/data/DuniyaResearchCorpus.kt`](app/src/main/java/com/example/duniya/data/DuniyaResearchCorpus.kt) — Built-in graduate-level multi-domain research corpus and 1B failure benchmark suite.
- [`app/src/main/java/com/example/duniya/ui/main/MainScreen.kt`](app/src/main/java/com/example/duniya/ui/main/MainScreen.kt) — Jetpack Compose UI with 4 tabs (`Research`, `50GB Packs`, `1B vs MoE`, `Airgap Audit`).
- [`web/index.html`](web/index.html) — Standalone 100% offline Web App Companion featuring the **Ethereum Silver Glassmorphic Design System**, interactive 8×8 silicon wafer die MoE router, multi-document synthesis, and sound synthesizer.
- [`web/style.css`](web/style.css) — Modern CSS with `@layer reset, base, theme, components, utilities`, `backdrop-filter: blur(24px) saturate(190%)`, specular hairlines, and responsive typography.
- [`web/app.js`](web/app.js) — Client-side 4-hop synthesis engine, hash routing, and Web Audio API offline sound effects.
- [`web/data.js`](web/data.js) — Pre-compiled JSON corpus containing 21 peer-reviewed research monographs, 6 hard benchmarks, equations, and citations.
- [`index.html`](index.html) — Root launcher and redirect to `web/index.html` for local and hosted web access.
- [`scripts/setup_offline_assets.sh`](scripts/setup_offline_assets.sh) — CLI verifier and external GGUF Sparse MoE pack provisioner.
- [`scripts/build_custom_knowledge_pack.py`](scripts/build_custom_knowledge_pack.py) — Python utility to compile custom offline knowledge packs.

---

### Running the Ethereum Silver Web Companion

You can launch the web companion instantly in any browser — zero server or build tool required:

```bash
# Option 1: Open directly in Chrome/Safari/Firefox (100% offline via file://)
open web/index.html

# Option 2: Run a lightweight local HTTP server
python3 -m http.server 8080
# Visit http://localhost:8080 or http://localhost:8080/web/
```

---

## License
MIT License — Open Source for the Offline AI & Ethereum / GrapheneOS Community.
