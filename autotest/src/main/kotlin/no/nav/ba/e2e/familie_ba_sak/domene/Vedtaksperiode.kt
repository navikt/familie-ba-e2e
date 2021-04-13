package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

abstract class Vedtaksperiode(
        open val periodeFom: LocalDate?,
        open val periodeTom: LocalDate?,
        open val vedtaksperiodetype: Vedtaksperiodetype
)

enum class Vedtaksperiodetype(val displayName: String) {
    UTBETALING(displayName = "utbetalingsperiode"),
    OPPHØR(displayName = "opphørsperiode"),
    AVSLAG(displayName = "avslagsperiode")
}