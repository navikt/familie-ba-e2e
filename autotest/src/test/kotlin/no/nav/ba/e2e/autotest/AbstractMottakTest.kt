package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import org.assertj.core.api.Assertions
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
abstract class AbstractMottakTest(val mottakKlient: FamilieBaMottakKlient) {


    @BeforeEach
    fun init() {
        mottakKlient.truncate()
    }

    protected fun erTaskOpprettet(taskStepType: String, callId: String) {
        try {
            await.atMost(60, TimeUnit.SECONDS)
                    .withPollInterval(Duration.ofSeconds(1))
                    .until {
                        sjekkOmTaskEksisterer(taskStepType, callId)
                    }
        } catch (e: ConditionTimeoutException) {
            error("TaskStepType $taskStepType ikke opprettet for callId $callId")
        }
    }

    protected fun sjekkOmTaskEksisterer(taskStepType: String, callId: String): Boolean {
        val tasker = mottakKlient.hentTasker("callId", callId)
        try {
            Assertions.assertThat(tasker.body)
                    .hasSizeGreaterThan(0)
                    .extracting("taskStepType").contains(taskStepType)
        } catch (e: AssertionError) {
            return false
        }
        return true
    }
}