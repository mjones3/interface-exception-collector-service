package com.arcone.biopro.distribution.partnerorderprovider.application.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JsonSchemaValidatingArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;
    private final Map<String, JsonSchema> schemaCache;

    private JsonSchemaDefinitionsMessageHandler jsonSchemaDefinitionsMessageHandler;

    public JsonSchemaValidatingArgumentResolver(ObjectMapper objectMapper, ResourcePatternResolver resourcePatternResolver) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
        this.schemaCache = new ConcurrentHashMap<>();
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(ValidJson.class) != null;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        // get schema path from ValidJson annotation
        String schemaPath = parameter.getParameterAnnotation(ValidJson.class).value();

        // get JsonSchema from schemaPath
        JsonSchema schema = getJsonSchema(schemaPath);

        try {
            return getJsonPayload(exchange).map(payload -> {
                try {
                    var json = objectMapper.readTree(payload);
                    var validationSet = schema.validate(json);
                    if (validationSet.isEmpty()) {
                        return objectMapper.treeToValue(json, parameter.getParameterType());
                    } else {
                        setMessageError(validationSet);
                        throw new JsonValidationFailedException(validationSet);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Not able to parse JSON Payload {}",e.getMessage());
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception ex) {
            log.error("Not able to parse Request Payload {}",ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void setMessageError(Set<ValidationMessage> validationMessages) {
        if (validationMessages == null) {
            return;
        }

        for (ValidationMessage validationMessage : validationMessages) {
            String messageErrorForPattern = jsonSchemaDefinitionsMessageHandler.getDefinitionMessage(validationMessage);
            if (messageErrorForPattern != null) {
                try {
                    var fieldName = validationMessage.getMessage().substring(0, validationMessage.getMessage().indexOf(":")+2);
                    FieldUtils.writeDeclaredField(
                        validationMessage, "message",
                        String.format("%s%s",fieldName, messageErrorForPattern),
                        true);
                } catch (IllegalAccessException e) {
                    log.error("Error change 'validationMessage' field on ValidationMessage to {}", messageErrorForPattern);
                }
            }
        }
    }

    private Mono<String> getJsonPayload(ServerWebExchange exchange) {
        ServerHttpRequest httpServletRequest = exchange.getRequest();

        StringBuffer contentStringBuffer = new StringBuffer();

        return DataBufferUtils.join(httpServletRequest.getBody())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                String body = new String(bytes, StandardCharsets.UTF_8);
                contentStringBuffer.append(body);
                log.debug("Request Body: {}", body);
                return Mono.just(String.valueOf(contentStringBuffer));
            });

    }



    public JsonSchema getJsonSchema(String schemaPath) {
        return schemaCache.computeIfAbsent(schemaPath, path -> {

            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            try {
                this.jsonSchemaDefinitionsMessageHandler = objectMapper.readValue(new ClassPathResource(path).getInputStream(), JsonSchemaDefinitionsMessageHandler.class);
                return schemaFactory.getSchema(new ClassPathResource(path).getInputStream());
            } catch (Exception e) {
                throw new JsonSchemaLoadingFailedException("An error occurred while loading JSON Schema, path: " + path, e);
            }
        });
    }

}
