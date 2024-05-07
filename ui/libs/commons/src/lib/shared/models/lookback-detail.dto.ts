import { LookbackTestResultDetailDto } from './lookback-test-result-detail.dto';

export interface LookbackDetailDto {
  donorId: number;
  donationId: number;
  unitNumber: string;
  testResultDetails: LookbackTestResultDetailDto[];
}

export interface LookbackReportDto {
  id?: number;
  unitNumber: string;
  donorIntention: string;
  donorId: string;
  birthDate: string | number | Date;
  drawDate: string | number | Date;
  testGroup: string;
  testType: string;
  testTypeId: number;
  result: string;
  dateDiscovery: string | number | Date;
  collectionLocation: string;
  reviewStatus: string;
  lastReviewDecision: string;
  lastReviewDecisionDate: string | number | Date;
}
