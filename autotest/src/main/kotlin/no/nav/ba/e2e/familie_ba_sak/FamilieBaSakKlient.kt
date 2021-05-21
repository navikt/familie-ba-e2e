package no.nav.ba.e2e.familie_ba_sak

import no.nav.ba.e2e.familie_ba_mottak.Task
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingKategori
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingType
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingUnderkategori
import no.nav.ba.e2e.familie_ba_sak.domene.BehandlingÅrsak
import no.nav.ba.e2e.familie_ba_sak.domene.FagsakRequest
import no.nav.ba.e2e.familie_ba_sak.domene.Logg
import no.nav.ba.e2e.familie_ba_sak.domene.Metrikk
import no.nav.ba.e2e.familie_ba_sak.domene.NyBehandling
import no.nav.ba.e2e.familie_ba_sak.domene.RestBeslutningPåVedtak
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsak
import no.nav.ba.e2e.familie_ba_sak.domene.RestFagsakDeltager
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggDocGen
import no.nav.ba.e2e.familie_ba_sak.domene.RestHenleggelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestJournalføring
import no.nav.ba.e2e.familie_ba_sak.domene.RestPersonResultat
import no.nav.ba.e2e.familie_ba_sak.domene.RestPostVedtakBegrunnelse
import no.nav.ba.e2e.familie_ba_sak.domene.RestRegistrerSøknad
import no.nav.ba.e2e.familie_ba_sak.domene.RestSøkParam
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import java.net.URI

@Service
class FamilieBaSakKlient(
        @Value("\${FAMILIE_BA_SAK_API_URL}") private val baSakUrl: String,
        private val restOperations: RestOperations
) : AbstractRestClient(restOperations, "familie-ba-sak") {

    fun truncate(): ResponseEntity<Ressurs<String>> {
        val uri = URI.create("$baSakUrl/api/e2e/truncate")

        return restOperations.getForEntity(uri)
    }

    fun journalfør(journalpostId: String,
                   oppgaveId: String,
                   journalførendeEnhet: String,
                   restJournalføring: RestJournalføring): Ressurs<String> {
        return postForEntity(
                uri = URI.create("$baSakUrl/api/journalpost/$journalpostId/journalfør/$oppgaveId?journalfoerendeEnhet=$journalførendeEnhet"),
                payload = restJournalføring
        )
    }

    fun opprettFagsak(søkersIdent: String): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker")

        return postForEntity(uri, FagsakRequest(
                personIdent = søkersIdent
        ))
    }

    fun opprettBehandling(søkersIdent: String,
                          behandlingType: BehandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
                          behandlingÅrsak: BehandlingÅrsak = BehandlingÅrsak.SØKNAD): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger")

        return postForEntity(uri, NyBehandling(
                kategori = BehandlingKategori.NASJONAL,
                underkategori = BehandlingUnderkategori.ORDINÆR,
                søkersIdent = søkersIdent,
                behandlingType = behandlingType,
                behandlingÅrsak = behandlingÅrsak
        ))
    }

    fun registrererSøknad(behandlingId: Long, restRegistrerSøknad: RestRegistrerSøknad): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger/$behandlingId/registrere-søknad-og-hent-persongrunnlag")

        return postForEntity(uri, restRegistrerSøknad)
    }

    fun putVilkår(behandlingId: Long, vilkårId: Long, restPersonResultat: RestPersonResultat): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/vilkaarsvurdering/$behandlingId/$vilkårId")

        return putForEntity(uri, restPersonResultat)
    }

    fun validerVilkårsvurdering(behandlingId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/vilkaarsvurdering/$behandlingId/valider")

        return postForEntity(uri, "")
    }

    fun lagreTilbakekrevingOgGåVidereTilNesteSteg(behandlingId: Long,
                                                  restTilbakekreving: RestTilbakekreving?): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/behandlinger/$behandlingId/tilbakekreving")
        return postForEntity(uri, restTilbakekreving ?: "")
    }

    fun sendTilBeslutter(fagsakId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId/send-til-beslutter?behandlendeEnhet=9999")

        return postForEntity(uri, "")
    }

    fun iverksettVedtak(fagsakId: Long, restBeslutningPåVedtak: RestBeslutningPåVedtak): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId/iverksett-vedtak")

        return postForEntity(uri, restBeslutningPåVedtak)
    }

    fun hentFagsak(fagsakId: Long): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId")

        return getForEntity(uri)
    }

    fun hentFagsakDeltager(personIdent: String): RestFagsakDeltager? {
        val ressurs = hentListeFagsakDeltager(RestSøkParam(personIdent))
        return ressurs?.data?.firstOrNull()
    }

    private fun hentListeFagsakDeltager(restSøkParam: RestSøkParam): Ressurs<List<RestFagsakDeltager>>? {
        val uri = URI.create("$baSakUrl/api/fagsaker/sok")
        return postForEntity(uri, restSøkParam)
    }

    fun hentTasker(key: String, value: String): ResponseEntity<List<Task>> {
        return restOperations.getForEntity("$baSakUrl/api/e2e/task/$key/$value")
    }

    fun forhaandsvisHenleggelseBrev(behandlingId: Long, restHenleggDocGen: RestHenleggDocGen): Ressurs<ByteArray>? {
        val uri = URI.create("$baSakUrl/api/dokument/forhaandsvis-brev/${behandlingId}")
        return postForEntity(uri, restHenleggDocGen)
    }

    fun genererOgHentVedtaksbrev(vedtakId: Long): Ressurs<ByteArray>? {
        val uri = URI.create("$baSakUrl/api/dokument/vedtaksbrev/${vedtakId}")
        return postForEntity(uri, vedtakId)
    }

    fun hentVedtaksbrev(vedtakId: Long): Ressurs<ByteArray>? {
        val uri = URI.create("$baSakUrl/api/dokument/vedtaksbrev/${vedtakId}")
        return getForEntity(uri)
    }

    fun henleggSøknad(behandlingId: Long, restHenleggelse: RestHenleggelse): Ressurs<RestFagsak>? {
        val uri = URI.create("$baSakUrl/api/behandlinger/${behandlingId}/henlegg")
        return putForEntity(uri, restHenleggelse)
    }

    fun hentLogg(behandlingId: Long): Ressurs<List<Logg>>? {
        val uri = URI.create("$baSakUrl/api/logg/${behandlingId}")
        return getForEntity(uri)
    }

    fun tellMetrikk(metrikkNavn: String, tag: Pair<String, String>): Long {
        val metric = restOperations.getForObject("$baSakUrl/internal/metrics/$metrikkNavn?tag=${tag.first}:${tag.second}",
                                                 Metrikk::class.java)
        return metric?.measurements?.first { it.statistic == "COUNT" }?.value ?: 0
    }

    fun leggTilVedtakBegrunnelse(fagsakId: Long, vedtakBegrunnelse: RestPostVedtakBegrunnelse): Ressurs<RestFagsak> {
        val uri = URI.create("$baSakUrl/api/fagsaker/$fagsakId/vedtak/begrunnelser")
        return postForEntity(uri, vedtakBegrunnelse)
    }

    fun triggerAutobrev18og6år(): Ressurs<String> {
        return getForEntity(URI.create("$baSakUrl/testverktoy/autobrev"))
    }
}

