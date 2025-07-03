import { LookUpDto } from '@shared';
import { CustomerCriteriaDto } from './customer-criteria.dto';

export interface OrderCriteriaDTO {
    orderStatus: LookUpDto[];
    orderPriorities: LookUpDto[];
    shipToLocation: CustomerCriteriaDto[];
    customers: CustomerCriteriaDto[];
}
