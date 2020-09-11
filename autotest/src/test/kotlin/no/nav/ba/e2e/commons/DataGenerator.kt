package no.nav.ba.e2e.commons

import no.nav.ba.e2e.familie_ba_sak.domene.*
import java.time.LocalDate

val morPersonident = "12345678901"
val farPersonident = "12345678911"
val barnPersonident = "01101800033"
val utenlandskBarnPersonident = "01101800044"
val utenlandskMorPersonident = "00000000005"
val utenlandskFarPersonident = "00000000006"

fun lagSøknadDTO(søkerIdent: String,
                 barnasIdenter: List<String>): SøknadDTO {
    return SøknadDTO(
            underkategori = BehandlingUnderkategori.ORDINÆR,
            søkerMedOpplysninger = SøkerMedOpplysninger(
                    ident = søkerIdent
            ),
            barnaMedOpplysninger = barnasIdenter.map {
                BarnMedOpplysninger(
                        ident = it
                )
            },
            endringAvOpplysningerBegrunnelse = ""
    )
}