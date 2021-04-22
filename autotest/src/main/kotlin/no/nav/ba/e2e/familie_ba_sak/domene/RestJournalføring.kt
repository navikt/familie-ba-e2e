package no.nav.ba.e2e.familie_ba_sak.domene

import no.nav.familie.kontrakter.felles.journalpost.LogiskVedlegg
import java.time.LocalDateTime

data class RestJournalpostDokument(
        val dokumentTittel: String?,
        val dokumentInfoId: String,
        val brevkode: String?,
        val logiskeVedlegg: List<LogiskVedlegg>?,
        val eksisterendeLogiskeVedlegg: List<LogiskVedlegg>?,
)

data class RestJournalføring(
        val avsender: NavnOgIdent,
        val bruker: NavnOgIdent,
        val datoMottatt: LocalDateTime?,
        val journalpostTittel: String?,
        val knyttTilFagsak: Boolean,
        val opprettOgKnyttTilNyBehandling: Boolean,
        val tilknyttedeBehandlingIder: List<String>,
        val dokumenter: List<RestJournalpostDokument>,
        val navIdent: String,
        val nyBehandlingstype: BehandlingType,
        val nyBehandlingsårsak: BehandlingÅrsak,
)

data class NavnOgIdent(val navn: String,
                       val id: String
)