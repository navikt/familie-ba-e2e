package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.familie.kontrakter.felles.Ressurs
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Assertions
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

val morPersonident = "12345678901"

fun lagSøknadDTO(søkerIdent: String,
                 barnasIdenter: List<String>): SøknadDTO {
    return SøknadDTO(
            underkategori = BehandlingUnderkategori.ORDINÆR,
            søkerMedOpplysninger = SøkerMedOpplysninger(
                    ident = søkerIdent
            ),
            barnaMedOpplysninger = barnasIdenter.map {
                BarnMedOpplysninger(
                        ident = it
                )
            },
            endringAvOpplysningerBegrunnelse = ""
    )
}

/**
 * Dette er en funksjon for å få en førstegangsbehandling til en ønsket tilstand ved test.
 * Man sender inn steg man ønsker å komme til (tilSteg), scenarioet og BaSakKlienten.
 */
fun kjørStegprosessForFGB(
        tilSteg: StegType,
        scenario: RestScenario,
        familieBaSakKlient: FamilieBaSakKlient
): Ressurs<RestFagsak> {
    val søkersIdent = scenario.søker.ident!!

    familieBaSakKlient.opprettFagsak(søkersIdent = søkersIdent)
    val restFagsakMedBehandling = familieBaSakKlient.opprettBehandling(søkersIdent = søkersIdent)

    val aktivBehandling = hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
    val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = søkersIdent,
                                                                        barnasIdenter = scenario.barna.map { it.ident!! }),
                                                  bekreftEndringerViaFrontend = false)
    val restFagsakEtterRegistrertSøknad =
            familieBaSakKlient.registrererSøknad(
                    behandlingId = aktivBehandling!!.behandlingId,
                    restRegistrerSøknad = restRegistrerSøknad
            )

    if (tilSteg == StegType.REGISTRERE_PERSONGRUNNLAG || tilSteg == StegType.REGISTRERE_SØKNAD) return restFagsakEtterRegistrertSøknad

    // Godkjenner alle vilkår på førstegangsbehandling.
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

    if (tilSteg == StegType.VILKÅRSVURDERING) return restFagsakEtterVilkårsvurdering


    val vedtaksperiode = restFagsakEtterVilkårsvurdering.data!!.behandlinger.first().vedtaksperioder.first()
    familieBaSakKlient.leggTilVedtakBegrunnelse(
            fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
            vedtakBegrunnelse = RestPostVedtakBegrunnelse(
                    fom = vedtaksperiode.periodeFom!!,
                    tom = vedtaksperiode.periodeTom,
                    vedtakBegrunnelse = VedtakBegrunnelseSpesifikasjon.INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER)
    )

    val restFagsakEtterSendTilBeslutter =
            familieBaSakKlient.sendTilBeslutter(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id)

    if (tilSteg == StegType.SEND_TIL_BESLUTTER) return restFagsakEtterSendTilBeslutter

    val restFagsakEtterIverksetting = familieBaSakKlient.iverksettVedtak(fagsakId = restFagsakEtterVilkårsvurdering.data!!.id,
                                                                         restBeslutningPåVedtak = RestBeslutningPåVedtak(
                                                                                 Beslutning.GODKJENT))
    if (tilSteg == StegType.BEHANDLING_AVSLUTTET) {
        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {

            val fagsak = familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id).data
            println("FAGSAK: $fagsak")
            fagsak?.status == FagsakStatus.LØPENDE
        }
    }

    return familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
}
