import { gql } from 'apollo-angular';
import { QuerySortDTO } from 'app/shared/models/query-order.model';
import { PageDTO } from '../../../../shared/models/page.model';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

// Shipment
export enum RecoveredPlasmaShipmentStatus {
    OPEN,
    IN_PROGRESS,
    CLOSED,
    PROCESSING
}
export const RecoveredPlasmaShipmentStatusMap: Record<keyof typeof RecoveredPlasmaShipmentStatus, string> = {
    OPEN: 'OPEN',
    IN_PROGRESS: 'IN PROGRESS',
    CLOSED: 'CLOSED',
    PROCESSING: 'PROCESSING',
};

// Carton
export enum RecoveredPlasmaCartonStatus {
    OPEN,
    CLOSED,
    REPACK,
}
export const RecoveredPlasmaCartonStatusMap: Record<keyof typeof RecoveredPlasmaCartonStatus, string> = {
    OPEN: 'OPEN',
    CLOSED: 'CLOSED',
    REPACK: 'REPACK',
};
export const RecoveredPlasmaCartonStatusCssMap: Record<keyof typeof RecoveredPlasmaCartonStatus, string> = {
    OPEN: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-100 text-blue-700',
    CLOSED: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-green-100 text-green-700',
    REPACK: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-[#FFEDD5] text-[#C2410C]',
};

export interface RecoveredPlasmaShipmentReportDTO {
    shipmentId: number;
    shipmentNumber: string;
    customerName: string;
    location: string;
    transportationReferenceNumber?: string;
    productType: string;
    shipmentDate?: string;
    status: keyof typeof RecoveredPlasmaShipmentStatus;
}

export interface RecoveredPlasmaShipmentQueryCommandRequestDTO {
    locationCode?: string[];
    shipmentNumber?: string;
    shipmentStatus?: string[];
    customers?: string[];
    productTypes?: string[];
    shipmentDateFrom?: string;
    shipmentDateTo?: string;
    querySort?: QuerySortDTO;
    pageNumber?: number;
    pageSize?: number;
    transportationReferenceNumber?: string;
}

export const SEARCH_RP_SHIPMENT = gql<
    {
        searchShipment: UseCaseResponseDTO<
            PageDTO<RecoveredPlasmaShipmentReportDTO>
        >;
    },
    {
        recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO;
    }
>`
    query (
        $recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO!
    ) {
        searchShipment(
            recoveredPlasmaShipmentQueryCommandRequestDTO: $recoveredPlasmaShipmentQueryCommandRequestDTO
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
`;
