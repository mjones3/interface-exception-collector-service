export interface MenuDto {
  id: number;
  title?: string;
  subtitle?: string;
  type: string;
  link?: string;
  externalLink: boolean;
  classes?: string;
  icon?: string;
  iconClasses?: string;
  parentId?: number;
  client: string;
  permissions?: string[];
  enabled: boolean;
}
