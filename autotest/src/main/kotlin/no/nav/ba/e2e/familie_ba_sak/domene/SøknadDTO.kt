package no.nav.ba.e2e.familie_ba_sak.domene

import no.nav.familie.kontrakter.felles.objectMapper
import java.time.LocalDate

data class RestRegistrerSøknad(
        val søknad: SøknadDTO,
        val bekreftEndringerViaFrontend: Boolean
)

data class SøknadDTO(
        val underkategori: BehandlingUnderkategori,
        val søkerMedOpplysninger: SøkerMedOpplysninger,
        val barnaMedOpplysninger: List<BarnMedOpplysninger>,
)

fun SøknadDTO.writeValueAsString(): String = objectMapper.writeValueAsString(this)

data class SøkerMedOpplysninger(
        val ident: String,
)

data class BarnMedOpplysninger(
        val ident: String,
        val borMedSøker: Boolean = true,
        val oppholderSegINorge: Boolean = true,
        val harOppholdtSegINorgeSiste12Måneder: Boolean = true,
        val navn: String = "",
        val inkludertISøknaden: Boolean = true,
        val fødselsdato: LocalDate? = null,
        val tilleggsopplysninger: String? = null
)
