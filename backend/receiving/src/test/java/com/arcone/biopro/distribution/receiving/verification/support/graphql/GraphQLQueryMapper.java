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
            """, temperatureCategory,employeeId,locationCode));
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

    public static String validateTemperature(String temperatureCategory, String temperatureValue) {
        return String.format("""
            query ValidateTemperature {
                validateTemperature(
                    validateTemperatureRequest: { temperature: %s, temperatureCategory: "%s" }
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
            """, temperatureValue, temperatureCategory);
    }

    public static String validateTransitTime(String temperatureCategory, String startDateTime, String startTimeZone, String endDateTime, String endTimeZone ) {
        return String.format("""
            query ValidateTransitTime {
                validateTransitTime(
                    validateTransitTimeRequest: {
                        temperatureCategory: "%s"
                        startDateTime: "%s"
                        startTimeZone: "%s"
                        endDateTime: "%s"
                        endTimeZone: "%s"
                    }
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

            """, temperatureCategory, startDateTime, startTimeZone, endDateTime, endTimeZone);

    }

    public static String validateBarcodeValue(String temperatureCategory,String barcodePattern, String barcodeValue) {
        return String.format("""
            query validateBarcode {
                validateBarcode(
                    validateBarcodeRequest: { temperatureCategory: "%s", barcodePattern: "%s", barcodeValue: "%s" }
                ) {
                    data
                    notifications {
                        message
                        type
                        code
                    }
                }
            }
            """, temperatureCategory, barcodePattern,barcodeValue);
    }

    public static String getImportById(String importId) {
        return String.format("""
            query findImportById {
                findImportById(
                   importId:%s
                ) {
                    _links
                    data
                    notifications {
                        message
                        type
                        code
                    }
                }
            }

            """, importId);

    }

    public static String validateInternalTransferInformation(String orderNumber, String employeeId , String locationCode  ) {
        return (String.format("""
            query {
                validateTransferOrderNumber(validateTransferOrderNumberRequest: {
                    orderNumber:%s
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
            """, orderNumber,employeeId,locationCode));
    }
}
