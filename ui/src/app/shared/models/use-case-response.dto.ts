export interface UseCaseNotificationDTO {
    type: string;
    message: string;
    code?: number;
    action?: string;
    reason?: string;
    details?: string[];
    name?: string;
}

export interface UseCaseResponseDTO<T> {
    notifications?: UseCaseNotificationDTO[];
    data?: T;
    _links?: Record<string, string>;
}
