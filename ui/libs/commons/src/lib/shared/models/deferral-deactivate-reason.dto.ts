export interface DeferralDeactivateReasonDto {
  id: number;
  descriptionKey: string;
  reentryReason: boolean;
  orderNumber: number;
  active: boolean;
}
