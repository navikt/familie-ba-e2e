package no.nav.ba.e2e.mockserver

import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.getForObject

@Service
class MockserverKlient {

    val restOperations = RestTemplateBuilder().additionalInterceptors(
            MdcValuesPropagatingClientInterceptor()).build()

    fun hentOppgaveOpprettetMedCallid(callId: String): String {
        return restOperations.getForObject("http://localhost:1337/rest/api/oppgave/cache/$callId")
    }

    fun clearOppaveCache() {
        restOperations.delete("http://localhost:1337/rest/api/oppgave/cache/clear")
    }

    fun clearFerdigstillJournapostCache() {
        restOperations.delete("http://localhost:1337/rest/api/dokarkiv/internal/ferdigstill/clear")
    }
}