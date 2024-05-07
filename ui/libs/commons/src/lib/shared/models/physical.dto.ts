import { PhysicalBPDto } from './physical-bp.dto';
import { PhysicalHGBDto } from './physical-hgb.dto';
import { PhysicalPulseDto } from './physical-pulse.dto';

export interface PhysicalDto {
  id: number;
  donationId?: number;
  height?: number;
  weight?: number;
  temperature?: number;
  physicalCondition?: string;
  leftArmCondition?: string;
  rightArmCondition?: string;
  employeeId?: string;
  physicalsHGB: PhysicalHGBDto[];
  physicalsBP: PhysicalBPDto[];
  physicalsPulse: PhysicalPulseDto[];
}
