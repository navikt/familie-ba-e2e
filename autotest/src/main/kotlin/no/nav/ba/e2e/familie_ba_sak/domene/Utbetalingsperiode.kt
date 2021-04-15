package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

/**
 * Dataklasser som brukes til frontend og backend når man jobber med vertikale utbetalingsperioder
 */
data class Utbetalingsperiode(
        override val periodeFom: LocalDate,
        override val periodeTom: LocalDate,
        override val vedtaksperiodetype: Vedtaksperiodetype = Vedtaksperiodetype.UTBETALING,
        val utbetalingsperiodeDetaljer: List<UtbetalingsperiodeDetalj>,
        val ytelseTyper: List<YtelseType>,
        val antallBarn: Int,
        val utbetaltPerMnd: Int,
) : Vedtaksperiode(periodeFom, periodeTom, vedtaksperiodetype)

data class UtbetalingsperiodeDetalj(
        val person: RestPerson,
        val ytelseType: YtelseType,
        val utbetaltPerMnd: Int,
)

enum class YtelseType(val klassifisering: String) {
    ORDINÆR_BARNETRYGD("BATR"),
    UTVIDET_BARNETRYGD("BAUT"),
    SMÅBARNSTILLEGG("BATRSMA"),
    EØS("BATR"),
    MANUELL_VURDERING("BATR")
}

