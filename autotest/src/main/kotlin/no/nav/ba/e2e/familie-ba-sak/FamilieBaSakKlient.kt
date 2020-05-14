package no.nav.ba.e2e.`familie-ba-sak`

import no.nav.ba.e2e.`familie-ba-sak`.domene.BehandlingKategori
import no.nav.ba.e2e.`familie-ba-sak`.domene.BehandlingType
import no.nav.ba.e2e.`familie-ba-sak`.domene.BehandlingUnderkategori
import no.nav.ba.e2e.`familie-ba-sak`.domene.NyBehandling
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.net.URI

@Service
class FamilieBaSakKlient(
        @Value("\${FAMILIE_BA_SAK_API_URL}") private val baSakUrl: String,
        private val restTemplate: RestTemplate
) {

    fun opprettBehandling(søkersIdent: String): ResponseEntity<Ressurs<Any>> {
        val uri = URI.create("$baSakUrl/api/behandlinger")
        val response = restTemplate.postForEntity<Ressurs<Any>>(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING
        ))

        return response
    }
}