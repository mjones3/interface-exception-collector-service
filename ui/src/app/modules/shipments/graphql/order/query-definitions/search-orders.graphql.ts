import { gql } from 'apollo-angular';
import { OrderReportDTO, QueryOrderByDTO } from '../../../../orders/order/models/search-order.model';

export const SEARCH_ORDERS = gql<OrderReportDTO[], QueryOrderByDTO>`
    query($orderQueryCommandDTO: OrderQueryCommandDTO!) {
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
