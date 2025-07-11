package com.arcone.biopro.distribution.order.verification.support;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

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

    public String convertHexToRGBA(String hex) {
        String hexValue = hex.replace("#", "");
        int r = Integer.parseInt(hexValue.substring(0, 2), 16);
        int g = Integer.parseInt(hexValue.substring(2, 4), 16);
        int b = Integer.parseInt(hexValue.substring(4, 6), 16);
        return String.format("rgba(%d, %d, %d, 1)", r, g, b);
    }

    public String[] getCommaSeparatedList(String param) {
        return Arrays.stream(param.split(",")).map(String::trim).toArray(String[]::new);
    }

    public <R> R[] getCommaSeparatedList(String param, Function<String, R> mappingFunction, IntFunction<R[]> arrayTypeGenerator) {
        return (R[]) Arrays.stream(param.split(","))
            .map(String::trim)
            .map(mappingFunction)
            .toArray(arrayTypeGenerator);
    }

    @SuppressWarnings("unchecked")
    public <R extends Comparable<? super R>> List<Map> sortListOfMapByProperty(List<Map> listOfMap, String key, Class<R> propertyReturnType) {
        return listOfMap.stream()
            .sorted(Comparator.comparing(e -> (R) e.get(key)))
            .toList();
    }

    public String parseDateKeyword(String keyword) {
        if (keyword.equals("<tomorrow>")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            log.info("Tomorrow's date is {}", tomorrow);
            return tomorrow.toString();
        } else if (keyword.equals("<today>")) {
            LocalDate today = LocalDate.now();
            log.info("Today's date is {}", today);
            return today.toString();
        } else if(keyword.equals("<yesterday>")){
            LocalDate yesterday = LocalDate.now().plusDays(-1);
            log.info("Yesterday's date is {}", yesterday);
            return yesterday.toString();
        }
        else if (keyword.equals("<next_week>")) {
            LocalDate nextWeek = LocalDate.now().plusWeeks(1);
            log.info("Next week's date is {}", nextWeek);
            return nextWeek.toString();
        } else if(keyword.equals("<null>")) {
            log.info("Shipment date in unset");
            return null;
        } else if(keyword.equals("<today_formatted>")){
            return LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } else if(keyword.equals("<tomorrow_formatted>")){
            return LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } else if (keyword.equals("<yesterday_formatted>")){
            return LocalDate.now().plusDays(-1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        }
        else {
            log.info("Keyword {} not recognized", keyword);
            return keyword;
        }
    }

}
