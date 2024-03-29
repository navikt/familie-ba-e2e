package no.nav.ba.e2e.familie_ba_mottak

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
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

    fun utflytting(identer: List<String>): ResponseEntity<String> {
        val uri = URI.create("$baMottakUrl/internal/e2e/pdl/utflytting")

        return restOperations.postForEntity(uri, identer)
    }

    fun postJournalhendelse(journalpostId: String): ResponseEntity<String> {
        val uri = URI.create("$baMottakUrl/internal/e2e/journal")

        return restOperations.postForEntity(uri, Journalpost(journalpostId.toLong()))
    }

    fun erHendelseMottatt(hendelseId: String, consumer: String): ResponseEntity<Boolean> {
        return restOperations.getForEntity("$baMottakUrl/internal/e2e/hendelselogg/$hendelseId/$consumer")
    }

    fun hentTasker(key: String, value: String): List<Task>? {
        val response = restOperations.getForEntity("$baMottakUrl/internal/e2e/task/$key/$value", List::class.java)
        return response.body?.mapNotNull { objectMapper.convertValue(it, Task::class.java) }
    }

    fun truncate(): ResponseEntity<Ressurs<String>> {
        val uri = URI.create("$baMottakUrl/internal/e2e/truncate")

        return restOperations.getForEntity(uri)
    }

    data class Journalpost(val journalpostId: Long)
}