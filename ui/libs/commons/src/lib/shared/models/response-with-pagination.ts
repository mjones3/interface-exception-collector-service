import {PaginationLinks} from './pagination-links.model';

export interface IResponseWithPagination {
  body: any;
  links: PaginationLinks;
  total: number;
}
