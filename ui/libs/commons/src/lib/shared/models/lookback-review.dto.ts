export interface LookbackReviewDto {
  id: number;
  lookbackId: number;
  reviewerEmployeeId: string;
  statusKey: string;
  reviewDecisionKey: string;
  comments: string;
  caseNumber?: string;
  createDate: string;
  modificationDate: string;
  clientTimezone: string;
}
