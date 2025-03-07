export interface QueryOrderByDTO {
    property: string;
    direction: 'ASC' | 'DESC';
}

export interface QuerySortDTO {
    orderByList: QueryOrderByDTO[];
}
