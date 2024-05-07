import { TransferProduct } from '@rsa/distribution/core/models/external-transfers.model';

export const availableProductsMock: TransferProduct[] = [
  {
    description: 'Aph Platelet Leuko Bag A',
    productCode: 'E123435',
    inventoryId: 1,
    unitNumber: 'W123343465672',
    lastTransferDate: '2022-06-09',
    originalDateShipped: '2022-06-09',
    originallyShippedFrom: 'Miami, FL',
    originallyShippedTo: 'Orlando, FL',
  },
  {
    description: 'Aph Platelet Leuko Bag B',
    productCode: 'E123434',
    inventoryId: 2,
    unitNumber: 'W981729814221',
    lastTransferDate: '2022-06-09',
    originalDateShipped: '2022-06-09',
    originallyShippedFrom: 'Miami, FL',
    originallyShippedTo: 'Orlando, FL',
  },
  {
    description: 'Aph Platelet Leuko Bag C',
    productCode: 'E122349',
    inventoryId: 3,
    unitNumber: 'W988888814221',
    lastTransferDate: '2022-06-09',
    originalDateShipped: '2022-06-09',
    originallyShippedFrom: 'Miami, FL',
    originallyShippedTo: 'Orlando, FL',
  },
];
