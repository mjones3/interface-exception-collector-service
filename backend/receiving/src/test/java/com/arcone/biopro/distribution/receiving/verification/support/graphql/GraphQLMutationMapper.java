package com.arcone.biopro.distribution.receiving.verification.support.graphql;

public class GraphQLMutationMapper {

    public static String createImportMutation(String temperatureCategory, String transitStartDateTime, String transitStartTimeZone, String transitEndDateTime, String transitEndTimeZone, String temperature, String thermometerCode, String locationCode, String comments, String employeeId) {
        return String.format("""
            mutation CreateImport {
                createImport(
                    createImportRequest: {
                        temperatureCategory: "%s"
                        transitStartDateTime: "%s"
                        transitStartTimeZone: "%s"
                        transitEndDateTime: "%s"
                        transitEndTimeZone: "%s"
                        temperature: %s
                        thermometerCode: "%s"
                        locationCode: "%s"
                        comments: "%s"
                        employeeId: "%s"
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
            """, temperatureCategory, transitStartDateTime, transitStartTimeZone, transitEndDateTime, transitEndTimeZone, temperature, thermometerCode, locationCode, comments, employeeId);
    }

    public static String createImportItemMutation(String importId,String unitNumber,String productCode,String aboRh,String expirationDate,String visualInspection,String licenseStatus, String employeeId) {
        return String.format("""
            mutation CreateImportItem {
                createImportItem(
                    createImportItemRequest: {
                        importId: %s
                        unitNumber: "%s"
                        productCode: "%s"
                        aboRh: "%s"
                        expirationDate: "%s"
                        visualInspection: "%s"
                        licenseStatus: "%s"
                        employeeId: "%s"
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
            """, importId, unitNumber, productCode, aboRh, expirationDate, visualInspection, licenseStatus, employeeId);
    }
}
