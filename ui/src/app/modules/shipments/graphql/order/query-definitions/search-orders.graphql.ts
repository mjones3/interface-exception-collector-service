import { gql } from 'apollo-angular';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from 'app/modules/orders/models/search-order.model';
import { PageDTO } from '../../../../orders/models/page.model';

export const SEARCH_ORDERS = gql<
    { searchOrders: PageDTO<OrderReportDTO> },
    { orderQueryCommandDTO: OrderQueryCommandDTO }
>`
    query ($orderQueryCommandDTO: OrderQueryCommandDTO!) {
        searchOrders(orderQueryCommandDTO: $orderQueryCommandDTO) {
            content
            pageNumber
            pageSize
            totalRecords
            totalPages
            querySort {
                orderByList {
                    property
                    direction
                }
            }
        }
    }
`;
