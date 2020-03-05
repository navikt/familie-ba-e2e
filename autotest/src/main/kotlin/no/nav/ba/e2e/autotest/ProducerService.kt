package no.nav.ba.e2e.autotest

import org.apache.avro.generic.GenericRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service


@Service
class ProducerService {
    @Autowired
    private val kafkaTemplate: KafkaTemplate<String, GenericRecord?>? = null

    fun sendMessage(message: GenericRecord?) {
        kafkaTemplate!!.send(TOPIC, message)
    }

    companion object {
        private const val TOPIC = "aapen-person-pdl-leesah-v1"
    }
}