package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.Utils
import no.nav.ba.e2e.commons.barnPersonident
import no.nav.ba.e2e.commons.lagSøknadDTO
import no.nav.ba.e2e.commons.morPersonident
import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.*
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.concurrent.TimeUnit

class AutotestHenleggelseTests(
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient) : AbstractMottakTest(mottakKlient, baSakKlient) {

    @Test
    fun `Opprett behandling, henlegg behandling feilaktig opprettet og opprett behandling på nytt`() {
        val søkersIdent = morPersonident
        val førsteBehandling = opprettBehandlingOgRegistrerSøknad(søkersIdent)

        val responseHenlagdSøknad = baSakKlient.henleggSøknad(førsteBehandling!!.behandlingId,
                                                              RestHenleggelse(årsak = "FEILAKTIG_OPPRETTET",
                                                                              begrunnelse = "feilaktig opprettet"))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(responseHenlagdSøknad!!.data!!.id).data
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.aktiv == false)
        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.resultat == BehandlingResultat.HENLAGT_FEILAKTIG_OPPRETTET)

        val logger = baSakKlient.hentLogg(responseHenlagdSøknad!!.data!!.id)
        if (logger?.status == Ressurs.Status.SUKSESS) {
            assertThat(logger.data?.filter { it.type == LoggType.HENLEGG_BEHANDLING }?.size == 1)
            assertThat(logger.data?.filter { it.type == LoggType.DISTRIBUERE_BREV }?.size == 0)
        }

        val andreBehandling = opprettBehandlingOgRegistrerSøknad(søkersIdent)
        assertThat(andreBehandling?.aktiv == true)
    }

    @Test
    fun `Opprett behandling, hent forhåndsvising av brev, henlegg behandling søknad trukket`() {
        val søkersIdent = morPersonident
        val førsteBehandling = opprettBehandlingOgRegistrerSøknad(søkersIdent)

        val responseForhandsvis = baSakKlient.forhaandsvisHenleggelseBrev(førsteBehandling!!.behandlingId,
                                                                          RestHenleggDocGen(mottakerIdent = morPersonident,
                                                                                            brevmal = "HENLEGGE_TRUKKET_SØKNAD"))
        assertThat(responseForhandsvis?.status == Ressurs.Status.SUKSESS)

        val responseHenlagdSøknad = baSakKlient.henleggSøknad(førsteBehandling.behandlingId,
                                                              RestHenleggelse(årsak = "SØKNAD_TRUKKET",
                                                                              begrunnelse = "Søknad trukket"))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(responseHenlagdSøknad!!.data!!.id).data
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.aktiv == false)
        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.resultat == BehandlingResultat.HENLAGT_SØKNAD_TRUKKET)

        val logger = baSakKlient.hentLogg(responseHenlagdSøknad!!.data!!.id)
        if (logger?.status == Ressurs.Status.SUKSESS) {
            assertThat(logger.data?.filter { it.type == LoggType.HENLEGG_BEHANDLING }?.size == 1)
            assertThat(logger.data?.filter { it.type == LoggType.DISTRIBUERE_BREV }?.size == 1)
        }
    }

    private fun opprettBehandlingOgRegistrerSøknad(søkersIdent: String): RestBehandling? {
        val barn1 = barnPersonident

        baSakKlient.opprettFagsak(søkersIdent = søkersIdent)
        val restFagsakMedBehandling = baSakKlient.opprettBehandling(søkersIdent = søkersIdent)

        val aktivBehandling = Utils.hentAktivBehandling(restFagsak = restFagsakMedBehandling.data!!)
        val restRegistrerSøknad = RestRegistrerSøknad(søknad = lagSøknadDTO(søkerIdent = søkersIdent,
                                                                            barnasIdenter = listOf(barn1)),
                                                      bekreftEndringerViaFrontend = false)
        baSakKlient.registrererSøknad(
                behandlingId = aktivBehandling!!.behandlingId,
                restRegistrerSøknad = restRegistrerSøknad
        )
        return aktivBehandling
    }
}
