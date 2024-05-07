import { PermissionDto } from './permission.dto';

export interface RoleDto {
  id: string;
  name: string;
  selected: boolean;
  active: boolean;
  permissions: PermissionDto[];
}
