import { FuseNavigationItem } from '@fuse/components/navigation';

export interface MenuModel {
    id: number;
    title?: string;
    subtitle?: string;
    type: FuseNavigationItem['type'];
    link?: string;
    externalLink: boolean;
    classes?: any;
    icon?: string;
    iconClasses?: any;
    parentId?: number;
    client: string;
    permissions?: string[];
    enabled: boolean;
}
