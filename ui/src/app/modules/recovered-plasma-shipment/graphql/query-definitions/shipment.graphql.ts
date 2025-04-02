import { gql } from 'apollo-angular';
import { PageDTO } from '../../../../shared/models/page.model';
import { QuerySortDTO } from 'app/shared/models/query-order.model';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface RecoveredPlasmaShipmentReportDTO {
    shipmentId: number;
    shipmentNumber: string;
    customerName: string;
    location: string;
    transportationReferenceNumber?: string;
    productType: string;
    shipmentDate?: string;
    status: 'OPEN' | 'CLOSED';
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
}

export const SEARCH_RP_SHIPMENT = gql<
    { searchShipment: UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> },
    { recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO }
>`
    query ($recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO!) {
        searchShipment(recoveredPlasmaShipmentQueryCommandRequestDTO: $recoveredPlasmaShipmentQueryCommandRequestDTO) {
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
