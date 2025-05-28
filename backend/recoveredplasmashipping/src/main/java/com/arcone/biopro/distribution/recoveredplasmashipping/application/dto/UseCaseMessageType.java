package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UseCaseMessageType {

    INVENTORY_SERVICE_IS_DOWN(1,  UseCaseNotificationType.SYSTEM, "Inventory Service is down."),
    SHIPMENT_CREATED_SUCCESS(2,  UseCaseNotificationType.SUCCESS, "Shipment created successfully"),
    CARTON_CREATED_SUCCESS(3,  UseCaseNotificationType.SUCCESS, "Carton created successfully"),
    CARTON_GENERATION_ERROR(6,  UseCaseNotificationType.SYSTEM, "Carton generation error. Contact Support."),
    CARTON_ITEM_PACKED_SUCCESS(7,  UseCaseNotificationType.SUCCESS, "Carton Item packed successfully"),
    VERIFY_CARTON_ITEM_SUCCESS(8,  UseCaseNotificationType.SUCCESS, "Carton Item verified successfully"),
    CARTON_VERIFICATION_ERROR(9,  UseCaseNotificationType.SYSTEM, "Carton verification error. Contact Support."),
    CARTON_ITEM_PACKED_ERROR(10,  UseCaseNotificationType.SYSTEM, "Carton Item packed error. Contact Support."),
    CARTON_CLOSED_SUCCESS(11,  UseCaseNotificationType.SUCCESS, "Carton closed successfully"),
    CARTON_CLOSED_ERROR(12,  UseCaseNotificationType.SYSTEM, "Close Carton error. Contact Support."),
    CARTON_PACKING_SLIP_GENERATED_SUCCESS(13,  UseCaseNotificationType.SUCCESS, "Carton Packing Slip generated successfully"),
    CARTON_PACKING_SLIP_GENERATED_ERROR(14,  UseCaseNotificationType.SYSTEM, "Carton Packing Slip generation error. Contact Support."),
    SHIPMENT_PROCESSING_SUCCESS(15,  UseCaseNotificationType.SUCCESS, "Close Shipment is in progress"),
    UNACCEPTABLE_UNITS_REPORT_PRINT_SUCCESS(16,  UseCaseNotificationType.SUCCESS, "Unacceptable Products report generated successfully"),
    UNACCEPTABLE_UNITS_REPORT_PRINT_ERROR(17,  UseCaseNotificationType.SYSTEM, "Unacceptable Products report generation error. Contact Support."),
    CARTON_REPACKED_SUCCESS(18,  UseCaseNotificationType.SUCCESS, "Products successfully removed"),
    PRINT_SHIPPING_SUMMARY_REPORT_SUCCESS(19,  UseCaseNotificationType.SUCCESS, "Shipping Summary Report generated successfully"),
    PRINT_SHIPPING_SUMMARY_REPORT_ERROR(20,  UseCaseNotificationType.SYSTEM, "Shipping Summary Report generation error. Contact Support."),
    CARTON_REMOVED_SUCCESS(21,  UseCaseNotificationType.SUCCESS, "Carton successfully removed"),
    CARTON_REMOVED_ERROR(22,  UseCaseNotificationType.SYSTEM, "Carton remove error. Contact Support.");

    Integer code;
    UseCaseNotificationType type;
    String message;

}
