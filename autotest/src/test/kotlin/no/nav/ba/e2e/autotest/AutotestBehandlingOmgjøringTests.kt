package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.*
import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.ba.e2e.mockserver.domene.RestScenarioPerson
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class AutotestBehandlingOmgjøringTests(
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient,
        @Autowired mockserverKlient: MockserverKlient) : AbstractMottakTest(mottakKlient, baSakKlient, mockserverKlient) {

    @Test
    fun `Opprett og innvilg en førstegangsbehandling, trigge autobrev`() {
        val fagsakId = oppretteOgInnvilgeFørstegangsbehandling().data!!.id
        baSakKlient.triggerAutobrev18og6år()

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(fagsakId = fagsakId).data
            println("FAGSAK: $fagsak")

            // Venter tils omregningsoppgave blitt opprettet og avsluttet.
            baSakKlient.hentFagsak(fagsakId = fagsakId).data?.behandlinger?.filter { it.årsak == BehandlingÅrsak.OMREGNING_6ÅR }
                    ?.firstOrNull()?.status == BehandlingStatus.AVSLUTTET
        }

        assertTrue(baSakKlient.hentFagsak(fagsakId = fagsakId).data?.behandlinger?.filter { it.årsak == BehandlingÅrsak.OMREGNING_6ÅR }?.size == 1)
    }

    private fun oppretteOgInnvilgeFørstegangsbehandling(): Ressurs<RestFagsak> {
        val scenario = mockserverKlient!!.lagScenario(
                RestScenario(
                        søker = RestScenarioPerson(fødselsdato = "1990-04-20", fornavn = "Mor", etternavn = "Søker"),
                        barna = listOf(RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(6).toString(),
                                                          fornavn = "Barn1",
                                                          etternavn = "Barnesen"),
                                       RestScenarioPerson(fødselsdato = LocalDate.now().minusYears(17).minusMonths(10).toString(),
                                                          fornavn = "Barn1",
                                                          etternavn = "Barnesen"))))

        val søkerId = scenario!!.søker.aktørId!!
        val barn1 = scenario.barna.first().aktørId!!
        val barn2 = scenario.barna.last().aktørId!!


        baSakKlient.opprettFagsak(søkersIdent = søkerId)
        val restFagsakMedBehandling = baSakKlient.opprettBehandling(søkersIdent = søkerId)
        val fagsakId = restFagsakMedBehandling.data!!.id

        val aktivBehandling = Utils.hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = morPersonident,
                                                                            barnasIdenter = listOf(barn1,
                                                                                                   barn2)),
                                                      bekreftEndringerViaFrontend = false)

        val restFagsakEtterRegistrertSøknad = baSakKlient.registrererSøknad(
                behandlingId = aktivBehandling!!.behandlingId,
                restRegistrerSøknad = restRegistrerSøknad
        )

        val aktivBehandlingEtterRegistrertSøknad = Utils.hentAktivBehandling(restFagsakEtterRegistrertSøknad.data!!)!!
        aktivBehandlingEtterRegistrertSøknad.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.filter { it.resultat != Resultat.OPPFYLT }?.forEach {
                baSakKlient.putVilkår(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                        vilkårId = it.id,
                        restPersonResultat =
                        RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                           vilkårResultater = listOf(it.copy(
                                                   resultat = Resultat.OPPFYLT,
                                                   periodeFom = LocalDate.now().minusMonths(4)
                                           ))))
            }
        }

        val restFagsakEtterVilkårsvurdering =
                baSakKlient.validerVilkårsvurdering(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId
                )

        val vedtaksperiode = restFagsakEtterVilkårsvurdering.data!!.behandlinger.first().vedtaksperioder.first()
        baSakKlient.leggTilVedtakBegrunnelse(
                fagsakId = fagsakId,
                vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                        fom = vedtaksperiode.periodeFom!!,
                        tom = vedtaksperiode.periodeTom,
                        vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
        )

        baSakKlient.sendTilBeslutter(fagsakId = fagsakId)
        baSakKlient.iverksettVedtak(fagsakId = fagsakId, restBeslutningPåVedtak = RestBeslutningPåVedtak(Beslutning.GODKJENT))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(fagsakId = fagsakId).data
            println("FAGSAK: $fagsak")
            fagsak?.status == FagsakStatus.LØPENDE
        }

        return baSakKlient.hentFagsak(fagsakId = fagsakId)
    }
}
