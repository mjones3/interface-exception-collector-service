import { DeviceChangeDto } from './device-change.dto';

export interface DeviceHistoryDto {
  staff?: string;
  date?: Date;
  task?: string;
  changes?: DeviceChangeDto[];
}
