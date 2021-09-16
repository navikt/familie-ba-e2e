package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.nyttTilleggOrdinærSats
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.domene.LoggType
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.kontrakter.ba.infotrygd.Barn
import no.nav.familie.kontrakter.ba.infotrygd.Delytelse
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.familie.kontrakter.ba.infotrygd.Sak
import no.nav.familie.kontrakter.ba.infotrygd.Stønad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AutotestMigrering(
    @Autowired
    private val familieBaSakKlient: FamilieBaSakKlient,
    @Autowired private val mockserverKlient: MockserverKlient
) {

    @Test
    fun `Skal ikke tillatte migrering av sak som ikke er BA OR OS`() {
        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)

        assertThatThrownBy {
            familieBaSakKlient.migrering(
                lagTestScenarioForMigrering(
                    valg = "OR",
                    undervalg = "EU"
                )!!.søker.ident!!
            )
        }.hasMessageContaining("Kan kun migrere ordinære saker")
        assertThatThrownBy {
            familieBaSakKlient.migrering(
                lagTestScenarioForMigrering(
                    valg = "OR",
                    undervalg = "IB"
                )!!.søker.ident!!
            )
        }.hasMessageContaining("Kan kun migrere ordinære saker")
        assertThatThrownBy {
            familieBaSakKlient.migrering(
                lagTestScenarioForMigrering(
                    valg = "UT",
                    undervalg = "EF"
                )!!.søker.ident!!
            )
        }.hasMessageContaining("Kan kun migrere ordinære saker")
        assertThatThrownBy {
            familieBaSakKlient.migrering(
                lagTestScenarioForMigrering(
                    valg = "UT",
                    undervalg = "EU"
                )!!.søker.ident!!
            )
        }.hasMessageContaining("Kan kun migrere ordinære saker")
    }


    @Test
    fun `Skal migrere en bruker som har sak BA OR OS i infotrygd barnetrygd tjenesten`() {
        val callId = UUID.randomUUID().toString()
        MDC.put("callId", callId)

        val scenarioMorMedBarn = lagTestScenarioForMigrering()

        println("Skal migrerer ${scenarioMorMedBarn?.søker?.ident!!}")

        val migreringRessurs = familieBaSakKlient.migrering(scenarioMorMedBarn.søker.ident!!)
        println("Kall mot migrering returnerte $migreringRessurs")

        assertThat(migreringRessurs.melding).isEqualTo("Migrering påbegynt")

        erTaskOpprettetISak("iverksettMotOppdrag", callId)
        erTaskOpprettetISak("statusFraOppdrag", callId)
        erTaskOpprettetISak("ferdigstillBehandling", callId)
        erTaskOpprettetISak("publiserVedtakTask", callId)


        val saksLogg = familieBaSakKlient.hentLogg(migreringRessurs.data!!.behandlingId)
        println("Validerer saksLogg etter migrering $saksLogg")
        assertThat(saksLogg?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(
            saksLogg?.getDataOrThrow()
                ?.filter { it.type == LoggType.BEHANDLING_OPPRETTET && it.tittel == "Migrering fra infotrygd opprettet" }?.size == 1
        )
        assertThat(saksLogg?.getDataOrThrow()?.filter { it.type == LoggType.DISTRIBUERE_BREV }?.size == 0)
        assertThat(saksLogg?.getDataOrThrow()?.filter { it.type == LoggType.FERDIGSTILLE_BEHANDLING }?.size == 1)

        val fagsak = familieBaSakKlient.hentFagsak(migreringRessurs.data?.fagsakId!!)
        assertThat(fagsak.status).isEqualTo(Ressurs.Status.SUKSESS)
        val migreringBehandling = fagsak.getDataOrThrow().behandlinger.find { it.type == BehandlingType.MIGRERING_FRA_INFOTRYGD }
        println("Validerer at migreringsBehandling vedtaksperiode er satt for $migreringBehandling")
        assertThat(migreringBehandling).isNotNull
        assertThat(listOf(LocalDate.now().withDayOfMonth(1), LocalDate.now().withDayOfMonth(1).plusMonths(1))).contains(
            migreringBehandling?.vedtaksperioder?.first()?.periodeFom
        )
    }

    private fun lagTestScenarioForMigrering(valg: String? = "OR", undervalg: String? = "OS"): RestScenario? {
        val barn = mockserverKlient.lagScenario(
            RestScenario(
                søker = RestScenarioPerson(
                    fødselsdato = LocalDate.now().minusYears(1).format(DateTimeFormatter.ISO_DATE),
                    fornavn = "Barn",
                    etternavn = "Barn"
                ), barna = emptyList()
            )
        )

        val scenarioMorMedBarn = mockserverKlient.lagScenario(
            RestScenario(
                søker = RestScenarioPerson(
                    fødselsdato = "1990-04-20",
                    fornavn = "Mor",
                    etternavn = "Søker",
                    infotrygdSaker = InfotrygdSøkResponse(
                        bruker = listOf(
                            lagInfotrygdSak(
                                nyttTilleggOrdinærSats.beløp.toDouble(),
                                barn?.søker?.ident!!,
                                valg,
                                undervalg
                            )
                        ), barn = emptyList()
                    )
                ),
                barna = listOf(
                    RestScenarioPerson(
                        fødselsdato = LocalDate.now().minusYears(7).toString(),
                        fornavn = "Barn",
                        etternavn = "Barnesen",
                        ident = barn.søker.ident,
                    )
                )
            )
        )
        return scenarioMorMedBarn
    }


    private fun lagInfotrygdSak(beløp: Double, identBarn: String, valg: String? = "OR", undervalg: String? = "OS"): Sak {
        return Sak(
            stønad = Stønad(
                barn = listOf(
                    Barn(identBarn, barnetrygdTom = "000000")
                ),
                delytelse = listOf(
                    Delytelse(
                        fom = LocalDate.now(),
                        tom = null,
                        beløp = beløp,
                        typeDelytelse = "MS",
                        typeUtbetaling = "J",
                    )
                ),
                opphørsgrunn = "0"
            ),
            status = "FB",
            valg = valg,
            undervalg = undervalg
        )
    }

    protected fun erTaskOpprettetISak(type: String, callId: String) {
        try {
            await.atMost(60, TimeUnit.SECONDS)
                .withPollInterval(Duration.ofSeconds(1))
                .until {
                    sjekkOmTaskEksistererISak(type, callId)
                }
        } catch (e: ConditionTimeoutException) {
            error("type $type ikke opprettet for callId $callId")
        }
    }

    protected fun sjekkOmTaskEksistererISak(type: String, callId: String): Boolean {
        val tasker = familieBaSakKlient.hentTasker("callId", callId)
        try {
            Assertions.assertThat(tasker.body)
                .hasSizeGreaterThan(0)
                .extracting("type").contains(type)
        } catch (e: AssertionError) {
            return false
        }
        return true
    }
}
