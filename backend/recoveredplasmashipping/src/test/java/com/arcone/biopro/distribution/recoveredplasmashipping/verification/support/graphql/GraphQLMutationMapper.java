package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql;

import java.util.Date;

public class GraphQLMutationMapper {

    public static String createShipment(String customerCode, String productType, Float cartonTareWeight, String scheduledDate, String TransportationRefNumber, String locationCode) {
        return (String.format("""
            mutation {
                createShipment(
            createShipmentRequest: {
                customerCode: %s
                productType: %s
                cartonTareWeight: %s
                scheduleDate: %s
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
    """, customerCode, productType, cartonTareWeight, scheduledDate, TransportationRefNumber, locationCode));
    }

}
