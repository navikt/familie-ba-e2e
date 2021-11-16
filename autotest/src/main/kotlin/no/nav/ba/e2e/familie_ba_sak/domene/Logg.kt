package no.nav.ba.e2e.familie_ba_sak.domene

import no.nav.ba.e2e.familie_ba_sak.FagsakStatus
import java.time.LocalDateTime

data class Logg(
    val id: Long,
    val opprettetAv: String,
    val opprettetTidspunkt: String,
    val behandlingId: Long,
    val type: LoggType,
    val tittel: String,
    val tekst: String
)

enum class LoggType {
    FØDSELSHENDELSE,
    BEHANDLENDE_ENHET_ENDRET,
    BEHANDLING_OPPRETTET,
    DOKUMENT_MOTTATT,
    SØKNAD_REGISTRERT,
    VILKÅRSVURDERING,
    SEND_TIL_BESLUTTER,
    GODKJENNE_VEDTAK,
    DISTRIBUERE_BREV,
    FERDIGSTILLE_BEHANDLING,
    OPPLYSNINGSPLIKT,
    HENLEGG_BEHANDLING,
}
