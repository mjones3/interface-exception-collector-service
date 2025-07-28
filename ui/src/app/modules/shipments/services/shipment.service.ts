import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { COMPLETE_SHIPMENT, PACK_ITEM } from '../graphql/shipment/mutation-definitions/shipment.graphql';
import { GET_SHIPMENT_BY_ID, LIST_SHIPMENTS } from '../graphql/shipment/query-definitions/shipment.graphql';
import {
    VERIFY_CHECK_DIGIT
} from '../graphql/unit-number-with-check-digit/query-definitions/unit-number-with-check-digit.graphql';
import { UNPACK_ITEM, UnpackItemRequest } from '../graphql/unpack-product.graphql';
import {
    CANCEL_SECOND_VERIFICATION,
    CancelSecondVerificationRequest,
    CONFIRM_CANCEL_SECOND_VERIFICATION,
    ConfirmCancelSecondVerificationRequest,
    GET_NOTIFICATION_DETAILS_BY_SHIPMENT_ID,
    GET_SHIPMENT_VERIFICATION_DETAILS_BY_ID,
    REMOVE_ITEM,
    RemoveItemRequest,
    RemoveProductResponseDTO,
    ShipmentDTO,
    VERIFY_ITEM,
    VerifyItemRequest,
    VerifyProductResponseDTO
} from '../graphql/verify-products/query-definitions/verify-products.graphql';
import { ShipmentDetailResponseDTO, ShipmentResponseDTO, VerifyProductDTO } from '../models/shipment-info.dto';
import {
    GET_UNLABELED_PRODUCTS,
    GetUnlabeledProductsRequestDTO,
    ProductResponseDTO
} from '../graphql/query-defintions/get-unlabeled-products.graphql';

@Injectable({
    providedIn: 'root',
})
export class ShipmentService {
    readonly servicePath = '/shipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public listShipments(): Observable<
        ApolloQueryResult<{ listShipments: ShipmentResponseDTO[] }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            LIST_SHIPMENTS
        );
    }

    public completeShipment(inputs: {
        shipmentId: number;
        employeeId: string;
    }): Observable<MutationResult<{ completeShipment: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            COMPLETE_SHIPMENT,
            inputs
        );
    }

    public verifyShipmentProduct(
        verifyProductDTO: VerifyProductDTO
    ): Observable<MutationResult<{ packItem: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            PACK_ITEM,
            verifyProductDTO
        );
    }

    validateCheckDigit(
        unitNumber: string,
        checkDigit: string
    ): Observable<MutationResult<{ verifyCheckDigit: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            VERIFY_CHECK_DIGIT,
            { unitNumber, checkDigit }
        );
    }

    public getShipmentById(
        shipmentId: number
    ): Observable<
        ApolloQueryResult<{ getShipmentDetailsById: ShipmentDetailResponseDTO }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_SHIPMENT_BY_ID,
            { shipmentId }
        );
    }

    public getShipmentVerificationDetailsById(shipmentId: number): Observable<
        ApolloQueryResult<{
            getShipmentVerificationDetailsById: VerifyProductResponseDTO;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_SHIPMENT_VERIFICATION_DETAILS_BY_ID,
            { shipmentId }
        );
    }

    public getNotificationDetailsByShipmentId(shipmentId: number): Observable<
        ApolloQueryResult<{
            getNotificationDetailsByShipmentId: RemoveProductResponseDTO;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_NOTIFICATION_DETAILS_BY_SHIPMENT_ID,
            { shipmentId }
        );
    }

    public verifyItem(verifyItemRequest: VerifyItemRequest): Observable<
        MutationResult<{
            verifyItem: RuleResponseDTO<{
                results: VerifyProductResponseDTO[];
            }>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            VERIFY_ITEM,
            verifyItemRequest
        );
    }

    public removeItem(removeItemRequest: RemoveItemRequest): Observable<
        MutationResult<{
            removeItem: RuleResponseDTO<{
                results: RemoveProductResponseDTO[];
            }>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            REMOVE_ITEM,
            removeItemRequest
        );
    }

    public unpackedItem(
        unpackItemsRequest: UnpackItemRequest
    ): Observable<MutationResult<{ unpackItems: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            UNPACK_ITEM,
            unpackItemsRequest
        );
    }

    public cancelSecondVerification(
        cancelSecondVerificationRequest: CancelSecondVerificationRequest
    ): Observable<
        MutationResult<{
            cancelSecondVerification: RuleResponseDTO<{
                results: ShipmentDTO[];
            }>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CANCEL_SECOND_VERIFICATION,
            cancelSecondVerificationRequest
        );
    }

    public confirmCancelSecondVerification(
        confirmCancelSecondVerificationRequest: ConfirmCancelSecondVerificationRequest
    ): Observable<
        MutationResult<{
            confirmCancelSecondVerification: RuleResponseDTO<{
                results: ShipmentDTO[];
            }>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CONFIRM_CANCEL_SECOND_VERIFICATION,
            confirmCancelSecondVerificationRequest
        );
    }

    public getUnlabeledProducts(getUnlabeledProductsRequest: GetUnlabeledProductsRequestDTO): Observable<ApolloQueryResult<{ getUnlabeledProducts: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService
            .executeQuery(
                this.servicePath,
                GET_UNLABELED_PRODUCTS,
                { getUnlabeledProductsRequest }
            );
    }

}
