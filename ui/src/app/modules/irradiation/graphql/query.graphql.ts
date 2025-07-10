import { gql } from '@apollo/client/core';

const GET_IRRADIATION_DEVICE_BY_ID = gql`
    query  validateDevice($deviceId: String!, $location: String!) {
        validateDevice(deviceId: $deviceId, location: $location) {
            errorMessage
            valid
        }
    }
`;

export {
    GET_IRRADIATION_DEVICE_BY_ID
};
