package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VerifyProductsPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${ui.base.url}")
    private String baseUrl;

    @Value("${ui.shipment-verify-products.url}")
    private String verifyProductsUrl;

    @FindBy(id = "prodTableId")
    private WebElement verifiedProductsTable;

    @FindBy(xpath = "//*[@id='informationDetails-Labeling-Product-Category']")
    private WebElement productCategory;

    @FindBy(xpath = "//*[@id='informationDetails-Shipping-Method']")
    private WebElement shippingMethodElement;

    @FindBy(xpath = "//*[@id='informationDetails-BioPro-Order-ID']")
    private WebElement orderNumber;

    @FindBy(id = "informationDetails-Priority")
    private WebElement orderPriority;

    @FindBy(xpath = "//*[@id='informationDetails-Customer-Code']")
    private WebElement customerId;

    @FindBy(xpath = "//*[@id='informationDetails-Customer-Name']")
    private WebElement customerName;

    @FindBy(id = "informationDetails-Status")
    private WebElement orderStatus;

    @FindBy(id = "informationDetails-External-Order-ID")
    private WebElement externalId;

    private static final String completeShipmentButton = "completeActionBtn";


    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(verifiedProductsTable);
    }

    public void isPageOpen(String shipmentId){
        sharedActions.isAtPage(verifyProductsUrl.replace("{shipmentId}", shipmentId));
    }

    public void viewPageContent() {
        waitForElementsVisible(shippingMethodElement, orderNumber, orderPriority, customerId, customerName, orderStatus, externalId);
    }

    private void waitForElementsVisible(WebElement... elements) {
        for (WebElement element : elements) {
            sharedActions.waitForVisible(element);
        }
    }

}
