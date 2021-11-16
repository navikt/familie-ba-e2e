package no.nav.ba.e2e.familie_ba_sak

import no.nav.ba.e2e.familie_ba_mottak.Task
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakRequest
import no.nav.ba.e2e.familie_ba_sak.domene.Logg
import no.nav.ba.e2e.familie_ba_sak.domene.Metrikk
import no.nav.ba.e2e.familie_ba_sak.domene.MigreringResponseDto
import no.nav.ba.e2e.familie_ba_sak.domene.NyBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggBehandlingInfo
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

    fun opprettFagsak(søkersIdent: String): Ressurs<RestMinimalFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker")

        return postForEntity(uri, FagsakRequest(
                personIdent = søkersIdent
        ))
    }

    fun opprettBehandling(søkersIdent: String,
                          behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                          behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD): Ressurs<RestMinimalFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger")

        return postForEntity(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = behandlingType,
                behandlingÅrsak = behandlingÅrsak
        ))
    }

    fun hentMinimalFagsak(fagsakId: Long): Ressurs<RestMinimalFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId")

        return getForEntity(uri)
    }

    fun henleggSøknad(behandlingId: Long, restHenleggelse: RestHenleggBehandlingInfo): Ressurs<RestMinimalFagsak>? {
        val uri = URI.create("$baSakUrl/api/behandlinger/${behandlingId}/henlegg")
        return putForEntity(uri, restHenleggelse)
    }

    fun hentLogg(behandlingId: Long): Ressurs<List<Logg>> {
        val uri = URI.create("$baSakUrl/api/logg/${behandlingId}")
        return getForEntity(uri)
    }


    fun hentTasker(key: String, value: String): ResponseEntity<List<Task>> {
        return restOperations.getForEntity("$baSakUrl/api/e2e/task/$key/$value")
    }


    fun tellMetrikk(metrikkNavn: String, tag: Pair<String, String>): Long {
        val metric = restOperations.getForObject("$baSakUrl/internal/metrics/$metrikkNavn?tag=${tag.first}:${tag.second}",
                                                 Metrikk::class.java)
        return metric?.measurements?.first { it.statistic == "COUNT" }?.value ?: 0
    }

    fun migrering(ident: String): Ressurs<MigreringResponseDto> {
        val uri = URI.create("$baSakUrl/api/migrering")

        return postForEntity(uri, PersonIdent(ident))
    }
}

