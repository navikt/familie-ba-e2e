package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.hentAktivBehandling
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingResultat
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.LoggType
import no.nav.ba.e2e.familie_ba_sak.domene.RestBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggDocGen
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HenleggelseTest(
        @Autowired
        private val baSakKlient: FamilieBaSakKlient,

        @Autowired
        private val mockserverKlient: MockserverKlient
) {

    val restScenario = RestScenario(
            søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
            barna = listOf(
                    RestScenarioPerson(fødselsdato = LocalDate.now().minusMonths(2).toString(),
                                       fornavn = "Barn",
                                       etternavn = "Barnesen")
            )
    )

    @Test
    fun `Opprett behandling, henlegg behandling feilaktig opprettet og opprett behandling på nytt`() {
        val scenario = mockserverKlient.lagScenario(restScenario)

        val førsteBehandling = opprettBehandlingOgRegistrerSøknad(scenario)

        val responseHenlagdSøknad = baSakKlient.henleggSøknad(førsteBehandling.behandlingId,
                                                              RestHenleggelse(årsak = "FEILAKTIG_OPPRETTET",
                                                                              begrunnelse = "feilaktig opprettet"))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(responseHenlagdSøknad!!.data!!.id).data
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.aktiv == false)
        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.resultat == BehandlingResultat.HENLAGT_FEILAKTIG_OPPRETTET)

        val logger = baSakKlient.hentLogg(responseHenlagdSøknad!!.data!!.id)
        if (logger?.status == Ressurs.Status.SUKSESS) {
            assertThat(logger.data?.filter { it.type == LoggType.HENLEGG_BEHANDLING }?.size == 1)
            assertThat(logger.data?.filter { it.type == LoggType.DISTRIBUERE_BREV }?.size == 0)
        }

        val andreBehandling = opprettBehandlingOgRegistrerSøknad(scenario)
        assertThat(andreBehandling.aktiv).isTrue
    }

    @Test
    fun `Opprett behandling, hent forhåndsvising av brev, henlegg behandling søknad trukket`() {
        val scenario = mockserverKlient.lagScenario(restScenario)
        val førsteBehandling = opprettBehandlingOgRegistrerSøknad(scenario)

        val responseForhandsvis = baSakKlient.forhaandsvisHenleggelseBrev(
                behandlingId = førsteBehandling.behandlingId,
                restHenleggDocGen = RestHenleggDocGen(mottakerIdent = scenario.søker.ident!!,
                                                      brevmal = "HENLEGGE_TRUKKET_SØKNAD"))
        assertThat(responseForhandsvis?.status == Ressurs.Status.SUKSESS)

        val responseHenlagdSøknad = baSakKlient.henleggSøknad(førsteBehandling.behandlingId,
                                                              RestHenleggelse(årsak = "SØKNAD_TRUKKET",
                                                                              begrunnelse = "Søknad trukket"))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(responseHenlagdSøknad!!.data!!.id).data
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.aktiv == false)
        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.resultat == BehandlingResultat.HENLAGT_SØKNAD_TRUKKET)

        val logger = baSakKlient.hentLogg(responseHenlagdSøknad!!.data!!.id)
        if (logger?.status == Ressurs.Status.SUKSESS) {
            assertThat(logger.data?.filter { it.type == LoggType.HENLEGG_BEHANDLING }?.size == 1)
            assertThat(logger.data?.filter { it.type == LoggType.DISTRIBUERE_BREV }?.size == 1)
        }
    }

    private fun opprettBehandlingOgRegistrerSøknad(scenario: RestScenario): RestBehandling {
        val søkersIdent = scenario.søker.ident!!
        val barn1 = scenario.barna[0].ident!!
        baSakKlient.opprettFagsak(søkersIdent = søkersIdent)
        val restFagsakMedBehandling = baSakKlient.opprettBehandling(søkersIdent = søkersIdent)

        val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = søkersIdent,
                                                                            barnasIdenter = listOf(barn1)),
                                                      bekreftEndringerViaFrontend = false)
        val fagsakEtterRegistrerSøknad = baSakKlient.registrererSøknad(
                behandlingId = aktivBehandling!!.behandlingId,
                restRegistrerSøknad = restRegistrerSøknad
        )
        return hentAktivBehandling(restFagsak = fagsakEtterRegistrerSøknad.data!!)!!
    }
}
