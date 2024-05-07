/**
 * Reason model represent the ReasonDTO in Discard API
 **/
export interface DiscardReason {
  reasonId: number;
  descriptionKey: string;
  description: string;
  orderNum: number;
  active: boolean;
  icon: string;
}
