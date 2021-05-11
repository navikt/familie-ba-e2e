package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.familie.prosessering.domene.Status
import org.assertj.core.api.Assertions
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
abstract class AbstractMottakTest(val mottakKlient: FamilieBaMottakKlient,
                                  val baSakKlient: FamilieBaSakKlient,
                                  val mockserverKlient: MockserverKlient? = null) {


    @BeforeEach
    fun init() {
        //mottakKlient.truncate()
        //baSakKlient.truncate()
        mockserverKlient?.clearOppaveCache()
        mockserverKlient?.clearFerdigstillJournapostCache()
    }

    @AfterEach
    fun tearDown() {
        mockserverKlient?.clearOppaveCache()
        mockserverKlient?.clearFerdigstillJournapostCache()
    }

    protected fun harTaskStatus(taskStepType: String, callId: String, status: Status? = null) {
        try {
            await.atMost(60, TimeUnit.SECONDS)
                    .withPollInterval(Duration.ofSeconds(1))
                    .until {
                        sjekkOmTaskIMottakHarStatus(taskStepType, callId, status)
                    }
        } catch (e: ConditionTimeoutException) {
            error("TaskStepType $taskStepType ikke opprettet for callId $callId")
        }
    }

    protected fun sjekkOmTaskIMottakHarStatus(taskStepType: String, callId: String, status: Status?): Boolean {
        println("Ser etter task av type $taskStepType, callid=$callId og status=$status")
        val tasker = mottakKlient.hentTasker("callId", callId)

        return when {
            tasker == null -> {
                println("Fant ingen task for callid $callId")
                false
            }
            tasker.firstOrNull { status == null && it.taskStepType == taskStepType } != null -> {
                println("Fant task med typen $taskStepType og callId $callId")
                true
            }
            tasker.firstOrNull { it.status == status && it.taskStepType == taskStepType } != null -> {
                println("Fant task med typen $taskStepType, callId $callId og status $status")
                true
            }
            tasker.firstOrNull { it.status == Status.FEILET && it.taskStepType == taskStepType } != null -> {
                val feiletTask = tasker.first { it.status == Status.FEILET && it.taskStepType == taskStepType }
                error("Error i task med $taskStepType og callId $callId. $feiletTask.")
            }
            else -> {
                println("Fant ingen task med $taskStepType, callId $callId og status $status i $tasker")
                false
            }
        }
    }

    protected fun erTaskOpprettetISak(taskStepType: String, callId: String) {
        try {
            await.atMost(60, TimeUnit.SECONDS)
                    .withPollInterval(Duration.ofSeconds(1))
                    .until {
                        sjekkOmTaskEksistererISak(taskStepType, callId)
                    }
        } catch (e: ConditionTimeoutException) {
            error("TaskStepType $taskStepType ikke opprettet for callId $callId")
        }
    }

    protected fun sjekkOmTaskEksistererISak(taskStepType: String, callId: String): Boolean {
        val tasker = baSakKlient.hentTasker("callId", callId)
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