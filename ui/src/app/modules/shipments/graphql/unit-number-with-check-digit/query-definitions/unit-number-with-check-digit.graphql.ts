import { gql } from 'apollo-angular';
import { RuleResponseDTO } from '../../../../../shared/models/rule.model';

export interface UnitNumberWithCheckDigitDTO {
    unitNumber: string;
    verifiedCheckDigit: string;
}

export const VERIFY_CHECK_DIGIT = gql<
    { verifyCheckDigit: RuleResponseDTO<{ data: UnitNumberWithCheckDigitDTO[] }> },
    { unitNumber: string, checkDigit: string }
>`
    query VerifyCheckDigit($unitNumber: String!, $checkDigit: String!) {
        verifyCheckDigit(unitNumber: $unitNumber, checkDigit: $checkDigit) {
            ruleCode
            _links
            results
            notifications {
                name
                statusCode
                notificationType
                code
                action
                reason
                message
            }
        }
    }
`;
