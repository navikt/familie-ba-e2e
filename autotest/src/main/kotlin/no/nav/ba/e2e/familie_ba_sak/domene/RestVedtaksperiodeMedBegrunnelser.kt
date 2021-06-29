package no.nav.ba.e2e.familie_ba_sak.domene

import java.time.LocalDate

data class RestVedtaksperiodeMedBegrunnelser(
        val id: Long,
        val fom: LocalDate?,
        val tom: LocalDate?,
        val type: Vedtaksperiodetype,
        val begrunnelser: List<RestVedtaksbegrunnelse>,
        val fritekster: List<String> = emptyList(),
)

data class RestVedtaksbegrunnelse(
        val vedtakBegrunnelseSpesifikasjon: VedtakBegrunnelseSpesifikasjon,
        val vedtakBegrunnelseType: VedtakBegrunnelseType,
        val personIdenter: List<String> = emptyList(),
)

enum class VedtakBegrunnelseType {
    INNVILGELSE,
    REDUKSJON,
    AVSLAG,
    OPPHØR,
    FORTSATT_INNVILGET
}

data class RestPutVedtaksperiodeMedBegrunnelse(
        val begrunnelser: List<RestPutVedtaksbegrunnelse>,
        val fritekster: List<String> = emptyList(),
)

data class RestPutVedtaksperiodeMedFritekster(
        val fritekster: List<String> = emptyList(),
)

data class RestPutVedtaksperiodeMedStandardbegrunnelser(
        val standardbegrunnelser: List<VedtakBegrunnelseSpesifikasjon>,
)

data class RestPutVedtaksbegrunnelse(
        val vedtakBegrunnelseSpesifikasjon: VedtakBegrunnelseSpesifikasjon,
)

enum class VedtakBegrunnelseSpesifikasjon {
    INNVILGET_LOVLIG_OPPHOLD_EØS_BORGER,
    REDUKSJON_UNDER_6_ÅR,
    AVSLAG_BOSATT_I_RIKET,
}