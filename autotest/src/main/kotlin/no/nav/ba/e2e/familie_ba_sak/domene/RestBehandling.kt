package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate
import java.time.LocalDateTime

data class RestBehandling(
        val aktiv: Boolean,
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
        val vedtakForBehandling: List<RestVedtak>,
        val endretAv: String,
        val vedtaksperioder: List<Vedtaksperiode>,
)

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
    TEKNISK_OPPHØR("Teknisk opphør"), // Kan være tilbakeføring til infotrygd, feilutbetaling
    OMREGNING_6ÅR("Omregning 6 år"),
    OMREGNING_18ÅR("Omregning 18 år")
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

enum class BehandlingResultat {
    INNVILGET,
    ENDRET,
    OPPHØRT,
    AVSLÅTT,
    FORTSATT_INNVILGET,
    DELVIS_INNVILGET,
    HENLAGT_FEILAKTIG_OPPRETTET,
    HENLAGT_SØKNAD_TRUKKET,
    IKKE_VURDERT,
}

data class RestPersonResultat(
        val personIdent: String,
        val vilkårResultater: List<RestVilkårResultat>?
)

data class RestVilkårResultat(
        val id: Long,
        val vilkårType: Vilkår,
        val resultat: Resultat,
        val periodeFom: LocalDate?,
        val periodeTom: LocalDate?,
        val begrunnelse: String,
        val endretAv: String = "VL",
        val endretTidspunkt: LocalDateTime = LocalDateTime.now(),
        val behandlingId: Long
)

data class RestVedtak(
        val aktiv: Boolean,
        val vedtaksdato: LocalDateTime?,
        val id: Long
)


enum class Resultat {
    OPPFYLT,
    IKKE_OPPFYLT,
    IKKE_VURDERT
}