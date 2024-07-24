package com.arcone.biopro.distribution.partnerorderproviderservice.application.validation;

import com.networknt.schema.ValidationMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonValidationFailedException extends RuntimeException {

    public static final String REQUIRED_FIELD_ERROR_CODE = "1028";

    private final Set<ValidationMessage> validationMessages;

    public JsonValidationFailedException(Set<ValidationMessage> validationMessages) {
        super("Json validation failed: " + validationMessages);
        this.validationMessages = validationMessages;
    }

    public List<Map<String, Object>> getErrorsList() {
        return validationMessages.stream().map(validation -> {
            Map<String, String> summaryAndField = getSummaryAndField(validation);

            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("code", Integer.valueOf(validation.getCode()));
            errorMap.put("summary", summaryAndField.get("summary"));
            errorMap.put("description", validation.getMessage());
            errorMap.put("field", summaryAndField.get("field"));

            return errorMap;
        }).collect(Collectors.toList());
    }

    Map<String, String> getSummaryAndField(ValidationMessage validationMessage) {
        if (validationMessage == null) {
            return null;
        }

        String path = validationMessage.getPath().substring(validationMessage.getPath().length()>2 ? 2 : 0);
        String code = validationMessage.getCode();

        if (code.equals(REQUIRED_FIELD_ERROR_CODE) || !path.contains(".")) {
            path = String.format("%s.%s", path, validationMessage.getArguments()[0]);
        }

        path = path.replaceFirst("drive.","").replaceAll("\\d", "").replaceAll("[\\[\\](){}]","");

        String summary = String.format("%s-%s", path, code);

        return Map.of("summary", summary, "field", path);
    }
}
