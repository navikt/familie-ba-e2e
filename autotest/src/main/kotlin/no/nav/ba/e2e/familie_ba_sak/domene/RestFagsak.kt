package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDateTime

enum class FagsakStatus {
    OPPRETTET, LØPENDE, AVSLUTTET
}

data class RestFagsak(
        val opprettetTidspunkt: LocalDateTime,
        val id: Long,
        val søkerFødselsnummer: String,
        val status: FagsakStatus,
        val behandlinger: List<RestBehandling>)
