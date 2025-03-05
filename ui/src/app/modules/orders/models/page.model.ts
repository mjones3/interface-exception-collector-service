import { QuerySortDTO } from './search-order.model';

export interface PageDTO<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalRecords: number;
    querySort: QuerySortDTO;
    hasPrevious: boolean;
    hasNext: boolean;
    isFirst: boolean;
    isLast: boolean;
    totalPages: number;
}
