package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.domene.RestBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak
import no.nav.ba.e2e.familie_ba_sak.domene.RestVedtak

object Utils {
    fun hentAktivBehandling(restFagsak: RestFagsak): RestBehandling? {
        return restFagsak.behandlinger.firstOrNull{ it.aktiv }
    }

    fun hentAktivtVedtak(restFagsak: RestFagsak): RestVedtak? {
        return hentAktivBehandling(restFagsak)?.vedtakForBehandling?.firstOrNull { it.aktiv }
    }
}