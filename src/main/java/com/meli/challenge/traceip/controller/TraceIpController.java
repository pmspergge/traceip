package com.meli.challenge.traceip.controller;

import com.meli.challenge.traceip.dto.IpInfoDTO;
import com.meli.challenge.traceip.dto.StatisticsDTO;
import com.meli.challenge.traceip.model.IpQuery;
import com.meli.challenge.traceip.service.KafkaProducerService;
import com.meli.challenge.traceip.service.TraceIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TraceIpController {

    private static final Logger logger = LoggerFactory.getLogger(TraceIpController.class);

    private final TraceIpService traceIpService;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public TraceIpController(TraceIpService traceIpService, KafkaProducerService kafkaProducerService) {
        this.traceIpService = traceIpService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @GetMapping("/trace-ip")
    public ResponseEntity<IpInfoDTO> traceIp(@RequestParam String ip) {
        logger.info("Received request to /api/trace-ip with IP: {}", ip);
        kafkaProducerService.sendIpTraceRequest(ip);
        IpQuery savedIpQuery = traceIpService.saveIpQueryAndTrace(ip);
        IpInfoDTO ipInfoDTO = traceIpService.getIpInfo(ip);
        return ResponseEntity.ok(ipInfoDTO);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        StatisticsDTO stats = traceIpService.getStatistics();
        return ResponseEntity.ok(stats);
    }


}
