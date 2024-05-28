import { MenuDto } from '@rsa/commons';

export const MENUS: MenuDto[] = [
  {
    id: 1,
    title: 'Shipments',
    subtitle: '',
    type: 'COLLAPSABLE',
    link: '',
    externalLink: false,
    classes: '',
    icon: 'shopping_cart',
    iconClasses: 'primary',
    parentId: null,
    client: '215b898c-0f1e-43e8-abd1-f835bef767e3',
    enabled: true,
  },
  {
    id: 2,
    title: 'Requests',
    subtitle: '',
    type: 'BASIC',
    link: '/orders/search',
    externalLink: false,
    classes: '',
    icon: 'search',
    iconClasses: 'primary',
    parentId: 1,
    client: '215b898c-0f1e-43e8-abd1-f835bef767e3',
    enabled: true,
  },
];
