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
            alreadyIrradiated
            notConfigurableForIrradiation
            quarantines {
                reason
                comments
                stopsManufacturing
            }
        }
    }
`;

const CHECK_DIGIT = gql`
    query  checkDigit($unitNumber: String!, $checkDigit: String!) {
        checkDigit(unitNumber: $unitNumber, checkDigit: $checkDigit) {
            isValid
        }
    }
`;

const VALIDATE_LOT_NUMBER = gql`
    query validateLotNumber($lotNumber: String!, $type: String!) {
        validateLotNumber(lotNumber: $lotNumber, type: $type)
    }
`;


export {
    GET_IRRADIATION_DEVICE_BY_ID,
    GET_CONFIGURATIONS,
    VALIDATE_UNIT,
    CHECK_DIGIT,
    VALIDATE_LOT_NUMBER
};
