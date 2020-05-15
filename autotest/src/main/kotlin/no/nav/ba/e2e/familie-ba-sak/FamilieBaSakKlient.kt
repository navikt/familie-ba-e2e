package no.nav.ba.e2e.`familie-ba-sak`

import no.nav.ba.e2e.`familie-ba-sak`.domene.*
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.net.URI

@Service
class FamilieBaSakKlient(
        @Value("\${FAMILIE_BA_SAK_API_URL}") private val baSakUrl: String,
        @Qualifier("jwtBearer") private val restOperations: RestOperations
) {

    fun truncate(): ResponseEntity<Ressurs<String>> {
        val uri = URI.create("$baSakUrl/api/e2e/truncate")

        return restOperations.getForEntity(uri)
    }

    fun opprettFagsak(søkersIdent: String): ResponseEntity<Ressurs<RestFagsak>> {
        val uri = URI.create("$baSakUrl/api/fagsaker")

        return restOperations.postForEntity(uri, FagsakRequest(
                personIdent = søkersIdent
        ))
    }

    fun opprettBehandling(søkersIdent: String): ResponseEntity<Ressurs<RestFagsak>> {
        val uri = URI.create("$baSakUrl/api/behandlinger")

        return restOperations.postForEntity(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING
        ))
    }
}