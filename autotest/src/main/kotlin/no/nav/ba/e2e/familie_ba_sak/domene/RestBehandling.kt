package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate
import java.time.LocalDateTime

data class RestBehandling(val aktiv: Boolean,
                          val arbeidsfordelingPåBehandling: RestArbeidsfordelingPåBehandling,
                          val årsak: BehandlingÅrsak,
                          val skalBehandlesAutomatisk: Boolean,
                          val behandlingId: Long,
                          val type: BehandlingType,
                          val status: BehandlingStatus,
                          val steg: StegType,
                          val stegTilstand: List<RestBehandlingStegTilstand>,
                          val søknadsgrunnlag: SøknadDTO?,
                          val kategori: BehandlingKategori,
                          val personer: List<RestPerson>,
                          val opprettetTidspunkt: LocalDateTime,
                          val underkategori: BehandlingUnderkategori,
                          val personResultater: List<RestPersonResultat>,
                          val resultat: BehandlingResultat,
                          val endretAv: String)

enum class BehandlingType(val visningsnavn: String) {
    FØRSTEGANGSBEHANDLING("Førstegangsbehandling"),
    REVURDERING("Revurdering"),
    MIGRERING_FRA_INFOTRYGD("Migrering fra infotrygd"),
    KLAGE("Klage"),
    MIGRERING_FRA_INFOTRYGD_OPPHØRT("Opphør migrering fra infotrygd"),
    TEKNISK_OPPHØR("Teknisk opphør")
}

enum class BehandlingÅrsak(val visningsnavn: String) {
    SØKNAD("Søknad"),
    FØDSELSHENDELSE("Fødselshendelse"),
    ÅRLIG_KONTROLL("Årsak kontroll"),
    DØDSFALL("Dødsfall"),
    NYE_OPPLYSNINGER("Nye opplysninger"),
    TEKNISK_OPPHØR("Teknisk opphør") // Kan være tilbakeføring til infotrygd, feilutbetaling
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

enum class BehandlingResultat(val brevMal: String, val displayName: String) {
    INNVILGET(brevMal = "innvilget", displayName = "Innvilget"),
    ENDRET_OG_FORTSATT_INNVILGET(brevMal = "innvilget", displayName = "Endret og fortsatt innvilget"),
    ENDRET_OG_OPPHØRT(brevMal = "endring_og_opphort", displayName = "Endret og opphørt"),
    OPPHØRT(brevMal = "opphor", displayName = "Opphørt"),
    AVSLÅTT(brevMal = "avslag", displayName = "Avslått"),
    FORTSATT_INNVILGET(brevMal = "ukjent", displayName = "Fortsatt innvilget"),
    DELVIS_INNVILGET(brevMal = "ukjent", displayName = "Delvis innvilget"),
    HENLAGT_FEILAKTIG_OPPRETTET(brevMal = "ukjent", displayName = "Henlagt feilaktig opprettet"),
    HENLAGT_SØKNAD_TRUKKET(brevMal = "ukjent", displayName = "Henlagt søknad trukket"),
    IKKE_VURDERT(brevMal = "ukjent", displayName = "Ikke vurdert"),
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
    OPPFYLT,
    IKKE_OPPFYLT,
    IKKE_VURDERT
}