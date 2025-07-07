import { gql } from '@apollo/client/core';

const SUBMIT_IRRADIATION_BATCH = gql`
    mutation SubmitIrradiationBatch(
        $dto: SubmitIrradiationBatchRequestDTO!
    ) {
        submitIrradiationBatch(dto: $dto) {
            batchId
        }
    }
`;

export {
    SUBMIT_IRRADIATION_BATCH,
};
