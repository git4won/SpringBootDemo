package com.example.demo.service;

import com.example.demo.kafka.Event;
import com.example.demo.kafka.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaService {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Scheduled(initialDelay = 5000, fixedDelay = 2000)
    public void send() {

        String id = UUID.randomUUID().toString();
        String type = "test";
        String source = "A";
        String data = new Date().toString();

        Event<String> event = new Event<>(id, type, source, data);
        try {
            producerTemplate.sendEvent(event);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
