package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakStatus
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggelse
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.familie.prosessering.domene.Status
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.core.ConditionTimeoutException
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.concurrent.TimeUnit

class JournaforingHendlserTest(
        @Autowired mockserverKlient: MockserverKlient,
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient) : AbstractMottakTest(mottakKlient, baSakKlient, mockserverKlient

) {

    val morPersonident = "12345678901"


    @Test
    fun `skal sende journalhendelse som fører til opprettJournalføringsoppgave`() {
        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_SKANNING)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        harTaskStatus("opprettJournalføringsoppgave", "e2e-" + response.body, status = Status.FERDIG)
        assertThat(mockserverKlient?.hentOppgaveOpprettetMedCallid("e2e-" + response.body))
                .contains("beskrivelse")
                .contains("JFR")
    }

    @Test
    fun `skal sende journalhendelse som fører til opprettJournalføringsoppgave med info om sak i oppgavebeskrivelsen`() {
        baSakKlient.opprettFagsak(søkersIdent = morPersonident)

        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_SKANNING)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        harTaskStatus("opprettJournalføringsoppgave", "e2e-" + response.body, status = Status.FERDIG)
        assertThat(mockserverKlient?.hentOppgaveOpprettetMedCallid("e2e-" + response.body))
                .contains("Bruker har sak i BA-sak")
                .contains("JFR")
                
                
    }

    @Test
    fun `mottak av digital søknad skal føre til opprettJournalføringsoppgave når bruker har sak i ba-sak`() {
        baSakKlient.opprettFagsak(søkersIdent = morPersonident)

        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_DIGITAL)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        harTaskStatus("opprettJournalføringsoppgave", "e2e-" + response.body, Status.FERDIG)
        assertThat(mockserverKlient?.hentOppgaveOpprettetMedCallid("e2e-" + response.body))
                .contains("Bruker har sak i BA-sak")
                .contains("JFR")
    }

    @Test
    @Disabled
    fun `mottak av søknad skal ikke føre til oppgave når bruker har avsluttet sak i ba-sak som følge av henleggelse`() {
        val fagsakId = baSakKlient.opprettFagsak(DEFAULT_PERSON).data!!.id
        baSakKlient.opprettBehandling(DEFAULT_PERSON).data!!.apply {
            baSakKlient.henleggSøknad(behandlinger.first{ it.aktiv }.behandlingId,
                                      RestHenleggelse(årsak = "FEILAKTIG_OPPRETTET", begrunnelse = "feilaktig opprettet"))
        }

        await.atMost(60, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1)).until {
            baSakKlient.hentFagsak(fagsakId).data.let {
                println("FAGSAK: $it")
                it?.status == FagsakStatus.AVSLUTTET
            }
        }

        val response = mottakKlient.postJournalhendelse(DEFAULT_JOURNALPOST)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        assertThrows<ConditionTimeoutException> {
            val callId = "e2e-" + response.body
            await.atMost(6, TimeUnit.SECONDS).withPollInterval(Duration.ofSeconds(1))
                    .until { sjekkOmTaskIMottakHarStatus("opprettJournalføringsoppgave", callId, Status.UBEHANDLET) }
        }
    }

    companion object {

        const val MIDLERTIDIG_JOURNALPOST_SKANNING = "123456789"
        const val MIDLERTIDIG_JOURNALPOST_DIGITAL = "123454321"

        const val DEFAULT_PERSON = "12345678911"
        const val DEFAULT_JOURNALPOST = "11111"
    }

}
