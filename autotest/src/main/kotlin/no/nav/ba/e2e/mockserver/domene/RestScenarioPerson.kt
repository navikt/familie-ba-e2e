package no.nav.ba.e2e.mockserver.domene

data class RestScenarioPerson(
        val ident: String? = null, // Settes av mock-server
        val familierelasjoner: List<Familierelasjon>? = emptyList(), // Settes av mock-server
        val fødselsdato: String, //yyyy-mm-dd
        val fornavn: String,
        val etternavn: String
)

data class Familierelasjon(
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: String
)

