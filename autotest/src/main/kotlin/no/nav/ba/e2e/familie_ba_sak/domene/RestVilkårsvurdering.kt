package no.nav.ba.e2e.familie_ba_sak.domene

data class RestVilkårsvurdering(
        val personResultater: List<RestPersonResultat>
)

data class RestBeslutningPåVedtak(
        val beslutning: Beslutning,
        val begrunnelse: String? = null
)

enum class Beslutning {
    GODKJENT, UNDERKJENT;
    fun erGodkjent() = this == GODKJENT
}