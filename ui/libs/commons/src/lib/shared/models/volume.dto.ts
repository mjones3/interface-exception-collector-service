import { ActivatedRoute } from '@angular/router';

export interface VolumeDto {
  id?: number;
  volumeTypeId: number;
  volume: number;
  volumeLossDate: Date;
  donorId: number;
  donationId?: number;
  excludeCalculation: boolean;
  deleteDate?: Date;
  createDate: Date;
  modificationDate: Date;
}
export interface VolumeData {
  weight?: number;
  volume?: number;
  activatedRoute?: ActivatedRoute;
  tareWeight?: number;
  useIntegratedScale?: boolean;
}

export interface VolumeLossDto {
  volumeGroupId: number;
  totalVolumeLoss: number;
}
