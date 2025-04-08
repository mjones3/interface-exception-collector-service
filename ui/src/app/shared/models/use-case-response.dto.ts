export interface UseCaseNotificationDTO {
    type: string;
    message: string;
    code: number;
}

export interface UseCaseResponseDTO<T> {
    notifications?: UseCaseNotificationDTO[];
    data?: T;
    _links?: Record<string, string>;
}
