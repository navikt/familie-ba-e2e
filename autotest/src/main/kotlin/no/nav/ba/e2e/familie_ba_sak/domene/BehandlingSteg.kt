package no.nav.ba.e2e.familie_ba_sak.domene

enum class StegType {
    HENLEGG_SØKNAD,
    REGISTRERE_PERSONGRUNNLAG,
    REGISTRERE_SØKNAD,
    VILKÅRSVURDERING,
    SIMULERING,
    VURDER_TILBAKEKREVING,
    SEND_TIL_BESLUTTER,
    BESLUTTE_VEDTAK,
    IVERKSETT_MOT_OPPDRAG,
    VENTE_PÅ_STATUS_FRA_ØKONOMI,
    IVERKSETT_MOT_FAMILIE_TILBAKE,
    JOURNALFØR_VEDTAKSBREV,
    DISTRIBUER_VEDTAKSBREV,
    FERDIGSTILLE_BEHANDLING,
    BEHANDLING_AVSLUTTET,
}

enum class BehandlingStegStatus(val navn: String, val beskrivelse: String) {
    IKKE_UTFØRT("IKKE_UTFØRT", "Steget er ikke utført"),
    UTFØRT("UTFØRT", "Utført")
}