export interface ProcessProductVersionDto {
  id: string;
  productId: string;
  releaseVersion: string;
  buildVersion: string;
  releaseNotes: string;
  createDate?: Date;
}
