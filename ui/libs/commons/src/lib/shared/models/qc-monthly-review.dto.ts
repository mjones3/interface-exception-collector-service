export interface QcMonthlyReviewDTO {
  id: number;
  month: number;
  monthText: string;
  year: number;
  locationId: number;
  qcType: string;
  reviewResult: string;
  reviewEmployeeId: string;
  reviewerName: string;
  reviewDate: string;
  comments: string;
  reviewDatTimezone: string;
  createDate: string;
  createDateTimeZone: string;
  modificationDate: string;
  modificationDateTimeZone: string;
}
