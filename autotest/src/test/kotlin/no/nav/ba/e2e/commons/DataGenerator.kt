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
                 typeSøker: TypeSøker = TypeSøker.ORDINÆR,
                 annenPartIdent: String,
                 barnasIdenter: List<String>): SøknadDTO {
    return SøknadDTO(
            kategori = BehandlingKategori.NASJONAL,
            underkategori = BehandlingUnderkategori.ORDINÆR,
            annenPartIdent = annenPartIdent,
            typeSøker = typeSøker,
            søkerMedOpplysninger = SøkerMedOpplysninger(
                    ident = søkerIdent
            ),
            barnaMedOpplysninger = barnasIdenter.map {
                BarnMedOpplysninger(
                        ident = it
                )
            }
    )
}

fun vilkårsvurderingInnvilget(søkerIdent: String,
                              barnIdent: String,
                              barnFødselsdato: LocalDate): List<RestPersonResultat> = listOf(
        RestPersonResultat(
                personIdent = søkerIdent,
                vilkårResultater = listOf(RestVilkårResultat(vilkårType = Vilkår.BOSATT_I_RIKET,
                                                             resultat = Resultat.JA,
                                                             periodeFom = LocalDate.of(2019, 5, 8),
                                                             periodeTom = null,
                                                             begrunnelse = ""),
                                          RestVilkårResultat(vilkårType = Vilkår.LOVLIG_OPPHOLD,
                                                             resultat = Resultat.JA,
                                                             periodeFom = LocalDate.of(2019, 5, 8),
                                                             periodeTom = null,
                                                             begrunnelse = ""))),
        RestPersonResultat(
                personIdent = barnIdent,
                vilkårResultater = listOf(
                        RestVilkårResultat(vilkårType = Vilkår.BOSATT_I_RIKET,
                                           resultat = Resultat.JA,
                                           periodeFom = LocalDate.of(2019, 5, 8),
                                           periodeTom = null,
                                           begrunnelse = ""),
                        RestVilkårResultat(vilkårType = Vilkår.UNDER_18_ÅR,
                                           resultat = Resultat.JA,
                                           periodeFom = barnFødselsdato,
                                           periodeTom = barnFødselsdato.plusYears(18),
                                           begrunnelse = ""),
                        RestVilkårResultat(vilkårType = Vilkår.GIFT_PARTNERSKAP,
                                           resultat = Resultat.JA,
                                           periodeFom = LocalDate.of(2019, 5, 8),
                                           periodeTom = null,
                                           begrunnelse = ""),
                        RestVilkårResultat(vilkårType = Vilkår.BOR_MED_SØKER,
                                           resultat = Resultat.JA,
                                           periodeFom = LocalDate.of(2019, 5, 8),
                                           periodeTom = null,
                                           begrunnelse = ""),
                        RestVilkårResultat(vilkårType = Vilkår.LOVLIG_OPPHOLD,
                                           resultat = Resultat.JA,
                                           periodeFom = LocalDate.of(2019, 5, 8),
                                           periodeTom = null,
                                           begrunnelse = "")
                )))