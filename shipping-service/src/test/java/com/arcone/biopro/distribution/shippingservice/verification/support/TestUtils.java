package com.arcone.biopro.distribution.shippingservice.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import org.apache.commons.io.FileUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Component
@Slf4j
public class TestUtils {

    @Value("${redpanda.url}")
    private String redpandaUrl;

    public static String resource(String fileName) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return FileUtils.readFileToString(new File(resource.toURI()));

    }

    public void kafkaSender(String resourceName, String topic) throws Exception {
        String data = resource(resourceName);
        StringBuilder dataBuilder = getStringBuilder(topic, data);

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

    private static @NotNull StringBuilder getStringBuilder(String topic, String data) {
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));

        String json = """
            {
                "topic": "%s",
                "partitionId": -1,
                "compression": "COMPRESSION_TYPE_SNAPPY",
                "key": {
                    "encoding": "PAYLOAD_ENCODING_TEXT"
                },
                "value": {
                    "encoding": "PAYLOAD_ENCODING_JSON",
                    "data": "%s"
                }
            }
            """.formatted(topic, encodedData);

        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(json);
        return dataBuilder;
    }
}
