package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.generellAssertFagsak
import no.nav.ba.e2e.commons.hentAktivBehandling
import no.nav.ba.e2e.commons.hentNåværendeEllerNesteMånedsUtbetaling
import no.nav.ba.e2e.commons.lagMockRestJournalføring
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.commons.tilleggOrdinærSats
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.Beslutning
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.NavnOgIdent
import no.nav.ba.e2e.familie_ba_sak.domene.RestBeslutningPåVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.RestPersonResultat
import no.nav.ba.e2e.familie_ba_sak.domene.RestPostVedtakBegrunnelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.familie_ba_sak.domene.Resultat
import no.nav.ba.e2e.familie_ba_sak.domene.StegType
import no.nav.ba.e2e.familie_ba_sak.domene.VedtakBegrunnelseSpesifikasjon
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.kontrakter.felles.Ressurs
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
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
class ManuellBehandlingAvJournalfortForstegangssoknad(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient,

        @Autowired
        private val mockserverKlient: MockserverKlient
) {

    @Test
    fun `Manuell behandling av journalført førstegangssøknad`() {
        val scenario = mockserverKlient.lagScenario(RestScenario(
                søker = RestScenarioPerson(fødselsdato = "1996-01-12", fornavn = "Mor", etternavn = "Søker"),
                barna = listOf(
                        RestScenarioPerson(fødselsdato = LocalDate.now().minusMonths(2).toString(),
                                           fornavn = "Barn",
                                           etternavn = "Barnesen")
                )
        ))

        val fagsakId: Ressurs<String> = familieBaSakKlient.journalfør(
                journalpostId = "1234",
                oppgaveId = "5678",
                journalførendeEnhet = "4833",
                restJournalføring = lagMockRestJournalføring(bruker = NavnOgIdent(
                        navn = scenario.søker.navn,
                        id = scenario.søker.ident!!
                ))
        )

        Assertions.assertEquals(Ressurs.Status.SUKSESS, fagsakId.status)

        val restFagsakEtterJournalføring = familieBaSakKlient.hentFagsak(fagsakId = fagsakId.data?.toLong()!!)
        generellAssertFagsak(restFagsak = restFagsakEtterJournalføring,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.REGISTRERE_SØKNAD)

        val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakEtterJournalføring.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = scenario.søker.ident!!,
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


        // Godkjenner alle vilkår på førstegangsbehandling. Barn over 18 år vil få avslag og hele resultatet blir delvis innvilget
        val aktivBehandlingEtterRegistrertSøknad = hentAktivBehandling(restFagsakEtterRegistrertSøknad.data!!)!!
        aktivBehandlingEtterRegistrertSøknad.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.filter { it.resultat != Resultat.OPPFYLT }?.forEach {

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

        val restFagsakEtterVilkårsvurdering =
                familieBaSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId
                )

        //Midlertidig løsning for å håndtere med og uten simuleringssteg.
        // TODO: Fjern når toggelen bruk_simulering er fjernet fra familie-ba-sak.
        val behandlingEtterVilkårsvurdering = hentAktivBehandling(restFagsak = restFagsakEtterVilkårsvurdering.data!!)!!
        var restFagsakEtterVurderTilbakekreving = restFagsakEtterVilkårsvurdering

        if (behandlingEtterVilkårsvurdering.steg == StegType.VURDER_TILBAKEKREVING) {
            generellAssertFagsak(restFagsak = restFagsakEtterVilkårsvurdering,
                                 fagsakStatus = FagsakStatus.OPPRETTET,
                                 behandlingStegType = StegType.VURDER_TILBAKEKREVING)

            restFagsakEtterVurderTilbakekreving = familieBaSakKlient.lagreTilbakekrevingOgGåVidereTilNesteSteg(
                    behandlingEtterVilkårsvurdering.behandlingId,
                    null)
        }

        generellAssertFagsak(restFagsak = restFagsakEtterVurderTilbakekreving,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.SEND_TIL_BESLUTTER)

        val vedtaksperiode = restFagsakEtterVurderTilbakekreving.data!!.behandlinger.first().vedtaksperioder.first()
        familieBaSakKlient.leggTilVedtakBegrunnelse(
                fagsakId = restFagsakEtterVurderTilbakekreving.data!!.id,
                vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                        fom = vedtaksperiode.periodeFom!!,
                        tom = vedtaksperiode.periodeTom,
                        vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
        )

        Assertions.assertEquals(tilleggOrdinærSats.beløp,
                                hentNåværendeEllerNesteMånedsUtbetaling(
                                        behandling = hentAktivBehandling(restFagsakEtterVurderTilbakekreving.data!!)
                                )
        )

        val restFagsakEtterSendTilBeslutter =
                familieBaSakKlient.sendTilBeslutter(fagsakId = restFagsakEtterVurderTilbakekreving.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterSendTilBeslutter,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.BESLUTTE_VEDTAK)

        val restFagsakEtterIverksetting = familieBaSakKlient.iverksettVedtak(fagsakId = restFagsakEtterVurderTilbakekreving.data!!.id,
                                                                             restBeslutningPåVedtak = RestBeslutningPåVedtak(
                                                                                     Beslutning.GODKJENT))
        generellAssertFagsak(restFagsak = restFagsakEtterIverksetting,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.IVERKSETT_MOT_OPPDRAG)

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {

            val fagsak = familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id).data
            println("FAGSAK ved manuell journalføring: $fagsak")
            fagsak?.status == FagsakStatus.LØPENDE
        }

        val restFagsakEtterBehandlingAvsluttet =
                familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterBehandlingAvsluttet,
                             fagsakStatus = FagsakStatus.LØPENDE,
                             behandlingStegType = StegType.BEHANDLING_AVSLUTTET)
    }
}