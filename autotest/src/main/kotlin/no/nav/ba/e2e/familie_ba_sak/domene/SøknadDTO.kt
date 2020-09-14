package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

data class RestRegistrerSøknad(
        val søknad: SøknadDTO,
        val bekreftEndringerViaFrontend: Boolean
)

data class SøknadDTO(
        val underkategori: BehandlingUnderkategori,
        val søkerMedOpplysninger: SøkerMedOpplysninger,
        val barnaMedOpplysninger: List<BarnMedOpplysninger>,
        val endringAvOpplysningerBegrunnelse: String

)

data class SøkerMedOpplysninger(
        val ident: String,
        val målform: Målform = Målform.NB
)

data class BarnMedOpplysninger(
        val ident: String,
        val navn: String = "",
        val fødselsdato: LocalDate? = null,
        val inkludertISøknaden: Boolean = true,
        val manueltRegistrert: Boolean = false
)

enum class Målform {
    NB, NN
}