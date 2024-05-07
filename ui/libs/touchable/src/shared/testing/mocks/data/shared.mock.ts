import { Option } from '@rsa/commons';

export const optionsMock = [
  { name: 'Apheresis Plasma Parent', selectionKey: '1', icon: 'product-plasma' },
  { name: 'Apheresis Plasma BagA', selectionKey: '2', icon: 'product-plasma' },
  { name: 'Apheresis Plasma BagB', selectionKey: '3', icon: 'product-plasma' },
  { name: 'Apheresis Plasma BagC', selectionKey: '4', icon: 'product-plasma' },
  { name: 'Apheresis Plasma BagD', selectionKey: '5', icon: 'product-plasma' }
];

export const sterileConnectionProcessMock = [
  { name: '300mL', selectionKey: '0', descriptionKey: '300mL' },
  { name: '600mL', selectionKey: '1', descriptionKey: '600mL' }
];

export const scdSerialNumberMock: Option[] = [
  { name: 'W999990002212', selectionKey: '0', descriptionKey: 'W999990002212' },
  { name: 'W999990002213', selectionKey: '1', descriptionKey: 'W999990002213' }
];

export const scdWaferLotNumberMock: Option[] = [
  { name: 'W999990002215', selectionKey: '0', descriptionKey: 'W999990002215' },
  { name: 'W999990002216', selectionKey: '1', descriptionKey: 'W999990002216' }
];

export const transferContainerLotNumberMock: Option[] = [
  { name: '0002212', selectionKey: '0' },
  { name: '0002215', selectionKey: '1' }
];

export const donorIntentionsMock: Option[] = [{
  name: 'Autologous',
  selectionKey: '1'
}, {
  name: 'Allogeneic',
  selectionKey: '2'
}, {
  name: 'Directed',
  selectionKey: '3'
}, {
  name: 'Therapeutic',
  selectionKey: '4'
}, {
  name: 'Hereditary Homochromatosis',
  selectionKey: '5'
}];

export const donationTypesMock: Option[] = [{
  name: 'Whole Blood',
  selectionKey: '1',
  icon: 'product-plasma'
}, {
  name: 'Platelets and Concurrent Plasma',
  selectionKey: '2',
  icon: 'product-platelets'
}, {
  name: '2 Units RBC',
  selectionKey: '3',
  icon: 'product-rbc'
}, {
  name: 'RBC/Jumbo Plasma Combo',
  selectionKey: '4',
  icon: 'product-plasma'
}, {
  name: 'RBC with Platelets and Plasma',
  selectionKey: '5',
  icon: 'product-rbc'
}, {
  name: 'Plasma Apheresis',
  selectionKey: '6',
  icon: 'product-plasma'
}, {
  name: 'Platelet Apheresis',
  selectionKey: '7',
  icon: 'product-rbc'
}, {
  name: 'Therapeutic Pheresis',
  selectionKey: '8',
  icon: 'product-rbc'
}, {
  name: 'Therapeutic Phleb',
  selectionKey: '9',
  icon: 'product-rbc'
}, {
  name: 'No RBC Reinf Apheresis Platelets',
  selectionKey: '10',
  icon: 'product-rbc'
}, {
  name: 'Auto-double RBC',
  selectionKey: '11',
  icon: 'product-rbc'
}, {
  name: 'Other Procedures',
  selectionKey: '12',
  icon: 'product-rbc'
}, {
  name: 'No RBC Reinf Apheresis Plasma',
  selectionKey: '13',
  icon: 'product-rbc'
}, {
  name: 'Directed Platelet and Concurrent Plasma',
  selectionKey: '14',
  icon: 'product-rbc'
}, {
  name: 'Directed 2 Units RBC',
  selectionKey: '15',
  icon: 'product-rbc'
}, {
  name: 'Directed RBC with Platelets',
  selectionKey: '16',
  icon: 'product-rbc'
}, {
  name: 'Directed Apheresis no Rinseback',
  selectionKey: '17',
  icon: 'product-rbc'
}, {
  name: 'Directed Plasma Apheresis',
  selectionKey: '18',
  icon: 'product-rbc'
}, {
  name: 'Directed Other',
  selectionKey: '19',
  icon: 'product-rbc'
}, {
  name: 'Directed Platelet Apheresis',
  selectionKey: '20',
  icon: 'product-rbc'
}, {
  name: 'Directed Plasma Apheresis no Rinse',
  selectionKey: '21',
  icon: 'product-rbc'
}, {
  name: 'Apheresis Single RBC',
  selectionKey: '22',
  icon: 'product-rbc'
}, {
  name: 'No RBC Reinf Apheresis Platelets + Conc Plasma',
  selectionKey: '23',
  icon: 'product-rbc'
}, {
  name: 'Concurrent Plasma',
  selectionKey: '24',
  icon: 'product-rbc'
}, {
  name: 'RBC with Platelets',
  selectionKey: '25',
  icon: 'product-rbc'
}, {
  name: 'Pooled Platelets',
  selectionKey: '26',
  icon: 'product-rbc'
}, {
  name: 'Pooled Cryo',
  selectionKey: '27',
  icon: 'product-rbc'
}
];

export const quarantinesMock = [
  {
    id: 1,
    product: 'E3102 Apheresis Plasma Bag',
    quarantineReason: 'ABO DISCREPANCY',
    quarantineDate: '07/01/2020',
    userId: '10003',
    quarantineDescription: 'Element 1 with Description'
  },
  {
    id: 2,
    product: 'E3102 Apheresis Plasma Bag',
    quarantineReason: 'ABS POSITIVE',
    quarantineDate: '07/01/2020',
    userId: '10003',
    quarantineDescription: 'Element 2 with Description'
  },
  {
    id: 3,
    product: 'E3102 Apheresis Plasma Bag',
    quarantineReason: 'Display other comments here ',
    quarantineDate: '06/30/2020',
    userId: '10002',
    quarantineDescription: 'Element 3 with Description'
  },
  {
    id: 4,
    product: 'Apheresis Plasma Bag A',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 4 with Description'
  },
  {
    id: 5,
    product: 'Apheresis Plasma Bag B',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 5 with Description'
  },
  {
    id: 6,
    product: 'Apheresis Plasma Bag B 6',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 7,
    product: 'Apheresis Plasma Bag B 7',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 8,
    product: 'Apheresis Plasma Bag B 8',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 9,
    product: 'Apheresis Plasma Bag B 9',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 10,
    product: 'Apheresis Plasma Bag B 10',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 11,
    product: 'Apheresis Plasma Bag B 11',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 12,
    product: 'Apheresis Plasma Bag B 12',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 13,
    product: 'Apheresis Plasma Bag B 13',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 14,
    product: 'Apheresis Plasma Bag B 14',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 15,
    product: 'Apheresis Plasma Bag B 15',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 16,
    product: 'Apheresis Plasma Bag B 16',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  },
  {
    id: 17,
    product: 'Apheresis Plasma Bag B 17',
    quarantineReason: 'QC IN PROCESS HOLD',
    quarantineDate: '06/29/2020',
    userId: '10001',
    quarantineDescription: 'Element 6 with Description'
  }
];
