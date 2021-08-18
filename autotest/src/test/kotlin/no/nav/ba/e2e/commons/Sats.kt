package no.nav.ba.e2e.commons

import java.time.LocalDate

data class Sats(val type: SatsType,
                val beløp: Int,
                val gyldigFom: LocalDate = LocalDate.MIN,
                val gyldigTom: LocalDate = LocalDate.MAX)

enum class SatsType(val beskrivelse: String) {
    ORBA("Ordinær barnetrygd"),
    SMA("Småbarnstillegg"),
    TILLEGG_ORBA("Tillegg til barnetrygd for barn 0-6 år"),
    FINN_SVAL("Finnmark- og Svalbardtillegg")
}

private val satser = listOf(
        Sats(SatsType.ORBA, 1054, LocalDate.of(2019, 3, 1), LocalDate.MAX),
        Sats(SatsType.ORBA, 970, LocalDate.MIN, LocalDate.of(2019, 2, 28)),
        Sats(SatsType.SMA, 660, LocalDate.MIN, LocalDate.MAX),
        Sats(SatsType.TILLEGG_ORBA, 970, LocalDate.MIN, LocalDate.of(2019, 2, 28)),
        Sats(SatsType.TILLEGG_ORBA, 1054, LocalDate.of(2019, 3, 1), LocalDate.of(2020, 8, 31)),
        Sats(SatsType.TILLEGG_ORBA, 1354, LocalDate.of(2020, 9, 1), LocalDate.of(2021, 8, 31)),
        Sats(SatsType.TILLEGG_ORBA, 1654, LocalDate.of(2021, 9, 1), LocalDate.MAX),
        Sats(SatsType.FINN_SVAL, 1054, LocalDate.MIN, LocalDate.of(2014, 3, 31))
)

val ordinærSats: Sats = satser.find { it.type == SatsType.ORBA && it.gyldigTom == LocalDate.MAX }!!
val tilleggOrdinærSats: Sats = satser.first { it.type == SatsType.TILLEGG_ORBA && !it.gyldigTom.isBefore(LocalDate.now())}!!
val nyttTilleggOrdinærSats: Sats = satser.find { it.type == SatsType.TILLEGG_ORBA && it.gyldigTom == LocalDate.MAX }!!
