package no.nav.ba.e2e.familie_ba_sak

import java.time.LocalDate
import java.time.LocalDateTime

data class RestMinimalFagsak(
        val opprettetTidspunkt: LocalDateTime,
        val id: Long,
        val søkerFødselsnummer: String,
        val status: FagsakStatus,
        val underBehandling: Boolean,
        val behandlinger: List<RestVisningBehandling>,
        val gjeldendeUtbetalingsperioder: List<Utbetalingsperiode>,
)

enum class FagsakStatus {
    OPPRETTET,
    LØPENDE, // Har minst én behandling gjeldende for fremtidig utbetaling
    AVSLUTTET
}

class RestVisningBehandling(
        val behandlingId: Long,
        val opprettetTidspunkt: LocalDateTime,
        val kategori: BehandlingKategori,
        val underkategori: BehandlingUnderkategori,
        val aktiv: Boolean,
        val årsak: BehandlingÅrsak?,
        val type: BehandlingType,
        val status: BehandlingStatus,
        val resultat: BehandlingResultat,
        val vedtaksdato: LocalDateTime?,
)

enum class BehandlingResultat(val displayName: String) {

    // Søknad
    INNVILGET(displayName = "Innvilget"),
    INNVILGET_OG_OPPHØRT(displayName = "Innvilget og opphørt"),
    INNVILGET_OG_ENDRET(displayName = "Innvilget og endret"),
    INNVILGET_ENDRET_OG_OPPHØRT(displayName = "Innvilget, endret og opphørt"),

    DELVIS_INNVILGET(displayName = "Delvis innvilget"),
    DELVIS_INNVILGET_OG_OPPHØRT(displayName = "Delvis innvilget og opphørt"),
    DELVIS_INNVILGET_OG_ENDRET(displayName = "Delvis innvilget og endret"),
    DELVIS_INNVILGET_ENDRET_OG_OPPHØRT(displayName = "Delvis innvilget, endret og opphørt"),

    AVSLÅTT(displayName = "Avslått"),
    AVSLÅTT_OG_OPPHØRT(displayName = "Avslått og opphørt"),
    AVSLÅTT_OG_ENDRET(displayName = "Avslått og endret"),
    AVSLÅTT_ENDRET_OG_OPPHØRT(displayName = "Avslått, endret og opphørt"),

    // Revurdering uten søknad
    ENDRET(displayName = "Endret"),
    ENDRET_OG_OPPHØRT(displayName = "Endret og opphørt"),
    OPPHØRT(displayName = "Opphørt"),
    FORTSATT_INNVILGET(displayName = "Fortsatt innvilget"),

    // Henlagt
    HENLAGT_FEILAKTIG_OPPRETTET(displayName = "Henlagt feilaktig opprettet"),
    HENLAGT_SØKNAD_TRUKKET(displayName = "Henlagt søknad trukket"),
    HENLAGT_AUTOMATISK_FØDSELSHENDELSE(displayName = "Henlagt avslått i automatisk vilkårsvurdering"),

    IKKE_VURDERT(displayName = "Ikke vurdert")
}


enum class BehandlingStatus {
    OPPRETTET,
    UTREDES,
    FATTER_VEDTAK,
    IVERKSETTER_VEDTAK,
    AVSLUTTET,
}


enum class BehandlingKategori(val visningsnavn: String) {
    EØS("EØS"),
    NASJONAL("Nasjonal");
}

enum class BehandlingUnderkategori(val visningsnavn: String) {
    UTVIDET("Utvidet"),
    ORDINÆR("Ordinær");
}


enum class BehandlingÅrsak(val visningsnavn: String) {

    SØKNAD("Søknad"),
    FØDSELSHENDELSE("Fødselshendelse"),
    ÅRLIG_KONTROLL("Årsak kontroll"),
    DØDSFALL_BRUKER("Dødsfall bruker"),
    NYE_OPPLYSNINGER("Nye opplysninger"),
    KLAGE("Klage"),
    TEKNISK_OPPHØR("Teknisk opphør"), // Ikke lenger i bruk. Bruk heller teknisk endring
    TEKNISK_ENDRING("Teknisk endring"), // Brukes i tilfeller ved systemfeil og vi ønsker å iverksette mot OS på nytt
    KORREKSJON_VEDTAKSBREV("Korrigere vedtak med egen brevmal"),
    OMREGNING_6ÅR("Omregning 6 år"),
    OMREGNING_18ÅR("Omregning 18 år"),
    SATSENDRING("Satsendring"),
    MIGRERING("Migrering"),
}

enum class BehandlingType(val visningsnavn: String) {
    FØRSTEGANGSBEHANDLING("Førstegangsbehandling"),
    REVURDERING("Revurdering"),
    MIGRERING_FRA_INFOTRYGD("Migrering fra infotrygd"),
    MIGRERING_FRA_INFOTRYGD_OPPHØRT("Opphør migrering fra infotrygd"),
    TEKNISK_OPPHØR("Teknisk opphør"), // Ikke lenger i bruk. Bruk heller teknisk endring
    TEKNISK_ENDRING("Teknisk endring")
}


data class Utbetalingsperiode(
        val periodeFom: LocalDate,
        val periodeTom: LocalDate,
        val antallBarn: Int,
        val utbetaltPerMnd: Int,
)