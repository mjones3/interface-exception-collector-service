export interface DhqDto {
  id?: number;
  parentId?: number;
  donationId?: number;
  questionText?: string;
  answer?: string;
  questionNumber?: number;
  dhqDeferredDonorId?: number;
  statusKey?: string;
  statusModificationDate?: string;
  decisionKey?: string;
  employeeId?: string;
  comments?: string;
  questionCode?: string;
  deferralCode?: string;
  children?: DhqDto;
  createDate?: string;
  deleteDate?: string;
}
