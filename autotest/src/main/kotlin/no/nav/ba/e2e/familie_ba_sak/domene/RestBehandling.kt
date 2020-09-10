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
                          val gjeldendeForUtbetaling: Boolean,
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
    UTREDES,
    FATTER_VEDTAK,
    IVERKSETTER_VEDTAK,
    AVSLUTTET,
}

enum class BehandlingResultatType(val brevMal: String, val displayName: String) {
    INNVILGET(brevMal = "innvilget", displayName = "Innvilget"),
    DELVIS_INNVILGET(brevMal = "ukjent", displayName = "Delvis innvilget"),
    AVSLÅTT(brevMal = "avslag", displayName = "Avslått"),
    OPPHØRT(brevMal = "opphor", displayName = "Opphørt"),
    HENLAGT(brevMal = "ukjent", displayName = "Henlagt"),
    IKKE_VURDERT(brevMal = "ukjent", displayName = "Ikke vurdert")
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
        val begrunnelse: String,
        val endretAv: String = "VL",
        val endretTidspunkt: LocalDateTime = LocalDateTime.now(),
        val behandlingId: Long
)


enum class Resultat {
    JA, NEI, KANSKJE
}