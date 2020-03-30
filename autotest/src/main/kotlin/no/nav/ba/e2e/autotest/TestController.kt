package no.nav.ba.e2e.autotest

import no.nav.person.pdl.leesah.Endringstype
import no.nav.person.pdl.leesah.Personhendelse
import no.nav.person.pdl.leesah.foedsel.Foedsel
import org.apache.avro.generic.GenericRecordBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/test")
class TestController {

    @Autowired
    lateinit var kafkaService: ProducerService

    @PostMapping
    fun sendFødselshendelse(@RequestBody request: PersonHendelse): String {
        val personhendelse = GenericRecordBuilder(Personhendelse.`SCHEMA$`)
        personhendelse.set("hendelseId", request.hendelseId ?: "2")
        personhendelse.set("personidenter", request.aktørOgPersonIdent)
        personhendelse.set("master", "")
        personhendelse.set("opprettet", 0L)
        personhendelse.set("opplysningstype", "FOEDSEL_V1")
        personhendelse.set("endringstype", Endringstype.OPPRETTET)

        val fødsel = GenericRecordBuilder(Foedsel.`SCHEMA$`)
        fødsel.set("foedselsdato", (Instant.now().toEpochMilli() / (1000 * 3600 * 24)).toInt()) //Setter dagens dato på avroformat
        personhendelse.set("foedsel", fødsel.build())

        kafkaService.sendMessage(personhendelse.build())

        return "Personhendelse sendt"
    }

    data class PersonHendelse(val hendelseId: String? = null,
                              val aktørOgPersonIdent: ArrayList<String>)
}

