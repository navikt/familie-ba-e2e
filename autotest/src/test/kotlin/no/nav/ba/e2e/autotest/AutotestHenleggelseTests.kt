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
    fun `Henlegg en behandling, hent forhåndsvising av brev, henlegg og opprett ny behandling`() {
        val søkersIdent = morPersonident
        val førsteBehandling = opprettBhandlingOgRegistrerSøknad(søkersIdent)

        val responseForhandsvis = baSakKlient.forhaandsvisHenleggelseBrev(førsteBehandling!!.behandlingId,
                                                                          RestHenleggDocGen(mottakerIdent = morPersonident,
                                                                                            brevmal = "HENLEGGE_TRUKKET_SØKNAD"))
        assertThat(responseForhandsvis?.status == Ressurs.Status.SUKSESS)

        // foerstesidegenerator er kke tilgjengelig derfor kan man ikke prøve med SØKNED_TRUKKET.
        val responseHenlagdSøknad = baSakKlient.henleggSøknad(førsteBehandling.behandlingId,
                                                              RestHenleggelse(årsak = "FEILAKTIG_OPPRETTET",
                                                                              begrunnelse = "Søknad trukket"))

        await.atMost(80, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            val fagsak = baSakKlient.hentFagsak(responseHenlagdSøknad!!.data!!.id).data
            fagsak?.status == FagsakStatus.AVSLUTTET
        }

        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.aktiv == false)
        assertThat(responseHenlagdSøknad?.data?.behandlinger?.first()?.samletResultat == BehandlingResultatType.HENLAGT_FEILAKTIG_OPPRETTET)

        val andreBehandling = opprettBhandlingOgRegistrerSøknad(søkersIdent)
        assertThat(andreBehandling?.aktiv == true)
    }

    private fun opprettBhandlingOgRegistrerSøknad(søkersIdent: String): RestBehandling? {
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
