package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.familie.kontrakter.felles.Ressurs
import org.junit.jupiter.api.Assertions
import no.nav.ba.e2e.familie_ba_sak.domene.RestVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.StegType
import java.lang.IllegalStateException
import java.time.LocalDate

fun hentAktivBehandling(restFagsak: RestFagsak): RestBehandling? {
    return restFagsak.behandlinger.firstOrNull { it.aktiv }
}

fun hentAktivtVedtak(restFagsak: RestFagsak): RestVedtak? {
    return hentAktivBehandling(restFagsak)?.vedtakForBehandling?.firstOrNull { it.aktiv }
}

fun hentNåværendeEllerNesteMånedsUtbetaling(behandling: RestBehandling?): Int {
    val utbetalingsperioder =
            behandling?.vedtaksperioder?.filterIsInstance(Utbetalingsperiode::class.java)?.sortedBy { it.periodeFom }
    val nåværendeUtbetalingsperiode = utbetalingsperioder
            ?.firstOrNull { it.periodeFom.isBefore(LocalDate.now().plusDays(1)) && it.periodeTom.isAfter(LocalDate.now().minusDays(1)) }

    val nesteUtbetalingsperiode = utbetalingsperioder?.firstOrNull { it.periodeFom.isAfter(LocalDate.now()) }

    return nåværendeUtbetalingsperiode?.utbetaltPerMnd ?: nesteUtbetalingsperiode?.utbetaltPerMnd ?: 0
}

fun generellAssertFagsak(restFagsak: Ressurs<RestFagsak>,
                         fagsakStatus: FagsakStatus,
                         behandlingStegType: StegType? = null,
                         behandlingResultat: BehandlingResultat? = null) {
    if (restFagsak.status !== Ressurs.Status.SUKSESS) throw IllegalStateException("generellAssertFagsak feilet. status: ${restFagsak.status.name},  melding: ${restFagsak.melding}")
    Assertions.assertEquals(fagsakStatus, restFagsak.data?.status)
    if (behandlingStegType != null) {
        Assertions.assertEquals(behandlingStegType, hentAktivBehandling(restFagsak = restFagsak.data!!)?.steg)
    }
    if (behandlingResultat != null) {
        Assertions.assertEquals(behandlingResultat, hentAktivBehandling(restFagsak = restFagsak.data!!)?.resultat)
    }
}
