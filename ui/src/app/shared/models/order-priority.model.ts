export enum OrderPriority {
    STAT,
    ASAP,
    ROUTINE,
    SCHEDULED,
}

export const OrderPriorityMap: Record<keyof typeof OrderPriority, string> = {
    STAT: 'STAT',
    ASAP: 'ASAP',
    ROUTINE: 'Routine',
    SCHEDULED: 'Scheduled',
};
