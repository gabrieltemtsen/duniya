package com.example.duniya.ui.main

import com.example.duniya.data.DuniyaKnowledgeDatabase
import com.example.duniya.data.DuniyaResearchCorpus
import com.example.duniya.engine.HybridResearchEngine
import com.example.duniya.engine.ResearchMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun hybridResearchEngine_synthesizesMultiHopComparisonAcrossZkProofs() {
        val db = DuniyaKnowledgeDatabase(context = null)
        val engine = HybridResearchEngine(db)

        val zkPrompt = DuniyaResearchCorpus.hardBenchmarkPrompts.first()
        val report = engine.executeResearch(
            rawQuery = zkPrompt.prompt,
            requestedMode = ResearchMode.COMPARE
        )

        assertTrue(report.executiveThesis.isNotBlank())
        assertTrue(report.sourceArticles.size >= 2)
        assertNotNull(report.comparisonTable)
        assertTrue(report.comparisonTable!!.rows.isNotEmpty())
        assertTrue(report.citations.isNotEmpty())
        assertTrue(report.moeTelemetry.activeExpertsPerToken == 2)
        assertTrue(report.moeTelemetry.numExpertsTotal == 64)
        assertTrue(report.hopTrace.size == 4)
    }

    @Test
    fun hybridSearch_retrievesCrisprPrimeEditingAndHapeHaceAccurately() {
        val db = DuniyaKnowledgeDatabase(context = null)

        val bioHits = db.hybridSearch("Prime editing pegRNA reverse transcriptase vs base editing", topK = 3)
        assertFalse(bioHits.isEmpty())
        assertTrue(bioHits.first().article.id == "bio_crispr_base_prime")

        val medHits = db.hybridSearch("HAPE vs HACE nifedipine dexamethasone altitude hypoxia", topK = 3)
        assertFalse(medHits.isEmpty())
        assertTrue(medHits.first().article.id == "surv_hape_hace_altitude")
    }
}
