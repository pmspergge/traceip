package com.meli.challenge.traceip.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendIpTraceRequest(String ip) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("trace-ip-topic", ip);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Mensaje enviado a Kafka: {}", ip);
                } else {
                    logger.error("Error al enviar mensaje a Kafka", ex);
                }
            });
        } catch (Exception e) {
            logger.error("Error inesperado al enviar mensaje a Kafka", e);
        }
    }
}
