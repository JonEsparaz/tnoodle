package org.worldcubeassociation.tnoodle.server.webscrambles.wcif

import org.worldcubeassociation.tnoodle.server.model.EventData
import org.worldcubeassociation.tnoodle.server.webscrambles.pdf.*
import org.worldcubeassociation.tnoodle.server.webscrambles.wcif.model.ActivityCode
import org.worldcubeassociation.tnoodle.server.webscrambles.wcif.model.extension.FmcExtension
import org.worldcubeassociation.tnoodle.server.webscrambles.wcif.model.ScrambleSet
import org.worldcubeassociation.tnoodle.server.webscrambles.wcif.model.extension.SheetCopyCountExtension
import java.time.LocalDate
import java.util.*

data class CompetitionDrawingData(val competitionTitle: String, val scrambleSheets: List<ScrambleDrawingData>)

data class ScrambleDrawingData(val scrambleSet: ScrambleSet, val activityCode: ActivityCode, val watermark: String? = null, val hasGroupID: Boolean = true) {
    val isFmc: Boolean
        get() = scrambleSet.findExtension<FmcExtension>()
            ?.isFmc ?: (activityCode.eventModel == EventData.THREE_FM)

    val numCopies: Int
        get() = scrambleSet.findExtension<SheetCopyCountExtension>()
            ?.numCopies ?: 1

    fun createPdf(creationDate: LocalDate, versionTag: String, sheetTitle: String, locale: Locale): PdfContent {
        assert(scrambleSet.scrambles.isNotEmpty())

        if (isFmc) {
            // We don't watermark the FMC sheets because they already have
            // the competition name on them. So we encrypt directly.
            return FmcSolutionSheet(scrambleSet, activityCode, sheetTitle, locale, hasGroupID)
        }

        val genericSheet = GeneralScrambleSheet(scrambleSet, activityCode) // encrypt when watermarking
        return WatermarkPdfWrapper(genericSheet, compileTitleString(locale), creationDate, versionTag, sheetTitle, watermark)
    }

    fun compileTitleString(locale: Locale, includeEvent: Boolean = true): String = activityCode.compileTitleString(locale, includeEvent, hasGroupID)
}
