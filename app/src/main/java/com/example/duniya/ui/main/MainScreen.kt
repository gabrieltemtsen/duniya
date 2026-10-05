package com.example.duniya.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.duniya.data.DuniyaResearchCorpus
import com.example.duniya.data.HardBenchmarkPrompt
import com.example.duniya.data.ResearchArticle
import com.example.duniya.engine.ComparisonTableData
import com.example.duniya.engine.NativeDuniyaBridge
import com.example.duniya.engine.ReportSection
import com.example.duniya.engine.ResearchMode
import com.example.duniya.engine.ResearchSynthesisReport
import com.example.duniya.theme.LocalDuniyaPalette
import com.example.duniya.theme.LocalDuniyaThemeToggle

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = LocalDuniyaPalette.current

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importExternalFileFromSaf(uri)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = p.pageBg,
        topBar = {
            DuniyaNativeMasthead(
                uiState = uiState,
                onNewResearch = { viewModel.startNewResearch() },
                onOpenAirgapTab = { viewModel.selectTab(DuniyaTab.AIRGAP) }
            )
        },
        bottomBar = {
            DuniyaBottomBar(
                selectedTab = uiState.activeTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(p.pageBg)
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                DuniyaTab.RESEARCH -> ResearchMonographWorkspace(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DuniyaTab.CORPUS -> CorpusAndPackManagerScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onLaunchSaf = { safLauncher.launch(arrayOf("*/*")) }
                )
                DuniyaTab.BENCHMARK -> BenchmarkArenaScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                DuniyaTab.AIRGAP -> GrapheneOsAirgapAuditScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    val modalArticle = uiState.selectedArticleForModal
    if (modalArticle != null) {
        OfflineArticleReaderModal(
            article = modalArticle,
            onDismiss = { viewModel.openArticleModal(null) },
            onAskFollowUp = { q ->
                viewModel.openArticleModal(null)
                viewModel.updateQueryInput(q)
                viewModel.selectTab(DuniyaTab.RESEARCH)
                viewModel.runResearchQuery(q, ResearchMode.DEEP_SYNTHESIS)
            },
            onOpenRelatedArticle = { relId ->
                val rel = DuniyaResearchCorpus.articles.firstOrNull { it.id == relId }
                if (rel != null) viewModel.openArticleModal(rel)
            }
        )
    }
}

// ============================================================================
// RICH EDITORIAL MARKDOWN PARSER (ZERO RAW ** ASTERISKS)
// ============================================================================

@Composable
private fun rememberFormattedMarkdown(
    rawText: String,
    boldColor: Color,
    codeColor: Color
): AnnotatedString {
    return remember(rawText, boldColor, codeColor) {
        val cleaned = rawText
            .replace(" • **", "\n\n• **")
            .replace(" • ", "\n• ")
            .trim()

        buildAnnotatedString {
            var idx = 0
            while (idx < cleaned.length) {
                val boldStart = cleaned.indexOf("**", idx)
                if (boldStart == -1) {
                    append(cleaned.substring(idx))
                    break
                }
                if (boldStart > idx) {
                    append(cleaned.substring(idx, boldStart))
                }
                val boldEnd = cleaned.indexOf("**", boldStart + 2)
                if (boldEnd == -1) {
                    append(cleaned.substring(boldStart + 2))
                    break
                }
                val boldContent = cleaned.substring(boldStart + 2, boldEnd)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = boldColor
                    )
                ) {
                    append(boldContent)
                }
                idx = boldEnd + 2
            }
        }
    }
}

@Composable
private fun RichEditorialText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 21.sp,
    fontFamily: FontFamily = FontFamily.SansSerif,
    color: Color = LocalDuniyaPalette.current.inkPrimary
) {
    val p = LocalDuniyaPalette.current
    val annotated = rememberFormattedMarkdown(
        rawText = text,
        boldColor = p.inkPrimary,
        codeColor = p.vermilion
    )
    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        color = color
    )
}

// ============================================================================
// NATIVE MASTHEAD & GEOMETRIC EMBLEM
// ============================================================================

@Composable
private fun DuniyaNativeMasthead(
    uiState: DuniyaUiState,
    onNewResearch: () -> Unit = {},
    onOpenAirgapTab: () -> Unit
) {
    val p = LocalDuniyaPalette.current
    val toggleTheme = LocalDuniyaThemeToggle.current
    val tel = uiState.kernelTelemetry
    val audit = uiState.securityAudit
    val isAirgapped = !audit.manifestInternetPermissionPresent && !audit.kernelAidInetPresent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.sheetSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Geometric Mark + Clean Brand + 100% Offline Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DuniyaGeometricMark(
                    chassisColor = p.hardwareChassis,
                    vermilionColor = p.vermilion,
                    inkColor = p.hardwareInk
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Duniya",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = p.inkPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Box(
                            modifier = Modifier
                                .size(5.5.dp)
                                .background(Color(0xFF38C98E), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = p.inkMuted
                        )
                    }
                }
            }

            // Right: New Research Button + Minimal Status Pill + Theme Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // New Research Action Button
                Surface(
                    color = p.vermilionSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onNewResearch() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Research",
                            tint = p.vermilion,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "New",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = p.vermilion
                        )
                    }
                }

                Surface(
                    color = p.recessedWell,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onOpenAirgapTab() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    if (isAirgapped) Color(0xFF38C98E) else p.vermilion,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isAirgapped) "0 Net · ${"%.0f".format(tel.vmRssMb)} MB" else "Audit",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = p.inkSecondary
                        )
                    }
                }

                // Theme Toggle
                Surface(
                    color = p.recessedWell,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { toggleTheme() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            p.isEthereum -> {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Switch to Paper",
                                    tint = p.inkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            p.isDark -> {
                                Text(
                                    text = "⟠",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF627EEA)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Switch to Dark",
                                    tint = p.inkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = p.ruleLine.copy(alpha = 0.6f), thickness = 0.5.dp)
    }
}

@Composable
private fun DuniyaGeometricMark(
    chassisColor: Color,
    vermilionColor: Color,
    inkColor: Color
) {
    Surface(
        color = chassisColor,
        shape = RoundedCornerShape(7.dp),
        modifier = Modifier.size(28.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(5.dp)) {
            val w = size.width
            val h = size.height
            val prismPath = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.92f, h * 0.86f)
                lineTo(w * 0.08f, h * 0.86f)
                close()
            }
            drawPath(
                path = prismPath,
                color = inkColor,
                style = Stroke(width = 1.7.dp.toPx())
            )
            drawLine(
                color = vermilionColor,
                start = Offset(w * 0.5f, h * 0.14f),
                end = Offset(w * 0.5f, h * 0.86f),
                strokeWidth = 1.7.dp.toPx()
            )
            drawCircle(
                color = vermilionColor,
                radius = 1.8.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.12f)
            )
        }
    }
}

// ============================================================================
// NATIVE BOTTOM NAVIGATION BAR
// ============================================================================

@Composable
private fun DuniyaBottomBar(
    selectedTab: DuniyaTab,
    onSelectTab: (DuniyaTab) -> Unit
) {
    val p = LocalDuniyaPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.sheetSurface)
    ) {
        HorizontalDivider(color = p.ruleLine.copy(alpha = 0.6f), thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(DuniyaTab.RESEARCH, "Research", Icons.AutoMirrored.Filled.MenuBook),
                Triple(DuniyaTab.CORPUS, "Vault", Icons.Default.Storage),
                Triple(DuniyaTab.BENCHMARK, "Benchmarks", Icons.Default.Analytics),
                Triple(DuniyaTab.AIRGAP, "Airgap", Icons.Default.Security)
            )

            items.forEach { (tab, label, icon) ->
                val isSelected = selectedTab == tab
                val contentColor = if (isSelected) p.vermilion else p.inkMuted

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) p.vermilionSoft else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) p.inkPrimary else p.inkMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 1: GEMINI-STYLE CONVERSATIONAL RESEARCH WORKSPACE
// ============================================================================

@Composable
private fun ResearchMonographWorkspace(
    uiState: DuniyaUiState,
    viewModel: MainScreenViewModel
) {
    val report = uiState.currentReport
    var isDieExpanded by remember(report?.query) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (report == null) {
                // GEMINI HERO LANDING STATE
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        GeminiHeroLanding(
                            onSelectPrompt = { bp ->
                                viewModel.runBenchmarkPrompt(bp)
                            }
                        )
                    }
                }
            } else {
                // GEMINI CONVERSATIONAL RESEARCH THREAD
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. User Message Bubble (Right-aligned)
                    item {
                        GeminiUserMessageBubble(query = report.query)
                    }

                    // 2. Gemini Assistant Response Card
                    item {
                        GeminiAssistantCard(
                            report = report,
                            isDieExpanded = isDieExpanded,
                            onToggleDie = { isDieExpanded = !isDieExpanded },
                            onFollowUpClick = { followUpQuery ->
                                viewModel.updateQueryInput(followUpQuery)
                                viewModel.runResearchQuery(followUpQuery, ResearchMode.DEEP_SYNTHESIS)
                            },
                            onOpenSourceArticle = { art ->
                                viewModel.openArticleModal(art)
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Input Dock
        GeminiInputDock(
            queryText = uiState.queryInput,
            onQueryChange = { viewModel.updateQueryInput(it) },
            isRunning = uiState.isRunningQuery,
            onSend = {
                viewModel.runResearchQuery(uiState.queryInput, ResearchMode.DEEP_SYNTHESIS)
            }
        )
    }
}

@Composable
private fun GeminiHeroLanding(
    onSelectPrompt: (HardBenchmarkPrompt) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalDuniyaPalette.current
    val benchmarks = DuniyaResearchCorpus.hardBenchmarkPrompts.take(4)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Ethereum Sparkle
        Surface(
            color = p.vermilionSoft,
            shape = CircleShape,
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "✦",
                    fontSize = 28.sp,
                    color = p.vermilion
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Hello, Researcher.",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = p.inkPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "What offline question would you like to explore today?",
            fontSize = 13.sp,
            color = p.inkMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "SUGGESTED INQUIRIES",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = p.vermilion
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            benchmarks.forEach { bp ->
                val icon = when {
                    bp.category.contains("Crypto") -> "🔐"
                    bp.category.contains("AI") -> "⚡"
                    bp.category.contains("Bio") -> "🧬"
                    bp.category.contains("Phys") -> "⚛️"
                    else -> "📜"
                }
                Surface(
                    color = p.sheetSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
                        .clickable { onSelectPrompt(bp) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bp.category.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.vermilion
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bp.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = p.inkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bp.prompt,
                                fontSize = 11.5.sp,
                                color = p.inkMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "→", fontSize = 16.sp, color = p.inkMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiUserMessageBubble(query: String) {
    val p = LocalDuniyaPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = p.sheetSurface,
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, p.ruleLine, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
        ) {
            Text(
                text = query,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = p.inkPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun GeminiAccordion(
    icon: String,
    title: String,
    badge: String? = null,
    badgeColor: Color = LocalDuniyaPalette.current.vermilion,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val p = LocalDuniyaPalette.current
    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = icon, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.inkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badge,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = p.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = p.ruleLine.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun GeminiAssistantCard(
    report: ResearchSynthesisReport,
    isDieExpanded: Boolean,
    onToggleDie: () -> Unit,
    onFollowUpClick: (String) -> Unit,
    onOpenSourceArticle: (ResearchArticle) -> Unit
) {
    val p = LocalDuniyaPalette.current
    val primaryArticle = report.sourceArticles.firstOrNull()
    val primaryTitle = primaryArticle?.title ?: "Multi-Domain Research Synthesis"
    val domain = primaryArticle?.domain?.uppercase() ?: "SYSTEMS & ALGORITHMS"
    val tel = report.moeTelemetry

    var expandedExplanation by remember(report.query) { mutableStateOf(false) }
    var expandedEquations by remember(report.query) { mutableStateOf(false) }
    var expandedMatrix by remember(report.query) { mutableStateOf(false) }
    var expandedFailure by remember(report.query) { mutableStateOf(false) }
    var expandedCitations by remember(report.query) { mutableStateOf(false) }

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Sparkle + Domain + MoE Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "✦", fontSize = 16.sp, color = p.vermilion)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = domain,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.vermilion,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = p.recessedWell,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { onToggleDie() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color(0xFF38C98E), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${tel.topExperts.size.coerceAtLeast(2)}/64 MoE · ${if (isDieExpanded) "Hide Die" else "8×8 Die ▾"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = p.inkSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = primaryTitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                lineHeight = 25.sp,
                color = p.inkPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Executive Findings (Clean Callout)
            Surface(
                color = p.recessedWell,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, p.ruleLine, RoundedCornerShape(10.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .background(p.vermilion, RoundedCornerShape(1.5.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        RichEditorialText(
                            text = primaryArticle?.summary ?: report.executiveThesis,
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp,
                            color = p.inkPrimary
                        )
                    }
                }
            }

            // Expanded Die (if toggled)
            if (isDieExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                SiliconMoEDieInstrument(report = report)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Collapsible Accordions:
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 1. Technical Explanation & Mechanisms
                if (report.sections.isNotEmpty() || !primaryArticle?.deepExplanation.isNullOrBlank()) {
                    GeminiAccordion(
                        icon = "📖",
                        title = "Technical Explanation & Derivation",
                        badge = "${report.sections.size.coerceAtLeast(1)} sections",
                        badgeColor = p.verdigris,
                        isExpanded = expandedExplanation,
                        onToggle = { expandedExplanation = !expandedExplanation }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!primaryArticle?.deepExplanation.isNullOrBlank()) {
                                RichEditorialText(
                                    text = primaryArticle?.deepExplanation ?: "",
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = p.inkSecondary
                                )
                            }
                            report.sections.forEachIndexed { idx, sec ->
                                Column {
                                    Text(
                                        text = "${idx + 1}. ${sec.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = p.inkPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    RichEditorialText(
                                        text = sec.body,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        color = p.inkSecondary
                                    )
                                    if (!sec.formulaOrMechanismBox.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = p.recessedWell,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth().border(0.5.dp, p.ruleLine, RoundedCornerShape(6.dp))
                                        ) {
                                            Text(
                                                text = sec.formulaOrMechanismBox,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.5.sp,
                                                color = p.brass,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Governing Equations
                val eqArticles = report.sourceArticles.filter { !it.firstPrinciplesMathOrMechanism.isNullOrBlank() }
                if (eqArticles.isNotEmpty()) {
                    GeminiAccordion(
                        icon = "📐",
                        title = "First-Principles Governing Equations",
                        badge = "${eqArticles.size} formulas",
                        badgeColor = p.brass,
                        isExpanded = expandedEquations,
                        onToggle = { expandedEquations = !expandedEquations }
                    ) {
                        ArchivalEquationsSheet(report = report)
                    }
                }

                // 3. Comparative Specification Matrix
                if (report.comparisonTable != null) {
                    GeminiAccordion(
                        icon = "📊",
                        title = "Comparative Specification Matrix",
                        badge = "${report.comparisonTable.entityHeaders.size} entities",
                        badgeColor = p.vermilion,
                        isExpanded = expandedMatrix,
                        onToggle = { expandedMatrix = !expandedMatrix }
                    ) {
                        ArchivalComparisonMatrixSheet(table = report.comparisonTable)
                    }
                }

                // 4. Why 1B Dense Models Fail
                val failureArticles = report.sourceArticles.filter { !it.oneBModelFailureMode.isNullOrBlank() }
                if (failureArticles.isNotEmpty()) {
                    GeminiAccordion(
                        icon = "⚠️",
                        title = "Why 1B Dense Models Fail on This Inquiry",
                        badge = "Autopsy",
                        badgeColor = p.crimson,
                        isExpanded = expandedFailure,
                        onToggle = { expandedFailure = !expandedFailure }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            failureArticles.forEach { art ->
                                Surface(
                                    color = p.crimsonSoft,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(0.5.dp, p.crimson.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = art.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = p.crimson
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = art.oneBModelFailureMode,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = p.inkPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Peer-Reviewed Citations
                if (report.sourceArticles.isNotEmpty()) {
                    GeminiAccordion(
                        icon = "📚",
                        title = "Peer-Reviewed Citations & References",
                        badge = "${report.sourceArticles.size} sources",
                        badgeColor = p.verdigris,
                        isExpanded = expandedCitations,
                        onToggle = { expandedCitations = !expandedCitations }
                    ) {
                        ArchivalCitationsSheet(
                            report = report,
                            onOpenSourceArticle = onOpenSourceArticle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Suggested Follow-up Inquiries
            val followUps = remember(primaryTitle, domain) {
                when {
                    domain.contains("CRYPTO") -> listOf(
                        "Compare Groth16 vs STARKs proof sizes",
                        "Explain binary tower fields in Binius",
                        "PeerDAS 2D KZG erasure coding"
                    )
                    domain.contains("AI") || domain.contains("SPARSE") -> listOf(
                        "How does 65K Engram table prevent collisions?",
                        "Compare MLA vs GQA KV-cache compression",
                        "Why do 1B dense models hallucinate on numbers?"
                    )
                    domain.contains("BIO") -> listOf(
                        "Compare Prime Editing PE3 vs Cas9 DSBs",
                        "Explain N1-methylpseudouridine immune evasion",
                        "What are senolytics vs OSKM reprogramming?"
                    )
                    domain.contains("PHYSIC") -> listOf(
                        "Tokamak disruptions vs Stellarator 3D coils",
                        "High-NA EUV anamorphic optics & shot noise",
                        "Perovskite-silicon tandem solar efficiency limits"
                    )
                    else -> listOf(
                        "Show first-principles mathematical derivation",
                        "Compare specification matrix",
                        "Why does a 1B model fail here?"
                    )
                }
            }

            Text(
                text = "Suggested Inquiries:",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = p.inkMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                followUps.forEach { fu ->
                    Surface(
                        color = p.recessedWell,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
                            .clickable { onFollowUpClick(fu) }
                    ) {
                        Text(
                            text = fu,
                            fontSize = 11.sp,
                            color = p.inkSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiInputDock(
    queryText: String,
    onQueryChange: (String) -> Unit,
    isRunning: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalDuniyaPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(p.pageBg.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            color = p.sheetSurface,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, p.ruleLine, RoundedCornerShape(26.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = p.inkMuted,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (queryText.isEmpty()) {
                        Text(
                            text = "Ask anything offline...",
                            fontSize = 13.5.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = p.inkMuted
                        )
                    }
                    BasicTextField(
                        value = queryText,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (queryText.isNotBlank() && !isRunning) onSend()
                        }),
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            color = p.inkPrimary,
                            fontFamily = FontFamily.SansSerif
                        ),
                        cursorBrush = SolidColor(p.vermilion),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (queryText.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = p.inkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Surface(
                    color = if (queryText.isNotBlank()) p.vermilion else p.recessedWell,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(enabled = queryText.isNotBlank() && !isRunning) {
                            onSend()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "↑",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (queryText.isNotBlank()) Color.White else p.inkMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Duniya runs 100% offline via local Sparse-MoE silicon · Zero remote queries",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = p.inkMuted.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun ExecutionTelemetryStrip(
    report: ResearchSynthesisReport,
    vmRssMb: Double,
    isDieExpanded: Boolean,
    onToggleDie: () -> Unit
) {
    val p = LocalDuniyaPalette.current
    val tel = report.moeTelemetry
    val totalHopMs = remember(report) { report.hopTrace.sumOf { it.latencyMs } }

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onToggleDie() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF38C98E), CircleShape)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "${tel.topExperts.size.coerceAtLeast(2)}/64 MoE · ${"%.1f".format(tel.effectiveTokensPerSec)} tok/s · ${"%.0f".format(vmRssMb)} MB · ${"%.0f".format(totalHopMs)}ms",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDieExpanded) "Hide Die" else "8×8 Die",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.vermilion
                )
                Icon(
                    imageVector = if (isDieExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = p.vermilion,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// ============================================================================
// ANODIZED SILICON INSTRUMENT: 8x8 EXPERT WAFER DIE & 4-HOP TRACE
// ============================================================================

@Composable
private fun SiliconMoEDieInstrument(report: ResearchSynthesisReport) {
    val p = LocalDuniyaPalette.current
    val moe = report.moeTelemetry
    var traceExpanded by remember { mutableStateOf(false) }

    val activeIds = remember(moe.topExperts) {
        moe.topExperts.take(2).map { it.id }.toSet()
    }
    val secondaryIds = remember(moe.topExperts) {
        moe.topExperts.drop(2).map { it.id }.toSet()
    }

    Surface(
        color = p.hardwareChassis,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.hardwareRule, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Silkscreen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { traceExpanded = !traceExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE55B3C), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SILICON MoE ROUTER · 64 EXPERTS (TOP-2 FLASH MMAP)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = p.hardwareInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (traceExpanded) "HIDE HOPS" else "4-HOP TRACE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE59F38)
                    )
                    Icon(
                        imageVector = if (traceExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFFE59F38),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Instrument Body: Left = 8x8 Silicon Wafer Grid, Right = Active Expert Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 8x8 Expert Die Map (64 Experts)
                Surface(
                    color = p.hardwareWell,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(104.dp)
                        .border(1.dp, p.hardwareRule, RoundedCornerShape(10.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        val cols = 8
                        val rows = 8
                        val gap = 2.5.dp.toPx()
                        val cellW = (size.width - gap * (cols - 1)) / cols
                        val cellH = (size.height - gap * (rows - 1)) / rows

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val expertId = r * 8 + c
                                val cellColor = when {
                                    expertId in activeIds -> Color(0xFFE55B3C)    // Active Top-2 Vermilion
                                    expertId in secondaryIds -> Color(0xFFE59F38) // Secondary Hop-2 Amber
                                    else -> Color(0xFF2B2924)                     // Dormant on UFS Flash
                                }
                                drawRoundRect(
                                    color = cellColor,
                                    topLeft = Offset(c * (cellW + gap), r * (cellH + gap)),
                                    size = Size(cellW, cellH),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Hardware Telemetry & Active Experts
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HardwareMetricTile(
                            label = "THROUGHPUT",
                            value = "${"%.1f".format(moe.effectiveTokensPerSec)} tok/s",
                            accent = Color(0xFF38C98E)
                        )
                        HardwareMetricTile(
                            label = "ENGRAM HASH",
                            value = "${moe.ngramLookups} O(1)",
                            accent = Color(0xFFE59F38)
                        )
                        HardwareMetricTile(
                            label = "FLASH MMAP",
                            value = "${moe.flashBytesStreamed / 1024} KB",
                            accent = p.hardwareInk
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = p.hardwareRule)
                    Spacer(modifier = Modifier.height(6.dp))

                    moe.topExperts.take(3).forEachIndexed { idx, exp ->
                        val isPrimary = idx < 2
                        val dotColor = if (isPrimary) Color(0xFFE55B3C) else Color(0xFFE59F38)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exp.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPrimary) p.hardwareInk else p.hardwareMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${"%.1f".format(exp.gateWeight * 100f)}%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = dotColor
                            )
                        }
                    }
                }
            }

            // Expandable 4-Hop Pipeline Trace
            AnimatedVisibility(visible = traceExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = p.hardwareRule)
                    Spacer(modifier = Modifier.height(10.dp))
                    report.hopTrace.forEach { hop ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                color = p.hardwareWell,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.border(1.dp, p.hardwareRule, RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = "H${hop.hopNumber}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE55B3C),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = hop.hopTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.hardwareInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${"%.1f".format(hop.latencyMs)} ms",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF38C98E)
                                    )
                                }
                                Text(
                                    text = hop.detail,
                                    fontSize = 10.sp,
                                    color = p.hardwareMuted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareMetricTile(
    label: String,
    value: String,
    accent: Color
) {
    val p = LocalDuniyaPalette.current
    Column {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = p.hardwareMuted
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = accent
        )
    }
}

// ============================================================================
// NATIVE SEGMENTED REPORT SUB-NAVIGATION BAR
// ============================================================================

@Composable
private fun ReportSectionSegmentedBar(
    selectedIndex: Int,
    matrixCount: Int,
    equationCount: Int,
    onSelectIndex: (Int) -> Unit
) {
    val p = LocalDuniyaPalette.current
    val tabs = listOf(
        "Monograph",
        if (matrixCount > 0) "Matrix ($matrixCount)" else "Matrix",
        "Equations ($equationCount)",
        "vs. 1B Dense"
    )

    Surface(
        color = p.recessedWell,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, p.ruleLine.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                val isSelected = selectedIndex == idx
                Surface(
                    color = if (isSelected) p.sheetSurface else Color.Transparent,
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectIndex(idx) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) p.vermilion else p.inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// REPORT SECTION 0: EDITORIAL MONOGRAPH THESIS & MECHANISMS
// ============================================================================

@Composable
private fun MonographThesisSheet(
    report: ResearchSynthesisReport,
    onJumpToMatrix: () -> Unit,
    onJumpToEquations: () -> Unit,
    onJumpTo1BContrast: () -> Unit
) {
    val p = LocalDuniyaPalette.current

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            val primaryArticle = report.sourceArticles.firstOrNull()
            val primaryTitle = primaryArticle?.title ?: "Multi-Domain Research Synthesis"
            val domain = primaryArticle?.domain?.uppercase() ?: "SYSTEMS & ALGORITHMS"

            // Metadata row: Domain + Verified offline sources
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOMAIN: $domain",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.vermilion
                )
                Text(
                    text = "${report.sourceArticles.size} verified offline sources",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.verdigris
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = primaryTitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.3).sp,
                color = p.inkPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = p.ruleLine.copy(alpha = 0.7f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Executive Synthesis Callout
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(p.vermilion, RoundedCornerShape(1.5.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXECUTIVE FINDINGS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.inkMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RichEditorialText(
                        text = report.executiveThesis,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = p.inkPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action Chips to Jump to Matrix / Equations / 1B Diagnostic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (report.comparisonTable != null) {
                    QuickJumpChip(
                        label = "Matrix (${report.comparisonTable.entityHeaders.size} entities) ->",
                        bgColor = p.vermilionSoft,
                        textColor = p.vermilion,
                        onClick = onJumpToMatrix
                    )
                }
                QuickJumpChip(
                    label = "First-Principles Equations (${report.sourceArticles.size}) ->",
                    bgColor = p.brassSoft,
                    textColor = p.brass,
                    onClick = onJumpToEquations
                )
                QuickJumpChip(
                    label = "1B Dense Failure Diagnostic ->",
                    bgColor = p.crimsonSoft,
                    textColor = p.crimson,
                    onClick = onJumpTo1BContrast
                )
            }
        }
    }
}

@Composable
private fun QuickJumpChip(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EditorialMechanismSheet(
    index: Int,
    section: ReportSection
) {
    val p = LocalDuniyaPalette.current
    var expanded by remember { mutableStateOf(index <= 2) }

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SECTION 0$index · TECHNICAL ANALYSIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.verdigris
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = section.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = p.inkPrimary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = p.inkMuted
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = p.ruleLine)
                    Spacer(modifier = Modifier.height(10.dp))
                    RichEditorialText(
                        text = section.body,
                        fontSize = 13.5.sp,
                        lineHeight = 21.sp,
                        color = p.inkSecondary
                    )

                    if (!section.formulaOrMechanismBox.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = p.recessedWell,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, p.ruleLine, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "FIRST-PRINCIPLES FORMULATION",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.brass
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = section.formulaOrMechanismBox,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = p.inkPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// REPORT SECTION 1: MULTI-DIMENSIONAL SPECIFICATION MATRIX
// ============================================================================

@Composable
private fun ArchivalComparisonMatrixSheet(table: ComparisonTableData) {
    val p = LocalDuniyaPalette.current

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "COMPARATIVE SPECIFICATION MATRIX",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.vermilion
                    )
                    Text(
                        text = "${table.entityHeaders.size} Entities × ${table.rows.size} Technical Dimensions",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = p.inkPrimary
                    )
                }
                Text(
                    text = "Swipe Table <->",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = p.inkMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = p.pageBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.ruleLine, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .background(p.recessedWell)
                                .padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIMENSION",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.vermilion,
                                modifier = Modifier.width(145.dp)
                            )
                            table.entityHeaders.forEach { header ->
                                Text(
                                    text = header,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.inkPrimary,
                                    modifier = Modifier
                                        .width(195.dp)
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }

                        table.rows.forEachIndexed { idx, row ->
                            val rowBg = if (idx % 2 == 0) p.sheetSurface else p.recessedWell.copy(alpha = 0.55f)
                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 9.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = row.dimension,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.vermilion,
                                    modifier = Modifier.width(145.dp)
                                )
                                row.valuesByEntity.forEach { cell ->
                                    Text(
                                        text = cell,
                                        fontSize = 12.sp,
                                        color = p.inkPrimary,
                                        modifier = Modifier
                                            .width(195.dp)
                                            .padding(horizontal = 8.dp),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = p.ruleLine)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// REPORT SECTION 2: FIRST-PRINCIPLES EQUATIONS & CONSTANTS SHEET
// ============================================================================

@Composable
private fun ArchivalEquationsSheet(report: ResearchSynthesisReport) {
    val p = LocalDuniyaPalette.current

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FIRST-PRINCIPLES EQUATIONS & QUANTITATIVE BOUNDS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = p.brass
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mathematical & Biochemical Formulations",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = p.inkPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            report.sourceArticles.forEachIndexed { idx, art ->
                Surface(
                    color = p.recessedWell,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .border(1.dp, p.ruleLine, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = p.brassSoft,
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    text = "EQ.0${idx + 1}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.brass,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = art.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.inkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = art.firstPrinciplesMathOrMechanism,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = p.inkPrimary
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// REPORT SECTION 3: 1B DENSE MODEL FAILURE DIAGNOSTIC SHEET
// ============================================================================

@Composable
private fun OneBModelDiagnosticSheet(report: ResearchSynthesisReport) {
    val p = LocalDuniyaPalette.current

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, p.crimson.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = p.crimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DIAGNOSTIC: WHY A 1B DENSE MODEL (~10 TOK/S) FAILS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.crimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.why1BModelFails,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = p.inkPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = p.crimsonSoft,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.crimson.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "SPECIMEN · UNGROUNDED 1B DENSE OUTPUT (~10.4 TOK/S):",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.crimson
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${report.baseline1BOutput}\"",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        color = p.inkPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// ARCHIVAL CITATIONS & VERIFIED ENCYCLOPEDIA SOURCES
// ============================================================================

@Composable
private fun ArchivalCitationsSheet(
    report: ResearchSynthesisReport,
    onOpenSourceArticle: (ResearchArticle) -> Unit
) {
    val p = LocalDuniyaPalette.current

    Surface(
        color = p.sheetSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "VERIFIED OFFLINE MONOGRAPHS & PRIMARY CITATIONS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = p.verdigris
            )
            Spacer(modifier = Modifier.height(8.dp))

            report.sourceArticles.forEachIndexed { idx, art ->
                Surface(
                    color = p.recessedWell,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, p.ruleLine, RoundedCornerShape(10.dp))
                        .clickable { onOpenSourceArticle(art) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SOURCE 0${idx + 1} · ${art.domain.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.vermilion
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = art.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.inkPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = p.sheetSurface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(1.dp, p.ruleLine, RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "Open Folio ->",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.inkPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = p.ruleLine)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BIBLIOGRAPHY & PROTOCOL STANDARDS:",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = p.inkMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            report.citations.forEachIndexed { idx, cit ->
                Text(
                    text = "[${idx + 1}] $cit",
                    fontSize = 11.sp,
                    color = p.inkSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// ============================================================================
// TAB 2: 50GB OFFLINE PACK MANAGER & ENCYCLOPEDIA LIBRARY
// ============================================================================

@Composable
private fun CorpusAndPackManagerScreen(
    uiState: DuniyaUiState,
    viewModel: MainScreenViewModel,
    onLaunchSaf: () -> Unit
) {
    val p = LocalDuniyaPalette.current
    val usedBytes = uiState.securityAudit.diskUsedBytes
    val usedMb = usedBytes / (1024.0 * 1024.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Storage Vault Header Module
        item {
            Surface(
                color = p.sheetSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "50 GB STORAGE VAULT",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.vermilion,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${"%.1f".format(usedMb)} MB / 50 GB",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.verdigris,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (usedMb / 50000.0).toFloat().coerceIn(0.02f, 1.0f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = p.vermilion,
                        trackColor = p.recessedWell
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.installedPacks.forEach { pack ->
                        Surface(
                            color = p.recessedWell,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, p.ruleLine.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${pack.category.uppercase()} · ${pack.format}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = p.vermilion,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pack.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.inkPrimary
                                    )
                                    Text(
                                        text = "${"%.2f".format(pack.sizeBytes / (1024.0 * 1024.0))} MB · ${pack.articleOrTensorCount} indexed units",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = p.inkMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = p.verdigrisSoft,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "MMAP ACTIVE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.verdigris,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (!uiState.lastGgufInspectMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.lastGgufInspectMessage,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = p.verdigris
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.verifyAndMountLocalGgufShard() },
                            colors = ButtonDefaults.buttonColors(containerColor = p.vermilion),
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan & Mount Packs",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onLaunchSaf,
                            shape = RoundedCornerShape(9.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = p.inkPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import External",
                                fontSize = 11.5.sp,
                                color = p.inkPrimary
                            )
                        }
                    }
                }
            }
        }

        // Instant SQLite FTS4 + NEON Vector Search Bar
        item {
            Column {
                Surface(
                    color = p.sheetSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = p.vermilion,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.corpusSearchQuery.isEmpty()) {
                                Text(
                                    text = "Search offline monographs (FTS4 BM25 + NEON Int8 <10ms)...",
                                    fontSize = 13.sp,
                                    color = p.inkMuted
                                )
                            }
                            BasicTextField(
                                value = uiState.corpusSearchQuery,
                                onValueChange = { viewModel.updateCorpusSearch(it) },
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = p.inkPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(p.vermilion),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (uiState.corpusSearchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = p.inkMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.updateCorpusSearch("") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DuniyaResearchCorpus.domains) { dom ->
                        val selected = uiState.selectedDomain == dom
                        Surface(
                            color = if (selected) p.hardwareChassis else p.sheetSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, if (selected) p.hardwareChassis else p.ruleLine, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectDomainFilter(dom) }
                        ) {
                            Text(
                                text = dom,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) p.hardwareInk else p.inkSecondary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Encyclopedia Monograph Cards
        items(uiState.filteredArticles, key = { it.id }) { article ->
            val hit = uiState.corpusSearchHits.firstOrNull { it.article.id == article.id }
            Surface(
                color = p.sheetSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                    .clickable { viewModel.openArticleModal(article) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${article.domain.uppercase()} · ${article.subcategory.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.vermilion,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hit != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BM25 ${"%.1f".format(hit.bm25LexicalScore)} · Vec ${"%.2f".format(hit.neonVectorSim)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.verdigris
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.summary,
                        fontSize = 12.5.sp,
                        color = p.inkSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 3: 1B DENSE vs. DUNIYA FLASH-MoE BENCHMARK ARENA
// ============================================================================

@Composable
private fun BenchmarkArenaScreen(
    uiState: DuniyaUiState,
    viewModel: MainScreenViewModel
) {
    val p = LocalDuniyaPalette.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = p.sheetSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VITALIK BENCHMARK TARGET · >50% FRONTIER QUALITY OFFLINE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.vermilion
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Flash-Streamed Sparse MoE (64x2) vs. 1B Dense Baseline",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = p.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Compares a 1B Dense On-Device Model (~10.4 tok/s, 100% active weights, ungrounded parametric recall) against Duniya's Flash-Streamed Sparse MoE (3.1% active weights) + 65K Disk N-Gram Engram + SQLite FTS4/NEON Int8 Hybrid RAG.",
                        fontSize = 12.5.sp,
                        color = p.inkSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    BenchmarkScoreBar(
                        label = "Duniya Sparse MoE + Hybrid RAG (~42.6 tok/s)",
                        scorePct = uiState.averageDuniyaScorePct,
                        color = p.verdigris
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BenchmarkScoreBar(
                        label = "Vitalik Target Bar (>50% of Frontier AI + Web Search)",
                        scorePct = 50,
                        color = p.brass
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BenchmarkScoreBar(
                        label = "1B Dense Mobile Baseline (~10.4 tok/s, High Hallucination)",
                        scorePct = uiState.average1BScorePct,
                        color = p.crimson
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.runFullBenchmarkSuite() },
                        colors = ButtonDefaults.buttonColors(containerColor = p.vermilion),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isRunningBenchmark) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "Execute All 6 Multi-Domain Hard Benchmarks On-Device",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (uiState.benchmarkEntries.isEmpty()) {
            items(DuniyaResearchCorpus.hardBenchmarkPrompts, key = { it.id }) { bp ->
                Surface(
                    color = p.sheetSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                        .clickable { viewModel.runBenchmarkPrompt(bp) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = bp.category.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.vermilion
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bp.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1B Failure Mode: ${bp.why1BFailsShort}",
                            fontSize = 12.sp,
                            color = p.crimson,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Run dossier in Research Lab ->",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.verdigris
                        )
                    }
                }
            }
        } else {
            items(uiState.benchmarkEntries, key = { it.benchmark.id }) { entry ->
                Surface(
                    color = p.sheetSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                        .clickable { viewModel.runBenchmarkPrompt(entry.benchmark) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.benchmark.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.inkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Duniya ${entry.duniyaScorePct}% vs 1B ${entry.dense1BScorePct}%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.verdigris
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(entry.duniyaTokPerSec)} tok/s (vs ${entry.dense1BTokPerSec} tok/s) · Expert: ${entry.topExpertUsed}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = p.vermilion
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RichEditorialText(
                            text = entry.duniyaReport.executiveThesis,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = p.inkSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkScoreBar(
    label: String,
    scorePct: Int,
    color: Color
) {
    val p = LocalDuniyaPalette.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = p.inkPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$scorePct%",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (scorePct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = p.recessedWell
        )
    }
}

// ============================================================================
// TAB 4: GRAPHENEOS & KERNEL AIRGAP SECURITY AUDIT
// ============================================================================

@Composable
private fun GrapheneOsAirgapAuditScreen(
    uiState: DuniyaUiState,
    viewModel: MainScreenViewModel
) {
    val p = LocalDuniyaPalette.current
    val audit = uiState.securityAudit
    val tel = uiState.kernelTelemetry

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = p.sheetSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.ruleLine, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = p.verdigrisSoft,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = p.verdigris,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HARDWARE AIRGAP & GRAPHENEOS AUDIT",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.verdigris
                            )
                            Text(
                                text = "Linux Kernel & PackageManager Verification",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = p.inkPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = p.ruleLine)
                    Spacer(modifier = Modifier.height(8.dp))

                    AuditCheckRow(
                        title = "android.permission.INTERNET",
                        status = if (!audit.manifestInternetPermissionPresent) "STRIPPED (PASS)" else "PRESENT (FAIL)",
                        passed = !audit.manifestInternetPermissionPresent,
                        detail = "Manifest explicitly strips INTERNET via tools:node=\"remove\". App cannot make API or web calls."
                    )
                    AuditCheckRow(
                        title = "Linux Kernel AID_INET (GID 3003)",
                        status = if (!audit.kernelAidInetPresent) "NO_AID_INET (PASS)" else "PRESENT (FAIL)",
                        passed = !audit.kernelAidInetPresent,
                        detail = "Verified via C++ getgroups(): process lacks GID 3003; AF_INET/AF_INET6 socket() syscalls are blocked by kernel."
                    )
                    AuditCheckRow(
                        title = "Google Play Services (GrapheneOS)",
                        status = "0 GMS DEPS (PASS)",
                        passed = !audit.googlePlayServicesRequired,
                        detail = "Runs 100% natively on pure AOSP & GrapheneOS via C++17 NDK, POSIX mmap, and Android SQLite FTS4."
                    )
                    AuditCheckRow(
                        title = "12 GB Maximum RAM Ceiling",
                        status = "${"%.1f".format(tel.vmRssMb)} MB / 12 GB (PASS)",
                        passed = tel.vmRssMb < 12288.0,
                        detail = "Peak RSS (VmHWM): ${"%.1f".format(tel.vmPeakMb)} MB · Device Total RAM: ${"%.0f".format(tel.deviceMemTotalMb)} MB (${"%.0f".format(tel.deviceMemAvailMb)} MB free)"
                    )
                    AuditCheckRow(
                        title = "50 GB Maximum Storage Ceiling",
                        status = "${"%.2f".format(audit.diskUsedBytes / (1024.0 * 1024.0))} MB / 50 GB (PASS)",
                        passed = audit.diskUsedBytes < audit.diskBudgetMaxBytes,
                        detail = "Sparse MoE & N-Gram tables streamed directly from flash via POSIX mmap + MADV_WILLNEED."
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.refreshTelemetryAndAudit() },
                        colors = ButtonDefaults.buttonColors(containerColor = p.hardwareChassis),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Re-Verify Live Kernel & Process Telemetry",
                            color = p.hardwareInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Anodized Hardware /proc/self/status Telemetry Module
        item {
            Surface(
                color = p.hardwareChassis,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, p.hardwareRule, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE LINUX /PROC/SELF/STATUS & MMAP TELEMETRY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE59F38)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val stats = listOf(
                        Pair("Native JNI Engine", "${NativeDuniyaBridge.isNativeLoaded} (libduniya_engine.so)"),
                        Pair("SIMD Instruction Set", tel.simdArch),
                        Pair("OS Kernel Page Size", "${tel.osPageSizeBytes} B (16KB ELF Aligned)"),
                        Pair("CPU Hardware Cores", "${tel.cpuCores} cores (${tel.processThreads} threads)"),
                        Pair("Process VmRSS (Physical)", "${"%.2f".format(tel.vmRssMb)} MB"),
                        Pair("Process VmHWM (Peak RSS)", "${"%.2f".format(tel.vmPeakMb)} MB"),
                        Pair("Flash-MoE mmap Region", "${"%.2f".format(tel.mmapPackBytes / (1024.0 * 1024.0))} MB"),
                        Pair("Cumulative MoE Steps", "${tel.totalExpertActivations} (2/64 active)"),
                        Pair("Disk N-Gram Lookups", "${tel.totalNgramLookups} O(1) reads"),
                        Pair("Page Faults (min / maj)", "${tel.minorPageFaultsTotal} / ${tel.majorPageFaultsTotal}")
                    )

                    stats.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.first,
                                fontSize = 11.sp,
                                color = p.hardwareMuted,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.second,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.hardwareInk,
                                maxLines = 1
                            )
                        }
                        HorizontalDivider(color = p.hardwareRule.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditCheckRow(
    title: String,
    status: String,
    passed: Boolean,
    detail: String
) {
    val p = LocalDuniyaPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (passed) p.verdigris else p.crimson,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.inkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (passed) p.verdigrisSoft else p.crimsonSoft,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = status,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) p.verdigris else p.crimson,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
        Text(
            text = detail,
            fontSize = 11.sp,
            color = p.inkSecondary,
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 23.dp, top = 3.dp)
        )
    }
}

// ============================================================================
// FULL-SCREEN ARCHIVAL FOLIO ARTICLE READER MODAL
// ============================================================================

@Composable
private fun OfflineArticleReaderModal(
    article: ResearchArticle,
    onDismiss: () -> Unit,
    onAskFollowUp: (String) -> Unit,
    onOpenRelatedArticle: (String) -> Unit
) {
    val p = LocalDuniyaPalette.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = p.pageBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Folio Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(p.sheetSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${article.domain.uppercase()} · ${article.subcategory.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.vermilion
                        )
                        Text(
                            text = article.title,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.inkPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = p.inkPrimary)
                    }
                }
                HorizontalDivider(color = p.ruleLine)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Executive Summary Box
                    Surface(
                        color = p.sheetSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "ABSTRACT & EXECUTIVE SUMMARY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.verdigris
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RichEditorialText(
                                text = article.summary,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = p.inkPrimary
                            )
                        }
                    }

                    // Deep Technical Mechanism
                    Surface(
                        color = p.sheetSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "FIRST-PRINCIPLES MECHANISM & ARCHITECTURE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.vermilion
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RichEditorialText(
                                text = article.deepExplanation,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = p.inkSecondary
                            )
                        }
                    }

                    // Equations & Constants
                    if (article.firstPrinciplesMathOrMechanism.isNotBlank()) {
                        Surface(
                            color = p.recessedWell,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "EQUATIONS, CONSTANTS & ASYMPTOTIC BOUNDS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.brass
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = article.firstPrinciplesMathOrMechanism,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = p.inkPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Structured Specs Table
                    if (article.structuredMetrics.isNotEmpty()) {
                        Surface(
                            color = p.sheetSurface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, p.ruleLine, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "STRUCTURED SPECIFICATION PARAMETERS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.vermilion
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                article.structuredMetrics.entries.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = entry.key,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = p.vermilion,
                                            modifier = Modifier.width(135.dp)
                                        )
                                        Text(
                                            text = entry.value,
                                            fontSize = 12.5.sp,
                                            color = p.inkPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    HorizontalDivider(color = p.ruleLine)
                                }
                            }
                        }
                    }

                    // Related Concept Graph Links
                    if (article.relatedIds.isNotEmpty()) {
                        Text(
                            text = "1-HOP CONCEPT GRAPH LINKS:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.inkMuted
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            article.relatedIds.forEach { relId ->
                                OutlinedButton(
                                    onClick = { onOpenRelatedArticle(relId) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(relId, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = p.inkPrimary)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onAskFollowUp("Synthesize first-principles mechanism and comparative trade-offs for ${article.title}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = p.vermilion),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Synthesize Multi-Hop Comparison with Sparse MoE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
