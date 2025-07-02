package com.arcone.biopro.distribution.irradiation.infrastructure.service;

import com.arcone.biopro.distribution.irradiation.domain.service.TextConfigService;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.TextConfigEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.TextConfigEntityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TextConfigServiceImpl implements TextConfigService {



    TextConfigEntityRepository repository;

    ConcurrentMap<String, String> textConfigCache = new ConcurrentHashMap<>();


    @EventListener(ContextRefreshedEvent.class)
    public void initializeCache() {
        loadAllTexts();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void loadAllTexts() {
        List<TextConfigEntity> allTexts = repository.findAll().collectList().block();
        if (allTexts != null) {
            allTexts.forEach(textConfig ->
                textConfigCache.put(getContextAndKey(textConfig.getContext(), textConfig.getKeyCode()), textConfig.getText())
            );
        }
    }

    @Override
    public String getText(String context, String keyCode) {
        if (textConfigCache.containsKey(getContextAndKey(context, keyCode))) {
            return textConfigCache.get(getContextAndKey(context, keyCode));
        }
        if (textConfigCache.containsKey(getContextAndKey(context, "DEFAULT"))) {
            var messageTemplate = textConfigCache.get(getContextAndKey(context, "DEFAULT"));
            return String.format(messageTemplate, convertToTitleCase(keyCode));
        }
        return getDefault(keyCode);
    }

    private String getDefault(String keyCode) {
        if (keyCode == null || keyCode.isEmpty()) {
            return "";
        }

        return StringUtils.capitalize(keyCode.replaceAll("_", " "));
    }

    private String convertToTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input.contains("_")) {
            return Arrays.stream(input.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
        }
        return input;
    }

    private String getContextAndKey(String context, String keyCode) {
        return String.format("%s-%s", context, keyCode);
    }
}
