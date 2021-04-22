package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.BarnMedOpplysninger
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingUnderkategori
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingÅrsak
import no.nav.ba.e2e.familie_ba_sak.domene.Beslutning
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.NavnOgIdent
import no.nav.ba.e2e.familie_ba_sak.domene.RestBeslutningPåVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak
import no.nav.ba.e2e.familie_ba_sak.domene.RestJournalføring
import no.nav.ba.e2e.familie_ba_sak.domene.RestJournalpostDokument
import no.nav.ba.e2e.familie_ba_sak.domene.RestPersonResultat
import no.nav.ba.e2e.familie_ba_sak.domene.RestPostVedtakBegrunnelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.familie_ba_sak.domene.Resultat
import no.nav.ba.e2e.familie_ba_sak.domene.StegType
import no.nav.ba.e2e.familie_ba_sak.domene.SøkerMedOpplysninger
import no.nav.ba.e2e.familie_ba_sak.domene.SøknadDTO
import no.nav.ba.e2e.familie_ba_sak.domene.VedtakBegrunnelseSpesifikasjon
import no.nav.ba.e2e.mockserver.domene.RestScenario
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.journalpost.LogiskVedlegg
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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

fun lagMockRestJournalføring(bruker: NavnOgIdent): RestJournalføring = RestJournalføring(
        avsender = bruker,
        bruker = bruker,
        datoMottatt = LocalDateTime.now().minusDays(10),
        journalpostTittel = "Søknad om ordinær barnetrygd",
        knyttTilFagsak = true,
        opprettOgKnyttTilNyBehandling = true,
        tilknyttedeBehandlingIder = emptyList(),
        dokumenter = listOf(
                RestJournalpostDokument(dokumentTittel = "Søknad om barnetrygd",
                                        brevkode = "mock",
                                        dokumentInfoId = "1",
                                        logiskeVedlegg = listOf(LogiskVedlegg("123", "Oppholdstillatelse")),
                                        eksisterendeLogiskeVedlegg = emptyList()
                ),
                RestJournalpostDokument(dokumentTittel = "Ekstra vedlegg",
                                        brevkode = "mock",
                                        dokumentInfoId = "2",
                                        logiskeVedlegg = listOf(LogiskVedlegg("123", "Pass")),
                                        eksisterendeLogiskeVedlegg = emptyList())
        ),
        navIdent = "09123",
        nyBehandlingstype = BehandlingType.FØRSTEGANGSBEHANDLING,
        nyBehandlingsårsak = BehandlingÅrsak.SØKNAD
)

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
