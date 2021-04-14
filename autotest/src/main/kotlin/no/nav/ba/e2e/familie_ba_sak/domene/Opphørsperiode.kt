package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

data class Opphørsperiode(
        override val periodeFom: LocalDate,
        override val periodeTom: LocalDate?,
        override val vedtaksperiodetype: Vedtaksperiodetype = Vedtaksperiodetype.OPPHØR
) : Vedtaksperiode(periodeFom, periodeTom, vedtaksperiodetype)

