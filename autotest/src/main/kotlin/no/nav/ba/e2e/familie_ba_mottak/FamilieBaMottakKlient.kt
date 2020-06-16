package no.nav.ba.e2e.familie_ba_mottak

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.net.URI

@Service
class FamilieBaMottakKlient(
        @Value("\${FAMILIE_BA_MOTTAK_API_URL}") private val baMottakUrl: String,
        private val restOperations: RestOperations
) : AbstractRestClient(restOperations, "familie-ba-mottak") {

    fun dødsfall(identer: List<String>): ResponseEntity<String> {
        val uri = URI.create("$baMottakUrl/internal/e2e/pdl/doedsfall")

        return restOperations.postForEntity(uri, identer)
    }

    fun fødsel(identer: List<String>): ResponseEntity<String> {
        val uri = URI.create("$baMottakUrl/internal/e2e/pdl/foedsel")

        return restOperations.postForEntity(uri, identer)
    }

    fun postJournalhendelse(journalpostId: String): ResponseEntity<String> {
        val uri = URI.create("$baMottakUrl/internal/e2e/journal/$journalpostId")

        return restOperations.postForEntity(uri, null)
    }

    fun erHendelseMottatt(hendelseId: String, consumer: String): ResponseEntity<Boolean> {
        return restOperations.getForEntity("$baMottakUrl/internal/e2e/hendelselogg/$hendelseId/$consumer")
    }

    fun hentTasker(key: String, value: String): ResponseEntity<List<Task>> {
        return restOperations.getForEntity("$baMottakUrl/internal/e2e/task/$key/$value")
    }

    fun truncate(): ResponseEntity<Ressurs<String>> {
        val uri = URI.create("$baMottakUrl/internal/e2e/truncate")

        return restOperations.getForEntity(uri)
    }
}