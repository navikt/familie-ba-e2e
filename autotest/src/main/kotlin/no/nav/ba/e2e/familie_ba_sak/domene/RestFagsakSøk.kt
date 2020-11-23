package no.nav.ba.e2e.familie_ba_sak.domene



data class RestSøkParam(
        var personIdent: String
)

data class RestHenleggDocGen(
        var mottakerIdent: String,
        var multiselectVerdier: Array<String> ?= emptyArray(),
        var brevmal: String
)

data class RestHenleggelse(
        var årsak: String,
        var begrunnelse: String
)

enum class FagsakDeltagerRolle {
    BARN, FORELDER, UKJENT
}

data class RestFagsakDeltager(
        var navn: String?= null,
        var ident: String,
        var rolle: FagsakDeltagerRolle,
        var kjønn: Kjønn?= Kjønn.UKJENT,
        var fagsakId: Long?= null
)