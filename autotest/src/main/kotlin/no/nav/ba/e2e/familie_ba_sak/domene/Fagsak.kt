package no.nav.ba.e2e.familie_ba_sak.domene

import no.nav.ba.e2e.familie_ba_sak.BehandlingKategori
import no.nav.ba.e2e.familie_ba_sak.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.BehandlingUnderkategori
import no.nav.ba.e2e.familie_ba_sak.BehandlingÅrsak

data class FagsakRequest(
        val personIdent: String?,
        val aktørId: String? = null
)

data class NyBehandling(
        val kategori: BehandlingKategori,
        val underkategori: BehandlingUnderkategori,
        val søkersIdent: String,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak
)

data class RestHenleggBehandlingInfo(
        val årsak: HenleggÅrsak,
        val begrunnelse: String
)


enum class HenleggÅrsak(val beskrivelse: String) {
    SØKNAD_TRUKKET("Søknad trukket"),
    FEILAKTIG_OPPRETTET("Behandling feilaktig opprettet"),
    FØDSELSHENDELSE_UGYLDIG_UTFALL("Behandlingen er automatisk henlagt");
}