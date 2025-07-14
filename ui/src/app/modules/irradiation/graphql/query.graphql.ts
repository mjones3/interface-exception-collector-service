import { gql } from '@apollo/client/core';

const GET_IRRADIATION_DEVICE_BY_ID = gql`
    query  validateDevice($deviceId: String!, $location: String!) {
        validateDevice(deviceId: $deviceId, location: $location)
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

const VALIDATE_UNIT = gql`
    query  validateUnit($unitNumber: String!, $location: String!) {
        validateUnit(unitNumber: $unitNumber, location: $location) {
            unitNumber
            productCode
            location
            status
            productDescription
            productFamily
            statusReason
            unsuitableReason
            expired
            quarantines {
                reason
                comments
                stopsManufacturing
            }
        }
    }
`;

export {
    GET_IRRADIATION_DEVICE_BY_ID,
    GET_CONFIGURATIONS,
    VALIDATE_UNIT
};
