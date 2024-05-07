import { NotificationDto } from './notification.dto';

/**
 * CheckInResponseDTO model represent the JSON received from Check-In validation action
 **/
export interface CheckInResponseDto {
  ruleCode: string;
  results: Result;
  _links: Link;
  notifications: NotificationDto[];
}

export interface Link {
  next: string;
}

export interface Result {
  result: string[];
}
