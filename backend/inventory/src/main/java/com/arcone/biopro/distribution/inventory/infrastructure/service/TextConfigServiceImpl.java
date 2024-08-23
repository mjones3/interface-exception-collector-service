package com.arcone.biopro.distribution.inventory.infrastructure.service;

import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.TextConfigEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.TextConfigEntityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
                textConfigCache.put(textConfig.getKeyCode(), textConfig.getText())
            );
        }
    }

    @Override
    public String getText(String keyCode) {
        return textConfigCache.computeIfAbsent(keyCode, this::getDefault);
    }

    private String getDefault(String keyCode) {
        if (keyCode == null || keyCode.isEmpty()) {
            return "";
        }

        return StringUtils.capitalize(keyCode.replaceAll("_", " "));
    }
}
