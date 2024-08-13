package com.arcone.biopro.distribution.order.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

@Component
@Slf4j
public class TestUtils {

    @Value("${default.ui.facility}")
    private String defaultFacility;

    public String getResource(String fileName) throws Exception {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        return FileUtils.readFileToString(new File(resource.toURI()));
    }

    public String formatSqlCommaSeparatedInParamList(String param) {
        var paramList = Arrays.stream(param.split(",")).map(String::trim).toArray(String[]::new);
        var formattedParam = String.join("','", paramList);
        return String.format("('%s')", formattedParam);
    }

    public void setFacilityCookie(String facilityId, WebDriver driver) {
        log.info("Setting facility cookie to {}", facilityId);
        driver.manage().addCookie(new Cookie("x-facility-id", facilityId));
    }

    public void setFacilityCookie(WebDriver driver) {
        log.info("Setting facility cookie to {}", this.defaultFacility);
        this.setFacilityCookie(this.defaultFacility, driver);
    }

}
