package com.arcone.biopro.distribution.partnerorderproviderservice.application.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.networknt.schema.ValidationMessage;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSchemaDefinitionsMessageHandler {

    public Map<String, Map<String, Object>> definitions;

    public String getDefinitionMessage(ValidationMessage message) {
        if (definitions == null || message == null) {
            return null;
        }

        String definitionMessage = null;

        for (var definitionName : definitions.keySet()) {

            Map<String, Object> definition = definitions.get(definitionName);

            definitionMessage = getGenericDefinitionMessage(message, definitionName, definition);

            if (definitionMessage == null) {
                definitionMessage = getDefinitionMessageFromSharedDefinition(message, definition);
                if (definitionMessage != null) {
                    break;
                }
            }
        }

        return definitionMessage;
    }

    protected String getGenericDefinitionMessage(ValidationMessage message, String definitionName, Map<String, Object> definition) {
        if (definitionName.equals(message.getPath().replace("$.",""))) {
            Object definitionMessage = definition.get("message");
            if (definitionMessage != null) {
                return definitionMessage.toString();
            }
        }
        return null;
    }

    protected String getDefinitionMessageFromSharedDefinition(ValidationMessage message, Map<String, Object> definition) {
        if (definition.containsKey("type")
            && definition.get("type").equals("string")
            && definition.containsKey("pattern")
            && definition.containsKey("message")
            && message.getMessage().contains(definition.get("pattern").toString())) {
            return definition.get("message").toString();
        }
        return null;
    }


}



