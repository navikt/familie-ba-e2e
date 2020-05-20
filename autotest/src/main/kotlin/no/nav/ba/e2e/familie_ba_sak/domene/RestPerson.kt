package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

data class RestPerson(
        val type: PersonType,
        val fødselsdato: LocalDate?,
        val personIdent: String,
        val navn: String,
        val kjønn: Kjønn
)


enum class PersonType {
    SØKER, ANNENPART, BARN
}

enum class Kjønn {
    MANN, KVINNE, UKJENT
}