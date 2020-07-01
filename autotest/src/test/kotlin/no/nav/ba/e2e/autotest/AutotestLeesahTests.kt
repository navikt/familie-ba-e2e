package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.Utils
import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingResultatType
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit


class AutotestLeesahTests(
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient) : AbstractMottakTest(mottakKlient, baSakKlient) {

    @Test
    fun `skal sende dødsfallhendelse`() {
        val dødsfallResponse = mottakKlient.dødsfall(listOf(PERSONIDENT_BARN, "1234567890123"))
        assertThat(dødsfallResponse.statusCode.is2xxSuccessful).isTrue()
        assertThat(dødsfallResponse.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(dødsfallResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat(erHendelseMottatt.body).isTrue()
    }


    @Test
    fun `skal sende fødselshendelse`() {

        val startVerdiMetrikkBehandlingOpprettetAutomatisk =
                baSakKlient.tellMetrikk("behandling.opprettet.automatisk", Pair("type", "FØRSTEGANGSBEHANDLING"))
        val startVerdiMetrikkFødselshendelse =
                baSakKlient.tellMetrikk("behandling.logg", Pair("type", "FØDSELSHENDELSE"))
        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)

        val fødselsHendelseResponse = mottakKlient.fødsel(listOf(PERSONIDENT_BARN, "1234567890123"))

        assertThat(fødselsHendelseResponse.statusCode.is2xxSuccessful).isTrue()
        assertThat(fødselsHendelseResponse.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(fødselsHendelseResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat(erHendelseMottatt.body).isTrue()

        erTaskOpprettetIMottak("mottaFødselshendelse", callId)
        erTaskOpprettetIMottak("sendTilSak", callId)

        erTaskOpprettetISak("behandleFødselshendelseTask", callId)


        metrikkSkalØkes("behandling.opprettet.automatisk",
                        Pair("type", "FØRSTEGANGSBEHANDLING"),
                        startVerdiMetrikkBehandlingOpprettetAutomatisk)

        metrikkSkalØkes("behandling.logg", Pair("type", "FØDSELSHENDELSE"), startVerdiMetrikkFødselshendelse)

        //Sjekker at fagsak med behandlingResultat INNVILGET har blitt lagd for mor
        await.atMost(60, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    harMorBehandlingMedResultat(BehandlingResultatType.INNVILGET)
                }
    }

    private fun harMorBehandlingMedResultat(resultatType: BehandlingResultatType): Boolean {
        val fagsakId = baSakKlient.hentFagsakDeltager(PERSONIDENT_MOR)?.fagsakId ?: return false

        val aktivBehandlingEtterHendelse = Utils.hentAktivBehandling(baSakKlient.hentFagsak(fagsakId).data!!)!!
        return aktivBehandlingEtterHendelse.samletResultat == resultatType
    }

    private fun metrikkSkalØkes(metrikkNavn: String, tagFilter: Pair<String, String>, startVerdiMetrikkFødselshendelse: Long) {
        await.atMost(60, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    baSakKlient.tellMetrikk(metrikkNavn, tagFilter) > startVerdiMetrikkFødselshendelse
                }
    }


    companion object {
        const val PERSONIDENT_MOR = "01129400001"
        const val PERSONIDENT_BARN = "01062000001"
    }

}
