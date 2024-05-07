export interface DeferralCodeDto {
  id?: number;
  code: number;
  descriptionKey: string;
  interval: string;
  deferralType: string;
}
