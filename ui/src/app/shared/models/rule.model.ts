import { NotificationDto } from '@shared';

export interface RuleResponseDTO<TResults = any> {
    ruleCode?: string
    notifications?: NotificationDto[]
    _links?: Record<string, string>
    results?: TResults
}
