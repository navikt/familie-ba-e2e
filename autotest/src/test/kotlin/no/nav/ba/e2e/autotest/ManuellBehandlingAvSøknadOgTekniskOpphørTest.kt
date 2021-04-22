package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.*
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ManuellBehandlingAvSøknadOgTekniskOpphørTest(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient,

        @Autowired
        private val mockserverKlient: MockserverKlient
) {

    val eldsteBarnAlder = 8L
    var scenario = RestScenario(
            søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
            barna = listOf(
                    RestScenarioPerson(fødselsdato = LocalDate.now().minusMonths(4).toString(),
                                       fornavn = "Yngste",
                                       etternavn = "Barnesen"),
                    RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(eldsteBarnAlder).toString(),
                                       fornavn = "Eldste",
                                       etternavn = "Barnesen"),
                    /**
                     * TODO kommentere ut denne når delvis innvilget er fullt støttet i ba-sak.
                     * RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(19).toString(),
                    fornavn = "Barn over 18 år",
                    etternavn = "Barnesen")*/
            )
    )

    @Order(1)
    @Test
    fun `Skal behandle manuelt opprettet behandling på 2 innvilgede barn`() {
        scenario = mockserverKlient.lagScenario(scenario)
        val søkersIdent = scenario.søker.ident!!
        val barn1 = scenario.barna[0]
        val barn2 = scenario.barna[1]

        val restFagsak = familieBaSakKlient.opprettFagsak(søkersIdent = søkersIdent)
        generellAssertFagsak(restFagsak = restFagsak, fagsakStatus = FagsakStatus.OPPRETTET)

        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.REGISTRERE_SØKNAD)
        assertEquals(1, restFagsakMedBehandling.data?.behandlinger?.size)

        val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = søkersIdent,
                                                                            barnasIdenter = scenario.barna.map { it.ident!! }),
                                                      bekreftEndringerViaFrontend = false)
        val restFagsakEtterRegistrertSøknad =
                familieBaSakKlient.registrererSøknad(
                        behandlingId = aktivBehandling!!.behandlingId,
                        restRegistrerSøknad = restRegistrerSøknad
                )
        generellAssertFagsak(restFagsak = restFagsakEtterRegistrertSøknad,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.VILKÅRSVURDERING)

        val søknad = hentAktivBehandling(restFagsak = restFagsakEtterRegistrertSøknad.data!!)?.søknadsgrunnlag
        assertNotNull(søknad)
        assertEquals(søkersIdent, søknad?.søkerMedOpplysninger?.ident)

        // Godkjenner alle vilkår på førstegangsbehandling. Barn over 18 år vil få avslag og hele resultatet blir delvis innvilget
        val aktivBehandlingEtterRegistrertSøknad = hentAktivBehandling(restFagsakEtterRegistrertSøknad.data!!)!!
        aktivBehandlingEtterRegistrertSøknad.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.filter { it.resultat != Resultat.OPPFYLT }?.forEach {
                if (restPersonResultat.personIdent == barn1.ident && it.vilkårType == Vilkår.BOR_MED_SØKER) {
                    // Testen kjører grønt også uten denne if-testen. TODO: Vurdere å utvide med flere asserts eller fjerne.
                    familieBaSakKlient.putVilkår(
                            behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                            vilkårId = it.id,
                            restPersonResultat =
                            RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                               vilkårResultater = listOf(it.copy(
                                                       resultat = Resultat.OPPFYLT,
                                                       periodeFom = LocalDate.parse(barn1.fødselsdato)
                                               ))))
                } else if (restPersonResultat.personIdent == barn2.ident && it.vilkårType == Vilkår.BOR_MED_SØKER) {
                    // Testen kjører grønt også uten denne if-testen. TODO: Vurdere å utvide med flere asserts eller fjerne.
                    familieBaSakKlient.putVilkår(
                            behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                            vilkårId = it.id,
                            restPersonResultat =
                            RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                               vilkårResultater = listOf(it.copy(
                                                       resultat = Resultat.OPPFYLT,
                                                       periodeFom = LocalDate.parse(barn2.fødselsdato),
                                                       periodeTom = LocalDate.parse(barn2.fødselsdato)
                                                               .plusYears(eldsteBarnAlder / 2),
                                               ))))

                    familieBaSakKlient.putVilkår(
                            behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                            vilkårId = it.id,
                            restPersonResultat =
                            RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                               vilkårResultater = listOf(it.copy(
                                                       resultat = Resultat.OPPFYLT,
                                                       periodeFom = LocalDate.parse(barn2.fødselsdato)
                                                               .plusYears((eldsteBarnAlder / 2) + 1),
                                               ))))
                } else {
                    familieBaSakKlient.putVilkår(
                            behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                            vilkårId = it.id,
                            restPersonResultat =
                            RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                               vilkårResultater = listOf(it.copy(
                                                       resultat = Resultat.OPPFYLT,
                                                       periodeFom = LocalDate.now().minusMonths(2)
                                               ))))
                }
            }
        }

        val restFagsakEtterVilkårsvurdering =
                familieBaSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId
                )

        generellAssertFagsak(restFagsak = restFagsakEtterVilkårsvurdering,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER,
                             behandlingResultat = BehandlingResultat.INNVILGET)

        val vedtaksperiode = restFagsakEtterVilkårsvurdering.data!!.behandlinger.first().vedtaksperioder.first()
        val restFagsakEtterVedtaksbegrunnelser = familieBaSakKlient.leggTilVedtakBegrunnelse(
                fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                        fom = vedtaksperiode.periodeFom!!,
                        tom = vedtaksperiode.periodeTom,
                        vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
        )

        assertEquals(ordinærSats.beløp + tilleggOrdinærSats.beløp,
                     hentNåværendeEllerNesteMånedsUtbetaling(
                             behandling = hentAktivBehandling(restFagsakEtterVilkårsvurdering.data!!)
                     )
        )

        var vedtaksbrevFørstegangsvedtak = familieBaSakKlient.genererOgHentVedtaksbrev(
                hentAktivtVedtak(restFagsak = restFagsakEtterVedtaksbegrunnelser.data!!)!!.id)
        Assertions.assertThat(vedtaksbrevFørstegangsvedtak?.status == Ressurs.Status.SUKSESS)

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

        vedtaksbrevFørstegangsvedtak = familieBaSakKlient.hentVedtaksbrev(
                hentAktivtVedtak(restFagsak = restFagsakEtterIverksetting.data!!)!!.id)
        Assertions.assertThat(vedtaksbrevFørstegangsvedtak?.status == Ressurs.Status.SUKSESS)
    }

    @Order(2)
    @Test
    fun `Revurdering`() {
        val søkersIdent = scenario.søker.ident!!

        val restFagsak = familieBaSakKlient.opprettFagsak(søkersIdent = søkersIdent)

        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent,
                                                                           behandlingType = BehandlingType.REVURDERING,
                                                                           behandlingÅrsak = BehandlingÅrsak.NYE_OPPLYSNINGER)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.VILKÅRSVURDERING)
        assertEquals(2, restFagsakMedBehandling.data?.behandlinger?.size)

        val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)!!

        // Setter alle vilkår til ikke-oppfylt på løpende førstegangsbehandling
        aktivBehandling.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.forEach {
                if (restPersonResultat.personIdent == scenario.barna[0].ident && it.vilkårType == Vilkår.BOR_MED_SØKER) {
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
        }

        val restFagsakEtterVilkårsvurdering =
                familieBaSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandling.behandlingId
                )
        generellAssertFagsak(restFagsak = restFagsakEtterVilkårsvurdering,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER,
                             behandlingResultat = BehandlingResultat.ENDRET)


        val vedtaksperiode = restFagsakEtterVilkårsvurdering.data!!.behandlinger.first().vedtaksperioder.first()
        val restFagsakEtterVedtaksbegrunnelser = familieBaSakKlient.leggTilVedtakBegrunnelse(
                fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                        fom = vedtaksperiode.periodeFom!!,
                        tom = vedtaksperiode.periodeTom,
                        vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
        )

        var vedtaksbrevRevurderingEndret = familieBaSakKlient.genererOgHentVedtaksbrev(
                hentAktivtVedtak(restFagsak = restFagsakEtterVedtaksbegrunnelser.data!!)!!.id)
        Assertions.assertThat(vedtaksbrevRevurderingEndret?.status == Ressurs.Status.SUKSESS)

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
            println("FAGSAK: $fagsak")
            hentAktivBehandling(restFagsak = fagsak!!)?.steg == StegType.BEHANDLING_AVSLUTTET
        }

        val restFagsakEtterBehandlingAvsluttet =
                familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterBehandlingAvsluttet,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.BEHANDLING_AVSLUTTET)

        vedtaksbrevRevurderingEndret = familieBaSakKlient.hentVedtaksbrev(
                hentAktivtVedtak(restFagsak = restFagsakEtterIverksetting.data!!)!!.id)
        Assertions.assertThat(vedtaksbrevRevurderingEndret?.status == Ressurs.Status.SUKSESS)

    }


    @Order(3)
    @Test
    fun `Skal teknisk opphøre behandling`() {
        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = scenario.søker.ident!!,
                                                                           behandlingType = BehandlingType.TEKNISK_OPPHØR,
                                                                           behandlingÅrsak = BehandlingÅrsak.TEKNISK_OPPHØR)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.VILKÅRSVURDERING)
        assertEquals(3, restFagsakMedBehandling.data?.behandlinger?.size)

        val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)!!

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
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER,
                             behandlingResultat = BehandlingResultat.OPPHØRT)

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
                                     behandlingStegType: StegType? = null,
                                     behandlingResultat: BehandlingResultat? = null) {

        assertEquals(Ressurs.Status.SUKSESS, restFagsak.status)
        assertEquals(fagsakStatus, restFagsak.data?.status)
        if (behandlingStegType != null) {
            assertEquals(behandlingStegType, hentAktivBehandling(restFagsak = restFagsak.data!!)?.steg)
        }
        if (behandlingResultat != null) {
            assertEquals(behandlingResultat, hentAktivBehandling(restFagsak = restFagsak.data!!)?.resultat)
        }
    }
}
