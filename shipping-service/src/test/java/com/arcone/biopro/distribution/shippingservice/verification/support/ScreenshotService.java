package com.arcone.biopro.distribution.shippingservice.verification.support;

import io.qameta.allure.Allure;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;


@Component
@Slf4j
@Setter
public class ScreenshotService implements TestWatcher {

    @Autowired
    protected ApplicationContext ctx;

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        log.info("Test failed. Capturing screenshot...");
        attachScreenshot();
    }

    public void attachScreenshot() {
        var driver = ctx.getBean(WebDriver.class);
        if (driver instanceof TakesScreenshot) {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (screenshotBytes == null) {
                log.info("Failed to capture screenshot. Bytes are null.");
            } else {
                log.info("Screenshot captured. Attaching to Allure report...");
                Allure.addAttachment("Screenshot", new ByteArrayInputStream(screenshotBytes));
                log.info("Screenshot attached to Allure report.");
            }
        } else {
            log.info("Driver does not support screenshots.");
        }
    }

    public void attachConditionalScreenshot(boolean condition) {
        if (condition) {
            attachScreenshot();
        } else {
            log.info("Skipping screenshot attachment due to configuration.");
        }
    }
}
