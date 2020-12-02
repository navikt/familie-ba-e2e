package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.commons.morPersonident
import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import no.nav.ba.e2e.familie_ba_sak.FamilieBaSakKlient
import no.nav.ba.e2e.mockserver.MockserverKlient
import no.nav.familie.prosessering.domene.Status
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AutotestJournaføringHendlserTests(
        @Autowired mockserverKlient: MockserverKlient,
        @Autowired mottakKlient: FamilieBaMottakKlient,
        @Autowired baSakKlient: FamilieBaSakKlient) : AbstractMottakTest(mottakKlient, baSakKlient, mockserverKlient

) {


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
                .contains("beskrivelse\":\"Ordinær barnetrygd")
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
    fun `skal sende journalhendelse som fører til oppdaterOgFerdigstillJournalpost`() {
        // Trenger først en fagsak på samme bruker som journalhendelsen gjelder i ba-sak
        baSakKlient.opprettFagsak(søkersIdent = morPersonident)

        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_DIGITAL)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        harTaskStatus("oppdaterOgFerdigstillJournalpost", "e2e-" + response.body, Status.FERDIG)
        harTaskStatus("opprettBehandleSakoppgave", "e2e-" + response.body, Status.FERDIG)
        assertThat(mockserverKlient?.hentOppgaveOpprettetMedCallid("e2e-" + response.body))
                .contains("beskrivelse\":\"Ordinær barnetrygd")
                .contains("BEH_SAK")
    }

    companion object {

        const val MIDLERTIDIG_JOURNALPOST_SKANNING = "123456789"
        const val MIDLERTIDIG_JOURNALPOST_DIGITAL = "123454321"
    }

}
