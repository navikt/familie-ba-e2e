package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.`familie-ba-sak`.FamilieBaSakKlient
import no.nav.ba.e2e.commons.randomFnr
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AutotestEnkelVerdikjede(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient
) {

    @Test
    fun `Skal opprette behandling`() {
        val søkersIdent = randomFnr()
        familieBaSakKlient.opprettBehandling(søkersIdent)
    }
}