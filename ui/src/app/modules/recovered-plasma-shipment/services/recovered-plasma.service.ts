import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { LookUpDto, NotificationTypeMap, ToastrImplService } from '@shared';
import { MutationResult } from 'apollo-angular';
import { catchError, Observable, Observer } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { DiscardRequestDTO } from '../../../shared/models/discard.model';
import { PageDTO } from '../../../shared/models/page.model';
import { UseCaseNotificationDTO, UseCaseResponseDTO } from '../../../shared/models/use-case-response.dto';
import { ConfirmationAcknowledgmentService } from '../../../shared/services/confirmation-acknowledgment.service';
import { DiscardService } from '../../../shared/services/discard.service';
import { CREATE_CARTON, CreateCartonRequestDTO } from '../graphql/mutation-definitions/create-carton.graphql';
import { CARTON_PACK_ITEM, PackCartonItemsDTO } from '../graphql/mutation-definitions/pack-items.graphql';
import { FIND_CARTON_BY_ID } from '../graphql/query-definitions/carton.graphql';
import { FIND_ALL_CUSTOMERS, RecoveredPlasmaCustomerDTO } from '../graphql/query-definitions/customer.graphql';
import { FIND_ALL_LOCATIONS, RecoveredPlasmaLocationDTO } from '../graphql/query-definitions/location.graphql';
import { FIND_ALL_LOOKUPS_BY_TYPE } from '../graphql/query-definitions/lookup.graphql';
import {
    RecoveredPlasmaShipmentQueryCommandRequestDTO,
    RecoveredPlasmaShipmentReportDTO,
    SEARCH_RP_SHIPMENT
} from '../graphql/query-definitions/shipment.graphql';
import {
    FindShipmentRequestDTO,
    RECOVERED_PLASMA_SHIPMENT_DETAILS
} from '../graphql/query-definitions/shipmentDetails.graphql';
import {
    CartonDTO,
    CartonPackedItemResponseDTO,
    RecoveredPlasmaShipmentResponseDTO
} from '../models/recovered-plasma.dto';
import { VERIFY_CARTON_PACK_ITEM, VerifyCartonItemsDTO } from '../graphql/mutation-definitions/verify-products.graphql';
import { CLOSE_CARTON, CloseCartonDTO } from '../graphql/mutation-definitions/close-carton.graphql';
import {
    CartonPackingSlipDTO,
    GENERATE_CARTON_PACKING_SLIP,
    GenerateCartonPackingSlipRequestDTO
} from '../graphql/query-definitions/generate-carton-packing-slip.graphql';
import { CLOSE_SHIPMENT, CloseShipmentRequestDTO } from '../graphql/mutation-definitions/close-shipment.graphql';
import {
    PRINT_UNACCEPTABLE_UNITS_REPORT,
    PrintUnacceptableUnitReportRequestDTO,
    UnacceptableUnitReportOutput
} from '../graphql/query-definitions/print-unacceptable-units-report.graphql';
import { REPACK_CARTON, RepackCartonDTO } from '../graphql/mutation-definitions/repack-carton.graphql';

@Injectable({
    providedIn: 'root',
})
export class RecoveredPlasmaService {
    readonly servicePath = '/recoveredplasmashipping/graphql';

    constructor(
        private dynamicGraphqlPathService: DynamicGraphqlPathService,
        private discardService: DiscardService,
        private confirmationAcknowledgmentService: ConfirmationAcknowledgmentService,
        private toastr: ToastrImplService
    ) {}

    public searchRecoveredPlasmaShipments(
        recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO
    ): Observable<
        ApolloQueryResult<{
            searchShipment: UseCaseResponseDTO<
                PageDTO<RecoveredPlasmaShipmentReportDTO>
            >;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_RP_SHIPMENT,
            { recoveredPlasmaShipmentQueryCommandRequestDTO }
        );
    }

    public findAllLookupsByType(type: string): Observable<
        ApolloQueryResult<{
            findAllLookupsByType: LookUpDto[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOOKUPS_BY_TYPE,
            { type }
        );
    }

    public findAllLocations(): Observable<
        ApolloQueryResult<{
            findAllLocations: RecoveredPlasmaLocationDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOCATIONS
        );
    }

    public findAllCustomers(): Observable<
        ApolloQueryResult<{
            findAllCustomers: RecoveredPlasmaCustomerDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_CUSTOMERS
        );
    }

    public createCarton(
        request: CreateCartonRequestDTO
    ): Observable<
        MutationResult<{ createCarton: UseCaseResponseDTO<CartonDTO> }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CREATE_CARTON,
            request
        );
    }

    public repackCarton(
        request: RepackCartonDTO
    ): Observable<
        MutationResult<{ repackCarton: UseCaseResponseDTO<CartonDTO> }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            REPACK_CARTON,
            request
        );
    }

    public checkRecoveredPlasmaFacility(facilityCode: string) {
        let matchFacility;
        return new Observable((observer: Observer<boolean>) => {
            this.findAllLocations().subscribe((res) => {
                matchFacility = res.data.findAllLocations.find(
                    (location) => location.code === facilityCode
                );
                observer.next(!!matchFacility);
                observer.complete();
            });
        });
    }

    public getCartonById(
        cartonId: number
    ): Observable<
        ApolloQueryResult<{ findCartonById: UseCaseResponseDTO<CartonDTO> }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_CARTON_BY_ID,
            { cartonId }
        );
    }

    public getShipmentById(
        findShipmentCommandDTO: FindShipmentRequestDTO
    ): Observable<
        ApolloQueryResult<{
            findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            RECOVERED_PLASMA_SHIPMENT_DETAILS,
            { findShipmentCommandDTO }
        );
    }

    public addCartonProducts(cartonProducts: PackCartonItemsDTO): Observable<
        MutationResult<{
            packCartonItem: UseCaseResponseDTO<CartonDTO>;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CARTON_PACK_ITEM,
            cartonProducts
        );
    }

    public closeCarton(closeCarton: CloseCartonDTO)
        : Observable<MutationResult<{ closeCarton: UseCaseResponseDTO<CartonDTO> }>> {

        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CLOSE_CARTON,
            closeCarton
        );
    }

    public verifyCartonProducts(cartonProducts: VerifyCartonItemsDTO)
        : Observable<MutationResult<{ verifyCarton: UseCaseResponseDTO<CartonDTO> }>> {

        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            VERIFY_CARTON_PACK_ITEM,
            cartonProducts
        );
    }

    public generateCartonPackingSlip(generateCartonPackingSlipRequestDTO: GenerateCartonPackingSlipRequestDTO)
        : Observable<ApolloQueryResult<{ generateCartonPackingSlip: UseCaseResponseDTO<CartonPackingSlipDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GENERATE_CARTON_PACKING_SLIP,
            generateCartonPackingSlipRequestDTO
        );
    }

    public printUnacceptableUnitsReport(printUnacceptableUnitReportRequest: PrintUnacceptableUnitReportRequestDTO)
        : Observable<ApolloQueryResult<{ printUnacceptableUnitsReport: UseCaseResponseDTO<UnacceptableUnitReportOutput> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            PRINT_UNACCEPTABLE_UNITS_REPORT,
            printUnacceptableUnitReportRequest
        );
    }

    public closeShipment(closeShipment: CloseShipmentRequestDTO)
    : Observable<MutationResult<{ closeShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> }>> {
    return this.dynamicGraphqlPathService.executeMutation(
        this.servicePath,
        CLOSE_SHIPMENT,
        closeShipment
    );
}

    //Handles INFO notifications and triggers discard if needed
    public handleInfoNotificationAndDiscard(
        notification: UseCaseNotificationDTO,
        inventoryItem: CartonPackedItemResponseDTO,
        locationCode: string,
        employeeId: string,
        callBacks: {resetFn: ()=> {}, focusFn: ()=> {}}
    ): void {
        if (notification.action === 'TRIGGER_DISCARD') {
            this.triggerDiscard(notification, inventoryItem, locationCode, employeeId, callBacks);
        } else {
            this.openAcknowledgmentMessageDialog(notification, callBacks);
        }
    }


    //Displays notification messages
    public displayNotificationMessage(
        notifications: UseCaseNotificationDTO[],
        focusCallback?: () => {}
    ): void {
        notifications.forEach((notification) => {
            const notificationType = NotificationTypeMap[notification.type];
            const toastrRef = this.toastr.show(
                notification.message,
                notificationType.title,
                {
                    ...(notificationType.timeOut
                        ? { timeOut: notificationType.timeOut }
                        : {}),
                    ...(notification.type === 'SYSTEM' ? { timeOut: 0 } : {}),
                },
                notificationType.type
            );
            if (focusCallback) {
                toastrRef?.onTap.subscribe(() => focusCallback());
            }
        });
    }


    //Triggers discard for an inventory item
    private triggerDiscard(
        triggerDiscardNotification: UseCaseNotificationDTO,
        inventoryItem: CartonPackedItemResponseDTO,
        locationCode: string,
        employeeId: string,
        callBacks: {resetFn: ()=> {}, focusFn: ()=> {}}
    ): void {
        this.discardService
            .discardProduct(
                this.getDiscardRequestDto(
                    inventoryItem,
                    triggerDiscardNotification.reason,
                    locationCode,
                    employeeId
                )
            )
            .pipe(
                catchError((err) => {
                    this.showDiscardSystemError(callBacks.focusFn);
                    throw err;
                })
            )
            .subscribe((response) => {
                const data = response?.data?.discardProduct;
                if (data) {
                    return this.openAcknowledgmentMessageDialog(
                        triggerDiscardNotification,
                        callBacks
                    );
                } else {
                    this.showDiscardSystemError(() => {
                        callBacks.focusFn();
                        callBacks.resetFn();
                    });
                }
            });
    }


    // Shows discard system error message
    private showDiscardSystemError(focusCallback): void {
        this.displayNotificationMessage(
            [
                {
                    type: 'SYSTEM',
                    message:
                        'Product has not been discarded in the system. Contact Support.',
                },
            ],
            focusCallback
        );
    }


    //Opens acknowledgment message dialog
    private openAcknowledgmentMessageDialog(
        notification: UseCaseNotificationDTO,
        callBacks: {resetFn: ()=> {}, focusFn: ()=> {}}
    ): void {
        const message = notification.message;
        const details = notification.details;
        this.confirmationAcknowledgmentService.notificationConfirmation(
            message,
            details,
            ()=> {
                callBacks.resetFn();
                callBacks.focusFn();
            }
        );
    }


    //Creates a discard request DTO
    private getDiscardRequestDto(
        inventoryItem: CartonPackedItemResponseDTO,
        reason: string,
        locationCode: string,
        employeeId: string,
        comments?: string
    ): DiscardRequestDTO {
        return {
            unitNumber: inventoryItem?.unitNumber,
            productCode: inventoryItem?.productCode,
            locationCode: locationCode,
            employeeId: employeeId,
            triggeredBy: 'SHIPPING',
            reasonDescriptionKey: reason,
            productFamily: inventoryItem?.productType,
            productShortDescription: inventoryItem?.productDescription,
            comments: comments,
        };
    }
}

