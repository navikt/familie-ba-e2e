package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.`familie-ba-sak`.FamilieBaSakKlient
import no.nav.ba.e2e.`familie-ba-sak`.domene.FagsakStatus
import no.nav.ba.e2e.commons.morPersonident
import no.nav.familie.kontrakter.felles.Ressurs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest(classes = [ApplicationConfig::class])
class AutotestEnkelVerdikjede(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient
) {

    @Test
    fun `Skal opprette behandling`() {
        val melding = familieBaSakKlient.truncate()
        assertEquals(HttpStatus.OK, melding.statusCode)

        val søkersIdent = morPersonident
        val restFagsak = familieBaSakKlient.opprettFagsak(søkersIdent = søkersIdent)
        assertNotNull(restFagsak.body)
        assertEquals(Ressurs.Status.SUKSESS, restFagsak.body?.status)
        assertEquals(FagsakStatus.OPPRETTET, restFagsak.body?.data?.status)

        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent)
        assertNotNull(restFagsakMedBehandling.body)
        assertEquals(Ressurs.Status.SUKSESS, restFagsakMedBehandling.body?.status)
        assertEquals(1, restFagsakMedBehandling.body?.data?.behandlinger?.size)

    }

    @Test
    fun `Skal manuelt journalføre og sjekke at behandlingen blir opprettet automatisk`() {

    }
}