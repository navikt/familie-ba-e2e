package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.generellAssertFagsak
import no.nav.ba.e2e.commons.hentAktivBehandling
import no.nav.ba.e2e.commons.hentNåværendeEllerNesteMånedsUtbetaling
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.commons.ordinærSats
import no.nav.ba.e2e.commons.tilleggOrdinærSats
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingÅrsak
import no.nav.ba.e2e.familie_ba_sak.domene.Beslutning
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.RestBeslutningPåVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.RestPersonResultat
import no.nav.ba.e2e.familie_ba_sak.domene.RestPostVedtakBegrunnelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.familie_ba_sak.domene.Resultat
import no.nav.ba.e2e.familie_ba_sak.domene.StegType
import no.nav.ba.e2e.familie_ba_sak.domene.VedtakBegrunnelseSpesifikasjon
import no.nav.ba.e2e.familie_ba_sak.domene.Vilkår
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
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
                    RestScenarioPerson(fødselsdato = LocalDate.now().minusMonths(2).toString(),
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
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER)

        val vedtaksperiode = restFagsakEtterVilkårsvurdering.data!!.behandlinger.first().vedtaksperioder.first()
        familieBaSakKlient.leggTilVedtakBegrunnelse(
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
        val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = scenario.søker.ident!!,
                                                                           behandlingType = BehandlingType.TEKNISK_OPPHØR,
                                                                           behandlingÅrsak = BehandlingÅrsak.TEKNISK_OPPHØR)
        generellAssertFagsak(restFagsak = restFagsakMedBehandling,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.VILKÅRSVURDERING)
        assertEquals(2, restFagsakMedBehandling.data?.behandlinger?.size)

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
}
