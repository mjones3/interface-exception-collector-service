export interface ReleaseAuthorizationDto {
  id: number;
  releaseId: number;
  authorizationTypeKey: string;
  deleteDate?: Date;
  createDate?: Date;
  modifcationDate?: Date;
}
