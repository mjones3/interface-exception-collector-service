package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.ConfigurationEntityRepository;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity.ConfigurationEntity;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZonedDateTime;

import static com.arcone.biopro.distribution.irradiation.BioProConstants.USE_CHECK_DIGIT;

@ContextConfiguration
public class ConfigurationSteps {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationSteps.class);

    @Autowired
    private IrradiationContext irradiationContext;

    @Autowired
    private ConfigurationEntityRepository configurationEntityRepository;

    @Given("the {string} is configured as {string}")
    public void theConfigurationIsConfiguredAs(String configKey, String configValue) {
        log.info("Setting configuration {} to {}", configKey, configValue);
        String key = "";
        if(configKey.equals("Check Digit")){
            key = USE_CHECK_DIGIT;
        }
        ConfigurationEntity configEntity = ConfigurationEntity.builder()
            .key(key)
            .value(configValue)
            .active(true)
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now())
            .build();

        configurationEntityRepository.save(configEntity)
            .block();
    }
}
