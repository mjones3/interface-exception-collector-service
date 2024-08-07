import { OrderSummaryDto } from "./order.dto";

export enum OrderStatuses {
    OPEN = 'ORDER_STATUS_COLOR_OPEN',
    SHIPPED = 'ORDER_STATUS_COLOR_SHIPPED',
    CANCELLED = 'ORDER_STATUS_COLOR_CANCELLED',
  }

export const OPEN_OPTION_VALUE = 'OPEN';


export interface OrderSummary extends OrderSummaryDto {
    statusDescriptionKey?: string;
    statusColor?: string;
    deliveryTypeDescriptionKey?: string;
  }