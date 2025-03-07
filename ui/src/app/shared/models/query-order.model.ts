export interface QueryOrderByDTO {
    property: string;
    direction: string;
}

export interface QuerySortDTO {
    orderByList: QueryOrderByDTO[];
}
