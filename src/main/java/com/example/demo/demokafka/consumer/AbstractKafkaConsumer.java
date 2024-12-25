package com.example.demo.demokafka.consumer;//package com.example.demo.springbootkafkaavro.consumer;
//
//import com.decathlon.eyes.flows.partner.exceptions.EyesFlowTechniqueException;
//import com.decathlon.eyes.flows.partner.exceptions.MissingVesselInformationException;
//import com.decathlon.eyes.flows.partner.exceptions.TooManyElementException;
//import com.decathlon.eyes.flows.partner.tdo.headers.DtoMessageHeaders;
//import com.decathlon.eyes.flows.partner.tdo.standard.DtoMetaData;
//import com.decathlon.tac.data.exception.TacException;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//
///**
// * BASE FOR KAFKA LISTENER.
// */
//@Slf4j
//public abstract class AbstractKafkaConsumer<K, V> {
//
//	protected DtoMetaData metadata;
//
//
//	/**
//	 * main process
//	 *
//	 * @param consumerRecord kafkaMessage from eyes api equipments
//	 * @throws TacException
//	 * @throws JsonProcessingException
//	 * @throws TooManyElementException
//	 * @throws MissingVesselInformationException
//	 */
//	protected abstract void process(ConsumerRecord<K, V> consumerRecord) throws EyesFlowTechniqueException, TacException, JsonProcessingException;
//
//	/**
//	 * process the message from the main topic
//	 *
//	 * @param consumerRecord
//	 */
//	public void listen(ConsumerRecord<K, V> consumerRecord) throws EyesFlowTechniqueException, JsonProcessingException {
//		log.info("Start listening {} offset {}", consumerRecord.topic(), consumerRecord.offset());
//		log.debug("Header : {}", consumerRecord.headers());
//		log.debug("Content : {}", consumerRecord.value());
//		metadata = new DtoMetaData((String) consumerRecord.key(), consumerRecord.offset(), consumerRecord.partition());
//		process(consumerRecord);
//	}
//
//	public DtoMessageHeaders buildHeader(String clientId) {
//		return new DtoMessageHeaders()
//			.setMetaData(metadata)
//			.setClientId(clientId)
//			.setSubId(clientId);
//	}
//}