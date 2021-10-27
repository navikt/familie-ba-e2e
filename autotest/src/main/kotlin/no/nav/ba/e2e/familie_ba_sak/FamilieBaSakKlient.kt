package no.nav.ba.e2e.familie_ba_sak

import no.nav.ba.e2e.familie_ba_mottak.Task
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingKategori
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingUnderkategori
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingÅrsak
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakRequest
import no.nav.ba.e2e.familie_ba_sak.domene.Logg
import no.nav.ba.e2e.familie_ba_sak.domene.Metrikk
import no.nav.ba.e2e.familie_ba_sak.domene.MigreringResponseDto
import no.nav.ba.e2e.familie_ba_sak.domene.NyBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestBeslutningPåVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsakDeltager
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggDocGen
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestJournalføring
import no.nav.ba.e2e.familie_ba_sak.domene.RestPersonResultat
import no.nav.ba.e2e.familie_ba_sak.domene.RestPostVedtakBegrunnelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.familie_ba_sak.domene.RestSøkParam
import no.nav.ba.e2e.familie_ba_sak.domene.RestTilbakekreving
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.PersonIdent
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

    fun opprettFagsak(søkersIdent: String): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker")

        return postForEntity(uri, FagsakRequest(
                personIdent = søkersIdent
        ))
    }

    fun opprettBehandling(søkersIdent: String,
                          behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                          behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger")

        return postForEntity(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = behandlingType,
                behandlingÅrsak = behandlingÅrsak
        ))
    }
    fun hentFagsak(fagsakId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId")

        return getForEntity(uri)
    }

    fun hentFagsakDeltager(personIdent: String): RestFagsakDeltager? {
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
    fun henleggSøknad(behandlingId: Long, restHenleggelse: RestHenleggelse): Ressurs<RestFagsak>? {
        val uri = URI.create("$baSakUrl/api/behandlinger/${behandlingId}/henlegg")
        return putForEntity(uri, restHenleggelse)
    }

    fun tellMetrikk(metrikkNavn: String, tag: Pair<String, String>): Long {
        val metric = restOperations.getForObject("$baSakUrl/internal/metrics/$metrikkNavn?tag=${tag.first}:${tag.second}",
                                                 Metrikk::class.java)
        return metric?.measurements?.first { it.statistic == "COUNT" }?.value ?: 0
    }
}

