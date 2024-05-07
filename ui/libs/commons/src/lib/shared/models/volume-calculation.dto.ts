export interface VolumeCalculationRequestDto {
  descriptionKey: string;
  donationId?: number;
  inventoryId?: number;
  bagTypeKey?: string;
  pasSolution?: boolean;
  weight: number;
  bagCode?: string;
}

export interface VolumeCalculationResponseDto {
  tareWeight?: number;
  volume?: string;
  weight?: string;
}
