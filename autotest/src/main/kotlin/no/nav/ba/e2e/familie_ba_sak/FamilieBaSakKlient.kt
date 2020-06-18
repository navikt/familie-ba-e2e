package no.nav.ba.e2e.familie_ba_sak

import no.nav.ba.e2e.familie_ba_mottak.Task
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import java.net.URI

@Service
class FamilieBaSakKlient(
        @Value("\${FAMILIE_BA_SAK_API_URL}") private val baSakUrl: String,
        private val restOperations: RestOperations
) : AbstractRestClient(restOperations, "familie-ba-sak") {

    fun truncate(): ResponseEntity<Ressurs<String>> {
        val uri = URI.create("$baSakUrl/api/e2e/truncate")

        return restOperations.getForEntity(uri)
    }

    fun opprettFagsak(søkersIdent: String): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker")

        return postForEntity(uri, FagsakRequest(
                personIdent = søkersIdent
        ))!!
    }

    fun opprettBehandling(søkersIdent: String): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger")

        return postForEntity(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING
        ))!!
    }

    fun registrererSøknad(behandlingId: Long, søknad: SøknadDTO): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger/$behandlingId/registrere-søknad-og-hent-persongrunnlag")

        return postForEntity(uri, søknad)!!
    }

    fun hentSøknad(behandlingId: Long): Ressurs<SøknadDTO> {
        val uri = URI.create("$baSakUrl/api/behandlinger/$behandlingId/søknad")

        return getForEntity(uri)
    }

    fun putVilkår(behandlingId: Long, vilkårId: Long, restPersonResultat: RestPersonResultat): Ressurs<List<RestPersonResultat>> {
        val uri = URI.create("$baSakUrl/api/vilkaarsvurdering/$behandlingId/$vilkårId")

        return putForEntity(uri, restPersonResultat)!!
    }

    fun validerVilkårsvurdering(behandlingId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/vilkaarsvurdering/$behandlingId/valider")

        return postForEntity(uri, "")!!
    }

    fun sendTilBeslutter(fagsakId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId/send-til-beslutter?behandlendeEnhet=9999")

        return postForEntity(uri, "")!!
    }

    fun iverksettVedtak(fagsakId: Long, restBeslutningPåVedtak: RestBeslutningPåVedtak): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId/iverksett-vedtak")

        return postForEntity(uri, restBeslutningPåVedtak)!!
    }

    fun hentFagsak(fagsakId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId")

        return getForEntity(uri)
    }

    fun hentFagsakDeltager(personIdent: String): RestFagsakDeltager ? {
        val uri = URI.create("$baSakUrl/api/fagsaker/sok")
        val ressurs = hentListeFagsakDeltager(RestSøkParam(personIdent))
        return ressurs?.data?.firstOrNull()
    }

    private fun hentListeFagsakDeltager(restSøkParam: RestSøkParam): Ressurs<List<RestFagsakDeltager>>? {
        val uri = URI.create("$baSakUrl/api/fagsaker/sok")
        return postForEntity(uri, restSøkParam)
    }

    fun hentTasker(key: String, value: String): ResponseEntity<List<Task>> {
        return restOperations.getForEntity("$baSakUrl/api/e2e/task/$key/$value")
    }

    fun tellMetrikk(metrikkNavn: String, tag: Pair<String, String>): Long {
        val metric = restOperations.getForObject("$baSakUrl/internal/metrics/$metrikkNavn?tag=${tag.first}:${tag.second}",
                                                 Metrikk::class.java)
        return metric?.measurements?.first { it.statistic == "COUNT" }?.value ?: 0
    }
}

data class Metrikk(
        val measurements: List<Measurement>
)

data class Measurement(
        val statistic: String,
        val value: Long
)