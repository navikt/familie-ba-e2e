package no.nav.ba.e2e.familie_ba_sak.domene

data class NyBehandling(
        val kategori: BehandlingKategori,
        val underkategori: BehandlingUnderkategori,
        val søkersIdent: String,
        val behandlingType: BehandlingType,
        val journalpostID: String? = null,
        val behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD,
        val skalBehandlesAutomatisk: Boolean = false,
        val barnasIdenter: List<String> = emptyList())

class NyBehandlingHendelse(
        val søkersIdent: String,
        val barnasIdenter: List<String>
)