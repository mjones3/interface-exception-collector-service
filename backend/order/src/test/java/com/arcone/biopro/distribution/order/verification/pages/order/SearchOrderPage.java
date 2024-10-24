package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.controllers.OrderController;
import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SearchOrderPage extends CommonPageFactory {

    OrderController orderController = new OrderController();

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Value("${ui.base.url}")
    private String baseUrl;


    //    Static locators and elements
    @FindBy(xpath = "//h3/*[text()='Search Orders']")
    private WebElement searchOrdersTitle;

    @FindBy(xpath = "//*[@class='p-datatable-loading-overlay']")
    private WebElement tableLoadingOverlay;

    @FindAll({
        @FindBy(xpath = "//tr[contains(@id,'order-table-row')]//td[position()=3]")
    })
    private List<WebElement> orderPriorityList;

    //    Dynamic locators
    private String orderDetailsButtonXpath(String orderId) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td/button[@id='detailsBtn']", orderId);
    }

    private String orderIdXpath(String orderId) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]", orderId);
    }

    private String orderStatusXpath(String orderId, String status) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td[contains(text(),'%s')]", orderId, status);
    }

    private String orderPriorityXpath(String orderId, String priority) {
        return String.format("//tr[contains(@id,'order-table-row')]/child::td[contains(text(),'%s')]/following-sibling::td/span[contains(text(),'%s')]", orderId, priority);
    }

    @Override
    public boolean isLoaded() {
        sharedActions.waitForVisible(searchOrdersTitle);
        sharedActions.waitForNotVisible(tableLoadingOverlay);
        return sharedActions.isElementVisible(searchOrdersTitle);
    }

    public void goTo() {
        driver.get(baseUrl + "/orders/search");
        Assert.assertTrue(isLoaded());
    }

    public void validateOrderDetails(String externalId, String orderStatus, String orderPriority) throws InterruptedException {
        sharedActions.waitLoadingAnimation();
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderIdXpath(externalId))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderStatusXpath(externalId, orderStatus))));
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderPriorityXpath(externalId, orderPriority))));
    }

    public WebElement getPriorityElement(String externalId, String orderPriority) {
        return driver.findElement(By.xpath(orderPriorityXpath(externalId, orderPriority)));
    }

    public void verifyOrderDetailsOption(String externalId) {
        sharedActions.waitForVisible(driver.findElement(By.xpath(orderDetailsButtonXpath(externalId))));
    }

    public void verifyOrderNotExists(String externalId) {
        try {
            sharedActions.waitForVisible(driver.findElement(By.xpath(orderIdXpath(externalId))));
            Assert.fail("Order " + externalId + " exists in the list of orders.");
        } catch (Exception e) {
            log.info("Order " + externalId + " does not exist in the list of orders.");
        }
    }

    public void verifyPriorityOrderList() {

        sharedActions.waitForVisible(orderPriorityList.getFirst());

        var priorityList = orderPriorityList.stream().toList().stream().map(WebElement::getText)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        log.info("Configured priority is: {}", orderController.priorities.keySet());
        log.info("Priority shown in UI: {}", priorityList);

        Assert.assertTrue(orderController.isPriorityOrdered(priorityList));
    }


    public List<WebElement> getOrderPriorityList() {
        return orderPriorityList.stream().toList();
    }

    public void searchOrder(String value) {
        // Implement type and search actions
    }
}
