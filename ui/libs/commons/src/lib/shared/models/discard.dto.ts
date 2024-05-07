/**
 * Discard model represent the DiscardDTO in Discard API
 **/
export interface Discard {
  id?: number;
  inventoryId: number;
  reasonId: number;
  reasonKey: string;
  employeeId: string;
  deleteDate: Date;
  comments: string;
  discardType?: string;
  createDate: Date;
  flaggedForResearch?: boolean;
  processFlagForResearch?: boolean;
}
