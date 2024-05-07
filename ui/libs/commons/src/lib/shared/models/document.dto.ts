export interface DocumentDto {
  id?: number;
  referenceId?: number;
  referenceType?: string;
  objectKey?: string;
  filename?: string;
  fileContent?: string;
  contentType?: string;
  size?: number;
  createDate?: string;
  modificationDate?: string;
  properties?: { [key: string]: string };
  employeeId?: string;
}
