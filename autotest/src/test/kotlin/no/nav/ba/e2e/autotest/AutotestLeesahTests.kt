package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])

class AutotestLeesahTests(@Autowired
                          private val mottakKlient: FamilieBaMottakKlient
) {


    @BeforeEach
    fun init() {
        mottakKlient.truncate()
    }

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
        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)
        val fødselsHendelseResponse = mottakKlient.fødsel(listOf("12345678901", "1234567890123"))
        assertThat(fødselsHendelseResponse.statusCode.is2xxSuccessful).isTrue()
        assertThat(fødselsHendelseResponse.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(fødselsHendelseResponse.body!!, "PDL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat(erHendelseMottatt.body).isTrue()

        sjekkOmTaskEksisterer("mottaFødselshendelse", callId)

        await.atMost(120, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    sjekkOmTaskEksisterer("sendTilSak", callId)
                }
    }

    private fun sjekkOmTaskEksisterer(taskStepType: String, callId: String): Boolean {
        val tasker = mottakKlient.hentTasker("callId", callId)
        try {
            assertThat(tasker.body)
                    .hasSizeGreaterThan(0)
                    .extracting("taskStepType").contains(taskStepType)
        } catch (e: AssertionError) {
            return false
        }
        return true
    }

}
