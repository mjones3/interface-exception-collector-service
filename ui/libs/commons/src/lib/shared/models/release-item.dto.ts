import { ReleaseAuthorizationTypeDto } from './relase-authorization-type.dto';
import { ReleaseTypeDto } from './release-type.dto';

export interface ReleaseItemDto {
  id: number;
  descriptionKey: string;
  orderNumber: number;
  active: boolean;
  createDate?: Date;
  types: ReleaseTypeDto[];
  authorizations: ReleaseAuthorizationTypeDto[];
  previouslyUsed?: Boolean;
}
