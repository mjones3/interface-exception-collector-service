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
    pageSize: number
}

// Generic
export interface QueryOrderByDTO {
    property: string;
    direction: string;
}

export interface QuerySortDTO {
    orderByList: QueryOrderByDTO[];
}

export interface OrderQueryCommandDTO {
    locationCode: string;
    orderNumber?: string;
    querySort?: QuerySortDTO;
    limit?: number;
}
