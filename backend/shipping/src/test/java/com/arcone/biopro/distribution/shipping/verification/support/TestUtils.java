package com.arcone.biopro.distribution.shipping.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Cookie;
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

    @Value("${default.ui.facility}")
    private String defaultFacility;

    /**
     * This method is used to read the contents of a file as a string.
     * It first gets the ClassLoader of the TestUtils class, then uses it to get a URL to the file.
     * It then converts this URL to a URI, which is used to create a File object.
     * Finally, it reads the contents of the file into a string using FileUtils.readFileToString and returns it.
     *
     * @param fileName The name of the file to read. This should be the path to the file relative to the classpath.
     * @return A string containing the contents of the file.
     * @throws Exception If there is an error accessing the file or reading its contents.
     */
    public String getResource(String fileName) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return FileUtils.readFileToString(new File(resource.toURI()));

    }

    /**
     * This method is used to send a Kafka message.
     * It first creates a JSON string that represents the Kafka message using the getStringBuilder method.
     * The message includes the topic, partitionId, compression type, key encoding, value encoding, and the data.
     * The data is Base64 encoded before being included in the JSON.
     * It then logs the body of the message, the topic, and the data.
     * Finally, it sends the message using a WebClient.
     *
     * @param resource The data to be sent in the message. This data will be Base64 encoded.
     * @param topic    The topic to which the message will be sent.
     */
    public void kafkaSender(String resource, String topic) {
        StringBuilder dataBuilder = getStringBuilder(topic, resource);

        log.info("BODY: {}", dataBuilder);
        log.info("TOPIC: {}", topic);
        log.info("DATA: {}", resource);

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

    /**
     * This method is used to create a JSON string that represents a Kafka message.
     * The message includes the topic, partitionId, compression type, key encoding, value encoding, and the data.
     * The data is Base64 encoded before being included in the JSON.
     *
     * @param topic The topic to which the message will be sent.
     * @param data  The data to be sent in the message. This data will be Base64 encoded.
     * @return A StringBuilder object containing the JSON string.
     */
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

    public void setFacilityCookie(String facilityId, WebDriver driver) {
        log.info("Setting facility cookie to {}", facilityId);
        driver.manage().addCookie(new Cookie("x-facility-id", this.defaultFacility));
    }

    public void setFacilityCookie(WebDriver driver) {
        log.info("Setting facility cookie to {}", this.defaultFacility);
        this.setFacilityCookie(this.defaultFacility, driver);
    }

}
