package no.nav.ba.e2e.familie_ba_sak.domene

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "vedtaksperiodetype")
@JsonSubTypes(*arrayOf(
        JsonSubTypes.Type(value = Utbetalingsperiode::class, name = "UTBETALING"),
        JsonSubTypes.Type(value = Avslagsperiode::class, name = "AVSLAG"),
        JsonSubTypes.Type(value = Opphørsperiode::class, name = "OPPHØR"),
        JsonSubTypes.Type(value = FortsattInnvilgetPeriode::class, name = "FORTSATT_INNVILGET")
))
abstract class Vedtaksperiode(
        open val periodeFom: LocalDate?,
        open val periodeTom: LocalDate?,
        open val vedtaksperiodetype: Vedtaksperiodetype
)

enum class Vedtaksperiodetype {
    UTBETALING,
    OPPHØR,
    AVSLAG,
    FORTSATT_INNVILGET
}