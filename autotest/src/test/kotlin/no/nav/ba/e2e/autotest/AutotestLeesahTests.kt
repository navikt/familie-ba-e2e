package no.nav.ba.e2e.autotest

import no.nav.person.pdl.leesah.Endringstype
import no.nav.person.pdl.leesah.Personhendelse
import no.nav.person.pdl.leesah.foedsel.Foedsel
import org.apache.avro.generic.GenericRecordBuilder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class AutotestLeesahTests {

	@Autowired
	lateinit var kafkaService: ProducerService

	@Test
	fun verifiserStandardFødselshendelse() {
		val personhendelse = GenericRecordBuilder(Personhendelse.`SCHEMA$`)
		personhendelse.set("hendelseId", "2")
		val personidenter = ArrayList<String>()
		personidenter.add("1234567890123")
		personidenter.add("12345678901")
		personhendelse.set("personidenter", personidenter)
		personhendelse.set("master", "")
		personhendelse.set("opprettet", 0L)
		personhendelse.set("opplysningstype", "FOEDSEL_V1")
		personhendelse.set("endringstype", Endringstype.OPPRETTET)

		val fødsel = GenericRecordBuilder(Foedsel.`SCHEMA$`)
		fødsel.set("foedselsdato", (Instant.now().toEpochMilli() / (1000 * 3600 * 24)).toInt()) //Setter dagens dato på avroformat
		personhendelse.set("foedsel", fødsel.build())

		kafkaService.sendMessage(personhendelse.build())
		
		
		// TODO kalle rest endepunkt i ba-sak og sjekke at vi har lagret behandlingen.
	}

}
