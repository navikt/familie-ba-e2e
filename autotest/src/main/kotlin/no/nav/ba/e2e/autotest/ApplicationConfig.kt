package no.nav.ba.e2e.autotest

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan

@SpringBootConfiguration
@EntityScan( ApplicationConfig.pakkenavn)
@ComponentScan( ApplicationConfig.pakkenavn)
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
class ApplicationConfig {

    companion object {
        const val pakkenavn = "no.nav.ba.e2e"
    }
}
