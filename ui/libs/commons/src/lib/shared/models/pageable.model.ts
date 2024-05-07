import { Direction } from './direction.enum';

export interface Pageable {
  /**
   * Current page
   */
  page?: number;

  /**
   * The number of items per paginated page.
   */
  size?: number;

  /**
   * Fields to sort
   */
  sort?: string[];

  /**
   * Sorting direction 'ASC' | 'DESC'
   */
  direction?: Direction;

  /**
   * The total number of items in the collection. Only useful when
   * doing server-side paging, where the collection size is limited
   * to a single page returned by the server API.
   *
   * For in-memory paging, this property should not be set, as it
   * will be automatically set to the value of  collection.length.
   */
  totalItems?: number;
}

export const defaultPageSize = 10;

export const pageableDefault: Pageable = {
  page: 0,
  size: defaultPageSize,
  sort: [],
  direction: Direction.ASC,
};

export interface Page {
  pageIndex: number;
  pageSize: number;
}
