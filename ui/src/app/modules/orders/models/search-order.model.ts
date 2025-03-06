import { QuerySortDTO } from 'app/shared/models/query-order.model';

export interface OrderCustomerReportDTO {
    code: string;
    name: string;
}

export interface OrderPriorityReportDTO {
    priority: string;
    priorityColor: string; // #FFFFFF
}

export interface OrderReportDTO {
    orderId: number;
    orderNumber: number;
    externalId: string;
    orderStatus: string;
    orderCustomerReport: OrderCustomerReportDTO;
    orderPriorityReport: OrderPriorityReportDTO;
    createDate: string; // yyyy-mm-ddTHH:MM:SS.sss-ZZ:ZZ
    desireShipDate: string; // yyyy-mm-dd"
}

export interface OrderResponsePageDTO {
    content?: OrderReportDTO[];
    pageable?: PageableDTO;
    total?: number;
}

export interface PageableDTO {
    pageNumber: number;
    pageSize: number;
}

export interface OrderQueryCommandDTO {
    locationCode: string;
    orderUniqueIdentifier?: string;
    orderStatus?: string[];
    deliveryTypes?: string[];
    customers?: string[];
    createDateFrom?: string;
    createDateTo?: string;
    desireShipDateFrom?: string;
    desireShipDateTo?: string;
    querySort?: QuerySortDTO;
    pageSize?: number;
    pageNumber?: number;
}
