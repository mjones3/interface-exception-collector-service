package com.arcone.biopro.distribution.receiving.verification.support.graphql;

import org.jetbrains.annotations.Nullable;

public class GraphQLMutationMapper {

    public static String createImportMutation(String temperatureCategory, String transitStartDateTime, String transitStartTimeZone, String transitEndDateTime, String transitEndTimeZone, String temperature, String thermometerCode, String locationCode, String comments, String employeeId) {
        return String.format("""
            mutation CreateImport {
                createImport(
                    createImportRequest: {
                        temperatureCategory: %s
                        transitStartDateTime: %s
                        transitStartTimeZone: %s
                        transitEndDateTime: %s
                        transitEndTimeZone: %s
                        temperature: %s
                        thermometerCode: %s
                        locationCode: %s
                        comments: %s
                        employeeId: %s
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
            """, formatNullString(temperatureCategory), formatNullString(transitStartDateTime), formatNullString(transitStartTimeZone), formatNullString(transitEndDateTime), formatNullString(transitEndTimeZone), temperature, formatNullString(thermometerCode), formatNullString(locationCode), formatNullString(comments), formatNullString(employeeId));
    }

    private static @Nullable String formatNullString(String field) {
        return field != null ? "\"" + field + "\"" : null;
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

    public static String completeImportMutation(String importId,String employeeId) {
        return String.format("""
            mutation completeImport {
                completeImport(
                    completeImportRequest: {
                        importId: %s
                        completeEmployeeId: "%s"
                    }
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
            """, importId, employeeId);
    }
}
