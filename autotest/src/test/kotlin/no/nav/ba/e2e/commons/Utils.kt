package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.domene.RestBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak
import no.nav.ba.e2e.familie_ba_sak.domene.Utbetalingsperiode
import java.time.LocalDate

fun hentAktivBehandling(restFagsak: RestFagsak): RestBehandling? {
    return restFagsak.behandlinger.firstOrNull { it.aktiv }
}

fun hentNåværendeEllerNesteMånedsUtbetaling(behandling: RestBehandling?): Int {
    val utbetalingsperioder =
            behandling?.vedtaksperioder?.filterIsInstance(Utbetalingsperiode::class.java)?.sortedBy { it.periodeFom }
    val nåværendeUtbetalingsperiode = utbetalingsperioder
            ?.firstOrNull { it.periodeFom.isBefore(LocalDate.now()) && it.periodeTom.isAfter(LocalDate.now()) }

    val nesteUtbetalingsperiode = utbetalingsperioder?.firstOrNull { it.periodeFom.isAfter(LocalDate.now()) }

    return nåværendeUtbetalingsperiode?.utbetaltPerMnd ?: nesteUtbetalingsperiode?.utbetaltPerMnd ?: 0
}