package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate
import java.time.LocalDateTime

data class RestBehandling(val aktiv: Boolean,
                          val behandlingId: Long,
                          val type: BehandlingType,
                          val status: BehandlingStatus,
                          val steg: StegType,
                          val kategori: BehandlingKategori,
                          val personer: List<RestPerson>,
                          val opprettetTidspunkt: LocalDateTime,
                          val underkategori: BehandlingUnderkategori,
                          val personResultater: List<RestPersonResultat>,
                          val samletResultat: BehandlingResultatType,
                          val vedtakForBehandling: List<Any?>,
                          val endretAv: String)

enum class BehandlingType(val visningsnavn: String) {
    FØRSTEGANGSBEHANDLING("Førstegangsbehandling"),
    REVURDERING("Revurdering"),
    MIGRERING_FRA_INFOTRYGD("Migrering fra infotrygd"),
    KLAGE("Klage"),
    MIGRERING_FRA_INFOTRYGD_OPPHØRT("Opphør migrering fra infotrygd"),
    TEKNISK_OPPHØR("Teknisk opphør")
}

enum class BehandlingKategori {
    EØS,
    NASJONAL
}

enum class BehandlingUnderkategori {
    UTVIDET,
    ORDINÆR
}

enum class BehandlingStatus {
    OPPRETTET,
    UNDERKJENT_AV_BESLUTTER,
    SENDT_TIL_BESLUTTER,
    GODKJENT,
    SENDT_TIL_IVERKSETTING,
    IVERKSATT,
    FERDIGSTILT
}

enum class BehandlingResultatType(val brevMal: String, val displayName: String) {
    INNVILGET(brevMal = "Innvilget", displayName = "Innvilget"),
    DELVIS_INNVILGET(brevMal = "Ukjent", displayName = "Delvis innvilget"),
    AVSLÅTT(brevMal = "Avslag", displayName = "Avslått"),
    OPPHØRT(brevMal = "Opphor", displayName = "Opphørt"),
    HENLAGT(brevMal = "Ukjent", displayName = "Henlagt"),
    IKKE_VURDERT(brevMal = "Ukjent", displayName = "Ikke vurdert")
}

data class RestPersonResultat(
        val personIdent: String,
        val vilkårResultater: List<RestVilkårResultat>?
)

data class RestVilkårResultat(
        val id: Long,
        val vilkårType: Any,
        val resultat: Resultat,
        val periodeFom: LocalDate?,
        val periodeTom: LocalDate?,
        val begrunnelse: String
)


enum class Resultat {
    JA, NEI, KANSKJE
}