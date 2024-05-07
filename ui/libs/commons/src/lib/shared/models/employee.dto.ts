import { EmployeeAttributeDto } from './employee-attribute.dto';
import { RoleDto } from './role.dto';

export interface EmployeeDto {
  id: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  active?: boolean;
  attributes?: EmployeeAttributeDto[];
  roles?: RoleDto[];
}
