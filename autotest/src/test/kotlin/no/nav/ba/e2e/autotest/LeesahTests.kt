package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.hentAktivBehandling
import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingResultat
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.prosessering.domene.Status
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class LeesahTests(
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient,
        @Autowired mockserverKlient: MockserverKlient) : AbstractMottakTest(mottakKlient, baSakKlient, mockserverKlient) {

    @Test
    fun `skal sende dødsfallhendelse`() {

        val scenario = mockserverKlient!!.lagScenario(
                RestScenario(
                        søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
                        barna = listOf(RestScenarioPerson(fødselsdato = LocalDate.now().minusDays(10).toString(),
                                                          fornavn = "Barn",
                                                          etternavn = "Barnesen"))))

        val dødsfallResponse = mottakKlient.dødsfall(listOf(scenario.barna.first().ident!!, "1234567890132"))
        assertThat(dødsfallResponse.statusCode.is2xxSuccessful).isTrue
        assertThat(dødsfallResponse.body).isNotNull

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(dødsfallResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue
        assertThat(erHendelseMottatt.body).isTrue
    }

    @Test
    fun `skal sende fødselshendelse`() {

        val startVerdiMetrikkBehandlingOpprettetAutomatisk =
                baSakKlient.tellMetrikk("behandling.opprettet", Pair("type", "FØRSTEGANGSBEHANDLING"))

        val startVerdiMetrikkFødselshendelse =
                baSakKlient.tellMetrikk("behandling.logg", Pair("type", "FØDSELSHENDELSE"))
        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)

        val scenario = mockserverKlient!!.lagScenario(
                RestScenario(
                        søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
                        barna = listOf(RestScenarioPerson(fødselsdato = LocalDate.now().withDayOfMonth(1).toString(),
                                                          fornavn = "Barn",
                                                          etternavn = "Barnesen"))))

        val fødselsHendelseResponse = mottakKlient.fødsel(listOf(scenario.barna.first().ident!!, scenario.søker.aktørId!!))

        assertThat(fødselsHendelseResponse.statusCode.is2xxSuccessful).isTrue
        assertThat(fødselsHendelseResponse.body).isNotNull

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(fødselsHendelseResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue
        assertThat(erHendelseMottatt.body).isTrue

        harTaskStatus("mottaFødselshendelse", callId, Status.FERDIG)
        harTaskStatus("sendTilSak", callId, Status.FERDIG)

        erTaskOpprettetISak("behandleFødselshendelseTask", callId)


        metrikkSkalØkes("behandling.opprettet",
                        Pair("type", "FØRSTEGANGSBEHANDLING"),
                        startVerdiMetrikkBehandlingOpprettetAutomatisk)

        metrikkSkalØkes("behandling.logg", Pair("type", "FØDSELSHENDELSE"), startVerdiMetrikkFødselshendelse)

        //Sjekker at fagsak med behandlingResultat INNVILGET har blitt lagd for mor
        await.atMost(60, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    harMorBehandlingMedResultat(BehandlingResultat.INNVILGET, scenario.søker.ident!!)
                }
    }

    private fun harMorBehandlingMedResultat(resultat: BehandlingResultat, søkersIdent: String): Boolean {
        val fagsakId = baSakKlient.hentFagsakDeltager(søkersIdent)?.fagsakId ?: return false

        val aktivBehandlingEtterHendelse = hentAktivBehandling(baSakKlient.hentFagsak(fagsakId).data!!)!!
        return aktivBehandlingEtterHendelse.resultat == resultat
    }

    private fun metrikkSkalØkes(metrikkNavn: String, tagFilter: Pair<String, String>, startVerdiMetrikkFødselshendelse: Long) {
        await.atMost(60, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    baSakKlient.tellMetrikk(metrikkNavn, tagFilter) > startVerdiMetrikkFødselshendelse
                }
    }
}
