import { QuerySortDTO } from './query-order.model';

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

export const EMPTY_PAGE: PageDTO<any> = Object.freeze({
    content: [],
    pageNumber: 0,
    pageSize: 0,
    totalRecords: 0,
    querySort: null,
    hasPrevious: false,
    hasNext: false,
    isFirst: false,
    isLast: false,
    totalPages: 0,
});
