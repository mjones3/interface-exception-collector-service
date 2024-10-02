import { gql } from 'apollo-angular';

export interface UnitNumberWithCheckDigitDTO {
    unitNumber: string;
    checkDigit: string;
    verifiedCheckDigit: string;
    valid: boolean;
    message: string;
}

export const VERIFY_CHECK_DIGIT = gql<
    { verifyCheckDigit: UnitNumberWithCheckDigitDTO },
    { unitNumber: string, checkDigit: string }
>`
    query VerifyCheckDigit($unitNumber: String!, $checkDigit: String!) {
        verifyCheckDigit(unitNumber: $unitNumber, checkDigit: $checkDigit) {
            unitNumber
            checkDigit
            verifiedCheckDigit
            valid
            message
        }
    }
`;
