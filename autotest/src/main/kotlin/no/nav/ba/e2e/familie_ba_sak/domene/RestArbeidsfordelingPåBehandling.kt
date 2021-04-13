package no.nav.ba.e2e.familie_ba_sak.domene

data class RestArbeidsfordelingPåBehandling(
        val behandlendeEnhetId: String,
        val behandlendeEnhetNavn: String,
        val manueltOverstyrt: Boolean = false,
)
