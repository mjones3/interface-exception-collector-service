import { gql } from '@apollo/client/core';

const GET_IRRADIATION_DEVICE_BY_ID = gql`
    query EnterDeviceId($dto: EnterDeviceIdRequestDTO!) {
        enterDeviceId(dto: $dto) {
            type
            bloodCenterId
            location
            name
            maxProducts
        }
    }
`;

export {
    GET_IRRADIATION_DEVICE_BY_ID
};
