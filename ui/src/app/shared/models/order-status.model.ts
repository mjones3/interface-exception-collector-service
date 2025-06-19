export enum OrderStatus {
    ALL,
    OPEN,
    CREATED,
    SHIPPED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
}

export const OrderStatusMap: Record<keyof typeof OrderStatus, string> = {
    ALL: 'All',
    OPEN: 'Open',
    CREATED: 'Created',
    SHIPPED: 'Shipped',
    IN_PROGRESS: 'In Progress',
    COMPLETED: 'Completed',
    CANCELLED: 'Canceled',
};
