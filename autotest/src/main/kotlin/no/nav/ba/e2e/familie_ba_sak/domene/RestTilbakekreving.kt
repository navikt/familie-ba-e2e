package no.nav.ba.e2e.familie_ba_sak.domene

import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg

class RestTilbakekreving(
        val valg: Tilbakekrevingsvalg,
        val varsel: String? = null,
        val begrunnelse: String,
        val tilbakekrevingsbehandlingId: String? = null,
)
