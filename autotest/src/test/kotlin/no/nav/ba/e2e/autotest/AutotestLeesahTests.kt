package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import java.util.*


class AutotestLeesahTests(
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient) : AbstractMottakTest(mottakKlient, baSakKlient) {

    @Test
    fun `skal sende dødsfallhendelse`() {
        val dødsfallResponse = mottakKlient.dødsfall(listOf("12345678901", "1234567890123"))
        assertThat(dødsfallResponse.statusCode.is2xxSuccessful).isTrue()
        assertThat(dødsfallResponse.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(dødsfallResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat(erHendelseMottatt.body).isTrue()
    }


    @Test
    fun `skal sende fødselshendelse`() {

        val metric = baSakKlient.hentMetric().body
        val count = metric!!.measurements.first { it.statistic == "COUNT" }.value


        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)
        val fødselsHendelseResponse = mottakKlient.fødsel(listOf("12345678901", "1234567890123"))
        assertThat(fødselsHendelseResponse.statusCode.is2xxSuccessful).isTrue()
        assertThat(fødselsHendelseResponse.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(fødselsHendelseResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat(erHendelseMottatt.body).isTrue()

        erTaskOpprettetIMottak("mottaFødselshendelse", callId)
        erTaskOpprettetIMottak("sendTilSak", callId)

        erTaskOpprettetISak("behandleFødselshendelseTask", callId)

    }


}
