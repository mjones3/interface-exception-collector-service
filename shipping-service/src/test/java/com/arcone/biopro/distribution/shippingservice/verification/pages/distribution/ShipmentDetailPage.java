package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@Slf4j
public class ShipmentDetailPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Value("${ui.base.url}")
    private String baseUrl;

    @Value("${ui.shipment-details.url}")
    private String shipmentDetailsUrl;

    @FindBy(how = How.ID , using = "viewPickListBtn")
    private WebElement viewPickListButton;


    @FindBy(id = "prodTableId")
    private WebElement productTable;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(productTable);
    }

    public void openViewPickListModal(){
        sharedActions.waitForVisible(viewPickListButton);
        sharedActions.click(viewPickListButton);
    }

    public void goTo(Long shipmentId) {
        var url = baseUrl+shipmentDetailsUrl.replace("{shipmentId}",String.valueOf(shipmentId));
        this.driver.get(url);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void waitForLoad(){
        sharedActions.waitForVisible(productTable);
    }
}
