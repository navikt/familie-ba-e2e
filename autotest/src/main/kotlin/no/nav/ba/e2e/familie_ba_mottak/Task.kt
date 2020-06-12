package no.nav.ba.e2e.familie_ba_mottak

import no.nav.familie.prosessering.domene.Avvikstype
import no.nav.familie.prosessering.domene.Status
import java.time.LocalDateTime
import java.util.*

data class Task(
        val id: Long? = null,
        val payload: String,
        var status: Status,
        var avvikstype: Avvikstype? = null,
        var opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),
        var triggerTid: LocalDateTime? = null,
        val taskStepType: String,
        val metadata: Properties = Properties()
)