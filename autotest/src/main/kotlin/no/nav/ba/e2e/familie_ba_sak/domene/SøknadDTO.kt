package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

data class RestRegistrerSøknad(
        val søknad: SøknadDTO,
        val bekreftEndringerViaFrontend: Boolean
)

data class SøknadDTO(
        val underkategori: BehandlingUnderkategori,
        val søkerMedOpplysninger: SøkerMedOpplysninger,
        val barnaMedOpplysninger: List<BarnMedOpplysninger>
)

data class SøkerMedOpplysninger(
        val ident: String
)

data class BarnMedOpplysninger(
        val ident: String,
        val navn: String = "",
        val inkludertISøknaden: Boolean = true,
        val fødselsdato: LocalDate? = null
)
