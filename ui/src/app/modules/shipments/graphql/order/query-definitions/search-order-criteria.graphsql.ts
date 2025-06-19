import { gql } from 'apollo-angular';
import { OrderCriteriaDTO } from '../../../../orders/models/order-criteria.model';

export const SEARCH_ORDER_CRITERIA = gql<
    { searchOrderCriteria: OrderCriteriaDTO },
    never
>`
    query {
        searchOrderCriteria {
            orderStatus {
                optionValue
                descriptionKey
            }
            orderPriorities {
                optionValue
                descriptionKey
            }
            customers {
                code
                name
            }
        }
    }
`;
