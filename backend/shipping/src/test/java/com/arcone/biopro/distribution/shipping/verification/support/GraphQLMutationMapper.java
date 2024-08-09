package com.arcone.biopro.distribution.shipping.verification.support;

public class GraphQLMutationMapper {

    public static String packItemMutation(Long shipmentItemId , String locationCode , String unitNumber , String employeeId
        , String productCode , String visualInspection) {
        return (String.format(
            """
                mutation {
                    packItem(packItemRequest:{
                        shipmentItemId:%s
                        , locationCode:"%s"
                        , unitNumber:"%s"
                        , employeeId:"%s"
                        , productCode:"%s"
                        , visualInspection:%s
                    }){
                        ruleCode
                        results
                        notifications{
                            statusCode
                            notificationType
                            message

                        }
                        _links
                    }
                }
                """
            , shipmentItemId , locationCode ,unitNumber , employeeId , productCode , visualInspection ));
    }

    public static String completeShipmentMutation(Long shipmentId , String employeeId) {
        return (String.format(
            """
                mutation {
                    completeShipment(completeShipmentRequest: { shipmentId: %s, employeeId: "%s" }){
                        ruleCode
                        results
                        notifications {
                            statusCode
                            notificationType
                            message
                         }

                         _links
                    }
                }
                """
            , shipmentId , employeeId));
    }
}




