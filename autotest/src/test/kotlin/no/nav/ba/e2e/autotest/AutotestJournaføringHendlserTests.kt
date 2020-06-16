package no.nav.ba.e2e.autotest

import no.nav.ba.e2e.familie_ba_mottak.FamilieBaMottakKlient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AutotestJournaføringHendlserTests(@Autowired mottakKlient: FamilieBaMottakKlient
) : AbstractMottakTest(mottakKlient) {

    @Test
    fun `skal sende journalhendelse som fører til opprettJournalføringsoppgave`() {
        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_SKANNING)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        erTaskOpprettet("opprettJournalføringsoppgave", "e2e-" + response.body)
    }


    @Test
    fun `skal sende journalhendelse som fører til oppdaterOgFerdigstillJournalpost`() {
        val response = mottakKlient.postJournalhendelse(MIDLERTIDIG_JOURNALPOST_DIGITAL)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        assertThat(response.body).isNotNull()

        //Sjekk om det er et innslag i hendelselogg på meldingen
        val erHendelseMottatt = mottakKlient.erHendelseMottatt(response.body!!, "JOURNAL")
        assertThat(erHendelseMottatt.statusCode.is2xxSuccessful).isTrue()
        assertThat((erHendelseMottatt.body)).isTrue()

        erTaskOpprettet("oppdaterOgFerdigstillJournalpost", "e2e-" + response.body)
        erTaskOpprettet("opprettBehandleSakoppgave", "e2e-" + response.body)
    }

    companion object {
        const val MIDLERTIDIG_JOURNALPOST_SKANNING = "123456789"
        const val MIDLERTIDIG_JOURNALPOST_DIGITAL = "123454321"
    }

}
