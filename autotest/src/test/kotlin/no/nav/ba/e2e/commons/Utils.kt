package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.domene.RestBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak

object Utils {
    fun hentAktivBehandling(restFagsak: RestFagsak): RestBehandling? {
        return restFagsak.behandlinger.firstOrNull{ it.aktiv }
    }
}