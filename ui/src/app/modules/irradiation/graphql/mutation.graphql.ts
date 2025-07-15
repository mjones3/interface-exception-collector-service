import { gql } from '@apollo/client/core';

const START_IRRADIATION_SUBMIT_BATCH = gql`
    mutation  submitBatch($input: SubmitBatchInput!) {
        submitBatch(input: $input) {
            batchId
            message
        }
    }
`;

export {
    START_IRRADIATION_SUBMIT_BATCH,
};
