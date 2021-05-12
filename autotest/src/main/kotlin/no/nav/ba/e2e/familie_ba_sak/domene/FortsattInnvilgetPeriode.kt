package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

class FortsattInnvilgetPeriode(
        override val periodeFom: LocalDate?,
        override val periodeTom: LocalDate?,
        override val vedtaksperiodetype: Vedtaksperiodetype = Vedtaksperiodetype.FORTSATT_INNVILGET,
        val utbetalingsperiode: Utbetalingsperiode
) : Vedtaksperiode(periodeFom, periodeTom, vedtaksperiodetype)