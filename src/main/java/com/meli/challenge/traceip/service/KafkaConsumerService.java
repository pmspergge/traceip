package com.meli.challenge.traceip.service;

import com.meli.challenge.traceip.dto.IpInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final TraceIpService traceIpService;

    public KafkaConsumerService(TraceIpService traceIpService) {
        this.traceIpService = traceIpService;
    }

    @KafkaListener(topics = "trace-ip-topic", groupId = "traceip-group")
    public void consumeIpTrace(String ip) {
        logger.info("Mensaje recibido de Kafka: {}", ip);
        IpInfoDTO ipInfo = traceIpService.getIpInfo(ip);
        logger.info("Información de IP procesada: {}", ipInfo);
    }
}
