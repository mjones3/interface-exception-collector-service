import { gql } from '@apollo/client/core';

const GET_IRRADIATION_DEVICE_BY_ID = gql`
    query ValidateDevice($deviceId: String!, $location: String!){
        validateDevice(deviceId: $deviceId, location: $location)
    }
`;

export {
    GET_IRRADIATION_DEVICE_BY_ID
};
