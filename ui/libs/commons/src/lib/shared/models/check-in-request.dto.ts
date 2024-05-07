/**
 * CheckInResponseDTO model represent the JSON received from Check-In validation action
 **/
export interface CheckInRequestDto {
  unitNumber: string;
  productCode: string;
  facilityId: string;
}
