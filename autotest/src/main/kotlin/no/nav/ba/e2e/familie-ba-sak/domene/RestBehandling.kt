package no.nav.ba.e2e.`familie-ba-sak`.domene

import java.time.LocalDateTime

enum class BehandlingStatus {
    OPPRETTET,
    UNDERKJENT_AV_BESLUTTER,
    SENDT_TIL_BESLUTTER,
    GODKJENT,
    SENDT_TIL_IVERKSETTING,
    IVERKSATT,
    FERDIGSTILT
}


data class RestBehandling(val aktiv: Boolean,
                          val behandlingId: Long,
                          val type: BehandlingType,
                          val status: BehandlingStatus,
                          val kategori: BehandlingKategori,
                          val endretAv: String)
