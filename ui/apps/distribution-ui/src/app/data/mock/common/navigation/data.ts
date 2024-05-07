/* tslint:disable:max-line-length */
import { TreoNavigationItem } from '@treo';

export const defaultNavigation: TreoNavigationItem[] = [
  {
    id: 1,
    type: 'collapsable',
    icon: 'shopping_cart',
    iconClasses: 'primary',
    title: 'Orders',
    children: [
      {
        id: 2,
        title: 'Search Orders',
        type: 'basic',
        icon: 'search',
        iconClasses: 'primary',
        link: '/orders/search',
      },
      {
        id: 3,
        title: 'Create Order',
        type: 'basic',
        icon: 'add',
        iconClasses: 'primary',
        link: '/orders/create',
      },
    ],
  },
  {
    id: 4,
    type: 'basic',
    icon: 'remove_shopping_cart',
    iconClasses: 'primary',
    title: 'Returns',
    link: '/returns',
  },
  {
    id: 5,
    type: 'basic',
    icon: 'import_export',
    iconClasses: 'primary',
    title: 'Imports',
    link: '/imports',
  },
  {
    id: 6,
    type: 'basic',
    icon: 'shopping_cart',
    iconClasses: 'primary',
    title: 'External Transfers',
    link: '/external-transfers',
  },
];

export const compactNavigation: TreoNavigationItem[] = [
  {
    id: 1,
    title: 'Starter',
    type: 'aside',
    icon: 'apps',
    children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
  },
];
export const futuristicNavigation: TreoNavigationItem[] = [
  {
    id: 1,
    title: 'Example component',
    type: 'basic',
    icon: 'hi_outline:chart-pie',
    link: '/example',
  },
  {
    id: 2,
    title: 'Dummy menu item #1',
    icon: 'hi_outline:calendar',
    type: 'basic',
  },
  {
    id: 3,
    title: 'Dummy menu item #1',
    icon: 'hi_outline:user-group',
    type: 'basic',
  },
];
export const horizontalNavigation: TreoNavigationItem[] = [
  {
    id: 1,
    title: 'Starter',
    type: 'group',
    icon: 'apps',
    children: [], // This will be filled from defaultNavigation so we don't have to manage multiple sets of the same navigation
  },
];
