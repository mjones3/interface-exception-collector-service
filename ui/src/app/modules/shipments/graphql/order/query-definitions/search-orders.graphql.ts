import { gql } from 'apollo-angular';
import { OrderQueryCommandDTO, OrderReportDTO } from 'app/modules/orders/models/search-order.model';

export const SEARCH_ORDERS = gql<
    { searchOrders: OrderReportDTO[] },
    { orderQueryCommandDTO: OrderQueryCommandDTO }
>`
    query ($orderQueryCommandDTO: OrderQueryCommandDTO!) {
        searchOrders(orderQueryCommandDTO: $orderQueryCommandDTO) {
            orderId
            orderNumber
            externalId
            orderStatus
            createDate
            desireShipDate
            orderPriorityReport {
                priority
                priorityColor
            }
            orderCustomerReport {
                code
                name
            }
        }
    }
`;
