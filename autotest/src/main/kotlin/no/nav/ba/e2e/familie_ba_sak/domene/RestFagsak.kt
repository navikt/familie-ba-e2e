package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate
import java.time.LocalDateTime

enum class FagsakStatus {
    OPPRETTET,
    LØPENDE,
    AVSLUTTET
}

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

enum class BehandlerRolle {
    UKJENT,
    VEILEDER,
    SAKSBEHANDLER,
    BESLUTTER,
    SYSTEM,
}

data class RestFagsak(
        val opprettetTidspunkt: LocalDateTime,
        val id: Long,
        val søkerFødselsnummer: String,
        val status: FagsakStatus,
        val underBehandling: Boolean,
        val behandlinger: List<RestBehandling>)

data class Logg(
        val id: Long,
        val opprettetAv: String,
        val opprettetTidspunkt: String,
        val behandlingId: Long,
        val type: LoggType,
        val tittel: String,
        val rolle: BehandlerRolle,
        val tekst: String)

data class RestPostVedtakBegrunnelse(
        val fom: LocalDate,
        val tom: LocalDate?,
        val vedtakBegrunnelse: VedtakBegrunnelseSpesifikasjon
)

