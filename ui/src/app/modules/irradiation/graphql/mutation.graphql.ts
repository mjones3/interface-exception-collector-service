import { gql } from '@apollo/client/core';

const START_IRRADIATION_SUBMIT_BATCH = gql`
    mutation  submitBatch($input: SubmitBatchInput!) {
        submitBatch(input: $input) {
            batchId
            message
        }
    }
`;

const COMPLETE_BATCH = gql`
    mutation completeBatch($input: CompleteBatchInput!) {
        completeBatch(input: $input) {
            success
            message
        }
    }
`;

export {
    START_IRRADIATION_SUBMIT_BATCH,
    COMPLETE_BATCH,
};
