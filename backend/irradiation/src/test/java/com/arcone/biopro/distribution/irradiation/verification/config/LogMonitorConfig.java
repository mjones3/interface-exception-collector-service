package com.arcone.biopro.distribution.irradiation.verification.config;

import ch.qos.logback.classic.Logger;
import com.arcone.biopro.distribution.irradiation.verification.utils.LogMonitor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogMonitorConfig {

    @Bean
    public LogMonitor logMonitor() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LogMonitor logMonitor = new LogMonitor();
        logMonitor.start();
        rootLogger.addAppender(logMonitor);

        return logMonitor;
    }
}
