export enum OrderPriority {
    STAT,
    ASAP,
    ROUTINE,
    SCHEDULED,
    DATE_TIME,
}

export const OrderPriorityMap: Record<keyof typeof OrderPriority, string> = {
    STAT: 'STAT',
    ASAP: 'ASAP',
    ROUTINE: 'ROUTINE',
    SCHEDULED: 'SCHEDULED',
    DATE_TIME: 'DATE-TIME',
};
