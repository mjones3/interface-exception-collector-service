package com.arcone.biopro.distribution.shippingservice.verification.pages.distribution;

import com.arcone.biopro.distribution.shippingservice.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shippingservice.verification.pages.SharedActions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ViewPickListPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @FindBy(how = How.ID , using = "ViewPickListDialog")
    private WebElement ViewPickListDialog;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(ViewPickListDialog);
    }

    public Map<String,String> getShipmentDetailsTableContent(){
        var pickListTable =  this.ViewPickListDialog.findElement(By.id("pickListTable"));

        List<WebElement> rowElements = pickListTable.findElements(By.xpath(".//tr"));

        List<WebElement> cellElements = rowElements.get(1).findElements(By.xpath(".//td"));

        var shipmentDetails = new HashMap<String,String>();
        shipmentDetails.put("orderNumber",cellElements.get(0).getText());
        shipmentDetails.put("customerId",cellElements.get(1).getText());
        shipmentDetails.put("customerName",cellElements.get(2).getText());

        return shipmentDetails;
    }

    public Map<String,String> getProductDetailsTableContent(){
        var pickListTable =  this.ViewPickListDialog.findElement(By.id("productDetailsTable"));

        List<WebElement> rowElements = pickListTable.findElements(By.xpath(".//tr"));

        var productDetails = new HashMap<String,String>();
        for (int i=1 ; i < rowElements.size() ; i++){
            var cellElements = rowElements.get(i).findElements(By.xpath(".//td"));
            var contentLine = cellElements.get(0).getText() + ":"+cellElements.get(1).getText()+":"+cellElements.get(2).getText();
            productDetails.put(contentLine,contentLine);
        }

        return productDetails;
    }

    public Map<String,String> getShortDateProductDetailsTableContent(){
        var shortDateDetailsTable =  this.ViewPickListDialog.findElement(By.id("shortDateDetailsTable"));

        List<WebElement> rowElements = shortDateDetailsTable.findElements(By.xpath(".//tr"));

        var shortDateProductDetails = new HashMap<String,String>();
        for (int i=1 ; i < rowElements.size() ; i++){
            var cellElements = rowElements.get(i).findElements(By.xpath(".//td"));
            var contentLine = cellElements.get(0).getText() + ":"+cellElements.get(1).getText()+":"+cellElements.get(2).getText();
            shortDateProductDetails.put(contentLine,contentLine);
        }

        return shortDateProductDetails;
    }

    public String getNoShortDateMessageContent(){
        var noShortDateDateMessage =  this.ViewPickListDialog.findElement(By.id("shortDateDetailsEmpty"));
        return noShortDateDateMessage.getText();
    }

}
