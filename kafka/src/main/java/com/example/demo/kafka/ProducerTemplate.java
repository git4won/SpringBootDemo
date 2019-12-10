package com.example.demo.kafka;

import com.example.demo.utils.JsonUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ProducerTemplate extends KafkaTemplate<String, String> {

    private String source;
    private String topic;


    public ProducerTemplate(String source, String topic, ProducerFactory<String, String> producerFactory) {
        this(source, topic, producerFactory, false);
    }

    public ProducerTemplate(String source, String topic, ProducerFactory<String, String> producerFactory, boolean autoFlush) {
        super(producerFactory, autoFlush);
        this.source = source;
        this.topic = topic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public <T> SendResult<String, String> sendEvent(String type, T data) throws ExecutionException, InterruptedException {
        String dataJson = JsonUtils.pojoToJson(data);
        String id = UUID.randomUUID().toString();
        Event<String> event = new Event<>(id, type, source, dataJson);
        return sendEvent(event);
    }

    public <T> SendResult<String, String> sendEvent(Event<T> event) throws ExecutionException, InterruptedException {
        String eventJson = JsonUtils.pojoToJson(event);

        if (eventJson == null) {
            // TODO
        }

        ListenableFuture<SendResult<String, String>> future = this.send(topic, event.getId(), eventJson);
        return future.get();
    }
}
