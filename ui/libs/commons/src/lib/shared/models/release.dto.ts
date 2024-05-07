import { ReleaseAuthorizationTypeDto } from './relase-authorization-type.dto';
import { ReleaseItemDto } from './release-item.dto';

export interface ReleaseDto {
  id?: number;
  releaseTypeKey: string;
  donationId: number;
  deleteDate?: Date;
  createDate?: Date;
  items: ReleaseItemDto[];
  inventories: number[];
  authorizations?: ReleaseAuthorizationTypeDto[];
}
