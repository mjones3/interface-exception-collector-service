export interface DonorConsentSignaturesDto {
  id: number;
  consentId: number;
  signatureTypeId: number;
  signatureTypeKey: string;
  signature: string;
  createDate?: Date;
}

export interface DonorConsentDto {
  id: number;
  commonRuleInformationandConsent?: string;
  commonRuleAuthorization?: string;
  researchAuthorization?: string;
  researchConsent?: string;
  consentTypeId: number;
  donationId: number;
  employeeId: number;
  deleteDate?: Date;
  createDate: Date;
  consentLanguage?: string;
  consentSignatures?: DonorConsentSignaturesDto[];
}
