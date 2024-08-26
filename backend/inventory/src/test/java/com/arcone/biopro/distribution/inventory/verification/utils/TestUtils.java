package com.arcone.biopro.distribution.inventory.verification.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Component
@Slf4j
public class TestUtils {

    @Value("${redpanda.url}")
    private String redpandaUrl;

    public void kafkaSender(String key,String data, String topic) throws Exception {
        StringBuilder dataBuilder = getStringBuilder(topic, key, data);

        log.info("BODY: {}", dataBuilder);
        log.info("TOPIC: {}", topic);
        log.info("DATA: {}", data);

        WebClient
            .builder()
            .baseUrl(redpandaUrl)
            .build()
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dataBuilder.toString())
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private static @NotNull StringBuilder getStringBuilder(String topic, String key, String data) {
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        String encodedKey = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));

        String json = """
            {
                "topic": "%s",
                "partitionId": -1,
                "compression": "COMPRESSION_TYPE_SNAPPY",
                "key": {
                    "encoding": "PAYLOAD_ENCODING_TEXT",
                    "data": "%s"
                },
                "value": {
                    "encoding": "PAYLOAD_ENCODING_JSON",
                    "data": "%s"
                }
            }
            """.formatted(topic, encodedKey, encodedData);

        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(json);
        return dataBuilder;
    }
}
