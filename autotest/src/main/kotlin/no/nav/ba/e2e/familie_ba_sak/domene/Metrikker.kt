package no.nav.ba.e2e.familie_ba_sak.domene

data class Metrikk(
        val measurements: List<Measurement>
)

data class Measurement(
        val statistic: String,
        val value: Long
)