package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.Utils
import no.nav.ba.e2e.commons.barnPersonident
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.commons.morPersonident
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.familie.kontrakter.felles.Ressurs
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [ApplicationConfig::class])
class AutotestEnkelVerdikjede(
        @Autowired
        private val familieBaSakKlient: FamilieBaSakKlient
) {

    @BeforeEach
    fun init() {
        familieBaSakKlient.truncate()
    }

    @AfterEach
    fun cleanup() {
        familieBaSakKlient.truncate()
    }

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
                                                                            annenPartIdent = "",
                                                                            typeSøker = TypeSøker.TREDJELANDSBORGER,
                                                                            barnasIdenter = listOf(barn1)), bekreftEndringerViaFrontend = false)
        val restFagsakEtterRegistrertSøknad =
                familieBaSakKlient.registrererSøknad(
                        behandlingId = aktivBehandling!!.behandlingId,
                        restRegistrerSøknad = restRegistrerSøknad
                )
        generellAssertFagsak(restFagsak = restFagsakEtterRegistrertSøknad,
                             fagsakStatus = FagsakStatus.OPPRETTET,
                             behandlingStegType = StegType.VILKÅRSVURDERING)

        val søknad = familieBaSakKlient.hentSøknad(behandlingId = aktivBehandling.behandlingId)
        assertEquals(søkersIdent, søknad.data?.søkerMedOpplysninger?.ident)


        val aktivBehandlingEtterRegistrertSøknad = Utils.hentAktivBehandling(restFagsakEtterRegistrertSøknad.data!!)!!
        aktivBehandlingEtterRegistrertSøknad.personResultater.forEach { restPersonResultat ->
            restPersonResultat.vilkårResultater?.forEach {
                familieBaSakKlient.putVilkår(
                        behandlingId = aktivBehandlingEtterRegistrertSøknad.behandlingId,
                        vilkårId = it.id,
                        restPersonResultat =
                        RestPersonResultat(personIdent = restPersonResultat.personIdent,
                                           vilkårResultater = listOf(it.copy(
                                                   resultat = Resultat.JA,
                                                   periodeFom = LocalDate.of(2019, 5, 8)
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

        await.atMost(60, TimeUnit.SECONDS).until {
            familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id).data?.status == FagsakStatus.LØPENDE
        }

        val restFagsakEtterBehandlingAvsluttet =
                familieBaSakKlient.hentFagsak(fagsakId = restFagsakEtterIverksetting.data!!.id)
        generellAssertFagsak(restFagsak = restFagsakEtterBehandlingAvsluttet,
                             fagsakStatus = FagsakStatus.LØPENDE,
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