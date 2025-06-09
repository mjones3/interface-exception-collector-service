package com.arcone.biopro.distribution.receiving.verification.support.graphql;

public class GraphQLQueryMapper {
    public static String enterShippingInformation(String temperatureCategory, String employeeId , String locationCode  ) {
        return (String.format("""
            query {
                enterShippingInformation(enterShippingInformationRequest: {
                    productCategory:"%s"
                    employeeId:"%s"
                    locationCode:"%s"
                }) {
                   data
                           notifications{
                               message
                               type
                               code

                           }
                           _links
                }
            }
            """, temperatureCategory,employeeId,temperatureCategory));
    }

    public static String validateDevice(String bloodCenterId, String locationCode) {
        return String.format("""
            query ValidateDevice {
                validateDevice(
                    validateDeviceRequest: { bloodCenterId: "%s", locationCode: "%s" }
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                        action
                        reason
                        details
                        name
                    }
                }
            }
            """, bloodCenterId, locationCode);
    }
}
