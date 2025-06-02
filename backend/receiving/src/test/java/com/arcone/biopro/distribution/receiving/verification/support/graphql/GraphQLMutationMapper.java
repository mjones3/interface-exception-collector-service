package com.arcone.biopro.distribution.receiving.verification.support.graphql;

public class GraphQLMutationMapper {

    public static String createShipment(String customerCode, String productType, Float cartonTareWeight, String shipmentDate, String TransportationRefNumber, String locationCode) {
        return (String.format("""
            mutation {
                createShipment(
            createShipmentRequest: {
                customerCode: %s
                productType: %s
                cartonTareWeight: %s
                shipmentDate: %s
                transportationReferenceNumber: %s
                locationCode: %s
                createEmployeeId: "4c973896-5761-41fc-8217-07c5d13a004b"
            }
        ) {
            data
            notifications {
                code
                type
                message
            }
            _links
        }
    }
    """, customerCode, productType, cartonTareWeight, shipmentDate, TransportationRefNumber, locationCode));
    }
}
