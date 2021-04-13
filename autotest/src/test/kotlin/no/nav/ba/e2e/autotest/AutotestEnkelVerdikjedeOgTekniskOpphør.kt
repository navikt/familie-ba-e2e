package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.Utils
import no.nav.ba.e2e.commons.barnPersonident
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.commons.morPersonident
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.familie.kontrakter.felles.Ressurs
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.time.Duration

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AutotestEnkelVerdikjedeOgTekniskOpphør(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient
) {

    @BeforeAll
    fun init() {
        familieBaSakKlient.truncate()
    }

    @AfterAll
    fun cleanup() {
        familieBaSakKlient.truncate()
    }

    @Order(1)
    @Test
    fun `Skal opprette behandling`() {
        val søkersIdent = morPersonident
        val barn1 = barnPersonident

        val restFagsak = familieBaSakKlient.opprettFagsak(søkersIdent = søkersIdent)
        generellAssertFagsak(restFagsak = restFagsak, fagsakStatus = FagsakStatus.OPPRETTET)

        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.REGISTRERE_SØKNAD)
        assertEquals(1, restFagsakMedBehandling.data?.behandlinger?.size)

        val aktivBehandling = Utils.hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = søkersIdent,
                                                                            barnasIdenter = listOf(barn1)),
                                                      bekreftEndringerViaFrontend = false)
        val restFagsakEtterRegistrertSøknad =
                familieBaSakKlient.registrererSøknad(
                        behandlingId = aktivBehandling!!.behandlingId,
                        restRegistrerSøknad = restRegistrerSøknad
                )
        generellAssertFagsak(restFagsak = restFagsakEtterRegistrertSøknad,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.VILKÅRSVURDERING)

        val søknad = Utils.hentAktivBehandling(restFagsak = restFagsakEtterRegistrertSøknad.data!!)?.søknadsgrunnlag
        assertNotNull(søknad)
        assertEquals(søkersIdent, søknad?.søkerMedOpplysninger?.ident)

        // Godkjenner alle vilkår på førstegangsbehandling
        val aktivBehandlingEtterRegistrertSøknad = Utils.hentAktivBehandling(restFagsakEtterRegistrertSøknad.data!!)!!
        aktivBehandlingEtterRegistrertSøknad.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.filter { it.resultat != Resultat.OPPFYLT }?.forEach {
                familieBaSakKlient.putVilkår(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                        vilkårId = it.id,
                        restPersonResultat =
                        RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                           vilkårResultater = listOf(it.copy(
                                                   resultat = Resultat.OPPFYLT,
                                                   periodeFom = LocalDate.now()
                                           ))))
            }
        }

        val restFagsakEtterVilkårsvurdering =
                familieBaSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId
                )

        generellAssertFagsak(restFagsak = restFagsakEtterVilkårsvurdering,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER)

        val restFagsakEtterVedtaksbegrunnelse = familieBaSakKlient.leggTilVedtakBegrunnelse(
                fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                        fom = LocalDate.now().plusMonths(1).withDayOfMonth(1),
                        tom = null,
                        vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
        )

        val restFagsakEtterSendTilBeslutter =
                familieBaSakKlient.sendTilBeslutter(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterSendTilBeslutter,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.BESLUTTE_VEDTAK)

        val restFagsakEtterIverksetting = familieBaSakKlient.iverksettVedtak(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                                                                             restBeslutningPåVedtak = RestBeslutningPåVedtak(
                                                                                     Beslutning.GODKJENT))
        generellAssertFagsak(restFagsak = restFagsakEtterIverksetting,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.IVERKSETT_MOT_OPPDRAG)

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {

            val fagsak = familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id).data
            println("FAGSAK: $fagsak")
            fagsak?.status == FagsakStatus.LØPENDE
        }

        val restFagsakEtterBehandlingAvsluttet =
                familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterBehandlingAvsluttet,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.BEHANDLING_AVSLUTTET)
    }

    @Order(2)
    @Test
    fun `Skal teknisk opphøre behandling`() {
        val søkersIdent = morPersonident

        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent,
                                                                           behandlingType = BehandlingType.TEKNISK_OPPHØR,
                                                                           behandlingÅrsak = BehandlingÅrsak.TEKNISK_OPPHØR)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.VILKÅRSVURDERING)
        assertEquals(2, restFagsakMedBehandling.data?.behandlinger?.size)

        val aktivBehandling = Utils.hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)!!

        // Setter alle vilkår til ikke-oppfylt på løpende førstegangsbehandling
        aktivBehandling.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.forEach {
                familieBaSakKlient.putVilkår(
                        behandlingId = aktivBehandling.behandlingId,
                        vilkårId = it.id,
                        restPersonResultat =
                        RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                           vilkårResultater = listOf(it.copy(
                                                   resultat = Resultat.IKKE_OPPFYLT
                                           ))))
            }
        }

        val restFagsakEtterVilkårsvurdering =
                familieBaSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandling.behandlingId
                )
        generellAssertFagsak(restFagsak = restFagsakEtterVilkårsvurdering,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER)

        val restFagsakEtterSendTilBeslutter =
                familieBaSakKlient.sendTilBeslutter(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterSendTilBeslutter,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.BESLUTTE_VEDTAK)

        val restFagsakEtterIverksetting = familieBaSakKlient.iverksettVedtak(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                                                                             restBeslutningPåVedtak = RestBeslutningPåVedtak(
                                                                                     Beslutning.GODKJENT))
        generellAssertFagsak(restFagsak = restFagsakEtterIverksetting,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.IVERKSETT_MOT_OPPDRAG)

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {

            val fagsak = familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id).data
            println("TEKNISK OPPHØR PÅ FAGSAK: $fagsak")
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        val restFagsakEtterBehandlingAvsluttet =
                familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterBehandlingAvsluttet,
                             fagsakStatus = FagsakStatus.AVSLUTTET,
                             behandlingStegType = StegType.BEHANDLING_AVSLUTTET)
    }

    private fun generellAssertFagsak(restFagsak: Ressurs<RestFagsak>,
                                     fagsakStatus: FagsakStatus,
                                     behandlingStegType: StegType? = null) {
        assertEquals(Ressurs.Status.SUKSESS, restFagsak.status)
        assertEquals(fagsakStatus, restFagsak.data?.status)
        if (behandlingStegType != null) {
            assertEquals(behandlingStegType, Utils.hentAktivBehandling(restFagsak = restFagsak.data!!)?.steg)
        }
    }
}
