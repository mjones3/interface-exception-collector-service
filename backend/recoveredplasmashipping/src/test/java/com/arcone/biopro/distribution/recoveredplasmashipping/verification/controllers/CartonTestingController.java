package com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql.GraphQLQueryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CartonTestingController {

    @Autowired
    private ApiHelper apiHelper;
    @Autowired
    private SharedContext sharedContext;
    @Autowired
    private DatabaseService databaseService;

    public Map createCarton(String shipmentId) {
        String payload = GraphQLMutationMapper.createCarton(shipmentId);
        var response = apiHelper.graphQlRequest(payload, "createCarton");
        addCartonToList((Map) response.get("data"));
        return response;
    }

    public Map createCarton(int shipmentId) {
        return createCarton(String.valueOf(shipmentId));
    }

    private void addCartonToList(Map carton) {
        List<Map> cartonList = sharedContext.getCreateCartonResponseList();
        if (cartonList == null) {
            cartonList = new ArrayList<>();
        }
        cartonList.add(carton);
        sharedContext.setCreateCartonResponseList(cartonList);
    }

    public void packCartonProduct(String cartonId, String unitNumber, String productCode, String locationCode) {
        String payload = GraphQLMutationMapper.packCartonItem(Integer.parseInt(cartonId), unitNumber, productCode, locationCode);
        var response = apiHelper.graphQlRequest(payload, "packCartonItem");
        var packedProducts = (List) ((Map) response.get("data")).get("packedProducts");
        sharedContext.setPackedProductsList(packedProducts);

        sharedContext.setLastCartonResponse((Map) response.get("data"));
    }

    public boolean checkProductIsPacked(String unitNumber, String productCode) {
        var packedList = sharedContext.getPackedProductsList();
        if (packedList.isEmpty()) {
            return false;
        } else {
            return packedList.stream().anyMatch(packedProduct -> {
                var packedUnitNumber = packedProduct.get("unitNumber");
                var packedProductCode = packedProduct.get("productCode");
                return unitNumber.equals(packedUnitNumber.toString()) && productCode.equals(packedProductCode.toString());
            });
        }
    }

    public boolean checkProductIsVerified(String unitNumber, String productCode) {
        var verifiedList = sharedContext.getVerifiedProductsList();
        if (verifiedList.isEmpty()) {
            return false;
        } else {
            return verifiedList.stream().anyMatch(verifiedProduct -> {
                var verifiedUnitNumber = verifiedProduct.get("unitNumber");
                var verifiedProductCode = verifiedProduct.get("productCode");
                return unitNumber.equals(verifiedUnitNumber.toString()) && productCode.equals(verifiedProductCode.toString());
            });
        }
    }

    public void insertPackedProduct(String cartonId, String unitNumber, String productCode, String productType) {
        databaseService.executeSql(DatabaseQueries.INSERT_PACKED_PRODUCT(cartonId, unitNumber, productCode, productType)).block();
    }

    public void verifyCartonProduct(String cartonId, String unitNumber, String productCode, String locationCode) {
        String payload = GraphQLMutationMapper.verifyCarton(Integer.parseInt(cartonId), unitNumber, productCode, locationCode);
        var response = apiHelper.graphQlRequest(payload, "verifyCarton");

        var verifiedProducts = (List) ((Map) response.get("data")).get("verifiedProducts");
        sharedContext.setVerifiedProductsList(verifiedProducts);

        var packedProducts = (List) ((Map) response.get("data")).get("packedProducts");
        sharedContext.setPackedProductsList(packedProducts);

        sharedContext.setLastCartonResponse((Map) response.get("data"));

        sharedContext.setLinksResponse((Map) response.get("_links"));
    }

    public void closeCarton(String id, String employeeId, String locationCode) {
        String payload = GraphQLMutationMapper.closeCarton(id, employeeId, locationCode);
        var response = apiHelper.graphQlRequest(payload, "closeCarton");
        sharedContext.setLastCloseCartonResponse((Map) response.get("data"));
        if (response.get("data") != null) {
            sharedContext.setLastCartonResponse((Map) response.get("data"));
        }
    }

    public void printCartonPackingSlip(String cartonId, String locationCode) {
        String payload = GraphQLQueryMapper.generateCartonPackingSlip(Integer.parseInt(cartonId), sharedContext.getEmployeeId(),locationCode);
        var response = apiHelper.graphQlRequest(payload, "generateCartonPackingSlip");
        sharedContext.setLastCartonPackingSlipResponse((Map) response.get("data"));
    }
}
