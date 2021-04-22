package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.kjørStegprosessForFGB
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingStatus
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingÅrsak
import no.nav.ba.e2e.familie_ba_sak.domene.StegType
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
class TestBehandlingOmgjøringTests(@Autowired val baSakKlient: FamilieBaSakKlient,
                                   @Autowired val mockserverKlient: MockserverKlient) {

    @Test
    fun `Opprett og innvilg en førstegangsbehandling, trigge autobrev`() {

        val scenario = mockserverKlient.lagScenario(
                RestScenario(
                        søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
                        barna = listOf(RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(6).toString(),
                                                          fornavn = "Barn1",
                                                          etternavn = "Barnesen"),
                                       RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(17).minusMonths(10).toString(),
                                                          fornavn = "Barn1",
                                                          etternavn = "Barnesen"))))

        val fagsakId = kjørStegprosessForFGB(tilSteg = StegType.BEHANDLING_AVSLUTTET,
                                             scenario = scenario,
                                             familieBaSakKlient = baSakKlient).data!!.id
        baSakKlient.triggerAutobrev18og6år()

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(fagsakId = fagsakId).data
            println("FAGSAK: $fagsak")

            // Venter tils omregningsoppgave blitt opprettet og avsluttet.
            baSakKlient.hentFagsak(fagsakId = fagsakId).data?.behandlinger?.filter { it.årsak == BehandlingÅrsak.OMREGNING_6ÅR }
                    ?.firstOrNull()?.status == BehandlingStatus.AVSLUTTET
        }

        assertTrue(baSakKlient.hentFagsak(fagsakId = fagsakId).data?.behandlinger?.filter { it.årsak == BehandlingÅrsak.OMREGNING_6ÅR }?.size == 1)
    }
}
