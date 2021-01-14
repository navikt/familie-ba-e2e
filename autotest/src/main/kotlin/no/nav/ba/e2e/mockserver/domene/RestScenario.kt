package no.nav.ba.e2e.mockserver.domene

data class RestScenario (
        val søker: RestScenarioPerson,
        val barna: List<RestScenarioPerson>
)