package com.arcone.biopro.distribution.irradiation.verification.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LogMonitor extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> logEvents = new ArrayList<>();
    private CountDownLatch latch;
    private String expectedMessage;

    public LogMonitor() {
        this.latch = new CountDownLatch(1);;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        logEvents.add(eventObject);

        if (expectedMessage != null && eventObject.getFormattedMessage().matches(expectedMessage)) {
            latch.countDown();
            log.info("Lock released " + latch.getCount() + " to message " + expectedMessage);
            expectedMessage = null;
            logEvents.clear();
            this.latch = new CountDownLatch(1);
        }
    }

    public void await(String message) throws InterruptedException {
        expectedMessage = ".*" + message + ".*";
        latch.await(60, TimeUnit.SECONDS);
        log.info("Lock acquired: " + latch.getCount() + " to message " + message);

    }
}
