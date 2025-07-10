import { gql } from '@apollo/client/core';

const GET_IRRADIATION_DEVICE_BY_ID = gql`
    query  validateDevice($deviceId: String!, $location: String!) {
        validateDevice(deviceId: $deviceId, location: $location) {
            errorMessage
            valid
        }
    }
`;

const GET_CONFIGURATIONS = gql`
    query  readConfiguration($keys: [String]) {
        readConfiguration(keys: $keys) {
            key
            value
        }
    }
`;

export {
    GET_IRRADIATION_DEVICE_BY_ID,
    GET_CONFIGURATIONS
};
