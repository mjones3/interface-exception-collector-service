import {
  Option,
  OrderBloodTypeDto,
  OrderItemProductDTO,
  OrderProductAttributeDto,
  OrderProductAttributeOptionDto,
  OrderProductFamilyDto,
} from '@rsa/commons';
import {
  LabelProdCategory,
  Order,
  OrderFee,
  OrderPriority,
  OrderProduct,
  ServiceFee,
  ShippedProduct,
  ShippingMethod,
} from '@rsa/distribution/core/models/orders.model';

export const orderFieldsMock: Option[] = [
  {
    selectionKey: 'externalId',
    descriptionKey: 'external-order-id.label',
  },
  {
    selectionKey: 'orderNumber',
    descriptionKey: 'order-number.label',
  },
];

export const shipToCustomersMock: Option[] = [
  {
    selectionKey: '1',
    descriptionKey: 'AdventHealth Zephyrhills',
  },
  {
    selectionKey: '2',
    descriptionKey: 'Zephyrhills General',
  },
];

export const billToCustomersMock: Option[] = [
  {
    selectionKey: '1',
    descriptionKey: 'AdventHealth Zephyrhills',
  },
  {
    selectionKey: '2',
    descriptionKey: 'Zephyrhills General',
  },
];

export const statusesMock: Option[] = [
  {
    selectionKey: '0',
    descriptionKey: 'All',
  },
  {
    selectionKey: '1',
    descriptionKey: 'Open',
  },
  {
    selectionKey: '2',
    descriptionKey: 'Filled',
  },
  {
    selectionKey: '3',
    descriptionKey: 'Shipped',
  },
  {
    selectionKey: '4',
    descriptionKey: 'Cancelled',
  },
];

export const prioritiesMock: OrderPriority[] = [
  {
    id: 1,
    name: 'STAT',
  },
  {
    id: 2,
    name: 'Processing Time',
  },
  {
    id: 3,
    name: 'Promise Date',
  },
  {
    id: 4,
    name: 'Availability of Product',
  },
];

export const serviceFeesMock: ServiceFee[] = [
  {
    id: 1,
    name: 'STAT Fee',
  },
  {
    id: 2,
    name: 'PLT Ab/test',
  },
  {
    id: 3,
    name: 'Irl',
  },
];

export const orderFeesMock: OrderFee[] = [
  { id: 1, serviceFee: serviceFeesMock[0], quantity: 10 },
  { id: 2, serviceFee: serviceFeesMock[1], quantity: 14 },
];

export const shippingMethodsMock: ShippingMethod[] = [
  {
    id: 1,
    name: 'Fedex',
  },
  {
    id: 2,
    name: 'USPS',
  },
  {
    id: 3,
    name: 'UPS',
  },
];

export const labelingProdCategoriesMock: LabelProdCategory[] = [
  {
    id: 1,
    name: 'Refrigerated',
  },
  {
    id: 2,
    name: 'Not Refrigerated',
  },
];

export const ordersMock: Order[] = [
  {
    id: 1,
    externalOrderId: '144444435ASD',
    orderNumber: '25767544',
    shipToCustomerId: 756756734,
    billToCustomerId: 8788764,
    createDate: '12/12/2021',
    shipDate: '12/12/2021',
    priority: { ...prioritiesMock[0] },
    shippingMethod: { ...shippingMethodsMock[0] },
    labelingProdCategory: { ...labelingProdCategoriesMock[0] },
    customerSearchCriteria: 'customer-name',
    status: 'Shipped',
    addedFees: [...orderFeesMock],
    comments: 'Comment',
  },
  {
    id: 2,
    externalOrderId: '123123125435ASD',
    orderNumber: '200432544',
    shipToCustomerId: 62754234,
    billToCustomerId: 872344,
    createDate: '12/12/2021',
    shipDate: '12/12/2021',
    priority: { ...prioritiesMock[0] },
    shippingMethod: { ...shippingMethodsMock[0] },
    labelingProdCategory: { ...labelingProdCategoriesMock[0] },
    customerSearchCriteria: 'customer-id',
    status: 'Cancelled',
    addedFees: [...orderFeesMock],
    comments: 'Comment',
  },
  {
    id: 3,
    externalOrderId: '123123125435ASD',
    orderNumber: '200432544',
    shipToCustomerId: 62754234,
    billToCustomerId: 872344,
    createDate: '12/12/2021',
    shipDate: '12/12/2021',
    priority: { ...prioritiesMock[0] },
    shippingMethod: { ...shippingMethodsMock[0] },
    labelingProdCategory: { ...labelingProdCategoriesMock[0] },
    customerSearchCriteria: 'customer-name',
    status: 'Shipped',
    addedFees: [...orderFeesMock],
  },
  {
    id: 4,
    externalOrderId: '123123125435ASD',
    orderNumber: '200432544',
    shipToCustomerId: 62754234,
    billToCustomerId: 872344,
    createDate: '12/12/2021',
    shipDate: '12/12/2021',
    priority: { ...prioritiesMock[0] },
    shippingMethod: { ...shippingMethodsMock[0] },
    labelingProdCategory: { ...labelingProdCategoriesMock[0] },
    customerSearchCriteria: 'customer-name',
    status: 'Open',
    addedFees: [...orderFeesMock],
  },
  {
    id: 5,
    externalOrderId: '123123125435ASD',
    orderNumber: '200432544',
    shipToCustomerId: 62754234,
    billToCustomerId: 872344,
    createDate: '12/12/2021',
    shipDate: '12/12/2021',
    priority: { ...prioritiesMock[1] },
    shippingMethod: { ...shippingMethodsMock[0] },
    labelingProdCategory: { ...labelingProdCategoriesMock[0] },
    customerSearchCriteria: 'customer-name',
    status: 'Open',
    addedFees: [...orderFeesMock],
  },
];
//CMV Neg HgbS Neg Irradiated Antigen Tested Aliquot Bags
export const productAttributesMock: OrderProductAttributeDto[] = [
  {
    propertyValue: '1',
    descriptionKey: 'CMV Neg',
    attributeValue: '1',
    color: '',
    propertyKey: '',
    orderNumber: 1,
  },
  {
    propertyValue: '2',
    descriptionKey: 'HgbS Neg',
    attributeValue: '1',
    color: '',
    propertyKey: '',
    orderNumber: 1,
  },
  {
    propertyValue: '3',
    descriptionKey: 'Irradiated',
    attributeValue: '1',
    color: '',
    propertyKey: '',
    orderNumber: 1,
  },
  {
    propertyValue: '4',
    descriptionKey: 'Antigen Tested',
    attributeValue: '1',
    color: '',
    propertyKey: '',
    orderNumber: 1,
  },
  {
    propertyValue: '5',
    descriptionKey: 'Aliquot Bags',
    attributeValue: '1',
    color: '',
    propertyKey: '',
    orderNumber: 1,
  },
];

export const bloodTypesMock: OrderBloodTypeDto[] = [
  {
    id: 1,
    productFamily: 'plasma',
    bloodTypeValue: 'A',
    orderNumber: 1,
    active: true,
    descriptionKey: 'A',
  },
  {
    id: 2,
    productFamily: 'plasma',
    bloodTypeValue: 'B',
    orderNumber: 1,
    active: true,
    descriptionKey: 'B',
  },
  {
    id: 3,
    productFamily: 'plasma',
    bloodTypeValue: 'o',
    orderNumber: 1,
    active: true,
    descriptionKey: 'O',
  },
  {
    id: 4,
    productFamily: 'plasma',
    bloodTypeValue: 'AB',
    orderNumber: 1,
    active: true,
    descriptionKey: 'AB',
  },
  {
    id: 5,
    productFamily: 'plasma',
    bloodTypeValue: 'OP',
    orderNumber: 1,
    active: true,
    descriptionKey: 'O POS',
  },
  {
    id: 6,
    productFamily: 'plasma',
    bloodTypeValue: 'ABP',
    orderNumber: 1,
    active: true,
    descriptionKey: 'AB POS',
  },
  {
    id: 7,
    productFamily: 'plasma',
    bloodTypeValue: 'ABN',
    orderNumber: 1,
    active: true,
    descriptionKey: 'AB NEg',
  },
  {
    id: 8,
    productFamily: 'plasma',
    bloodTypeValue: 'any',
    orderNumber: 1,
    active: true,
    descriptionKey: 'ANY',
  },
];

export const productFamiliesMock: OrderProductFamilyDto[] = [
  {
    familyType: '1',
    descriptionKey: 'Red Blood Cells',
    familyValue: '',
    familyCategory: '',
    active: true,
  },
  {
    familyType: '2',
    descriptionKey: 'Plasma',
    familyValue: '',
    familyCategory: '',
    active: true,
  },
  {
    familyType: '3',
    descriptionKey: 'Platelets',
    familyValue: '',
    familyCategory: '',
    active: true,
  },
];

export const antigensTestedMock: OrderProductAttributeOptionDto[] = [
  {
    id: 1,
    descriptionKey: 'C',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 2,
    descriptionKey: 'E',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 3,
    descriptionKey: 'c',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 4,
    descriptionKey: 'e',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 5,
    descriptionKey: 'K',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 6,
    descriptionKey: 'Fya',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 7,
    descriptionKey: 'Fyb',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 8,
    descriptionKey: 'Jkb',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 9,
    descriptionKey: 'Jkb',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 20,
    descriptionKey: 'S',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 10,
    descriptionKey: 's',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 11,
    descriptionKey: 'M',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 12,
    descriptionKey: 'N',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 13,
    descriptionKey: 'k-cellano',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 14,
    descriptionKey: 'Lea',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 15,
    descriptionKey: 'Cw',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 16,
    descriptionKey: 'Cw',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 17,
    descriptionKey: 'Jsa',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 18,
    descriptionKey: 'V',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
  {
    id: 19,
    descriptionKey: 'Goa',
    optionValue: '',
    orderNumber: 1,
    active: true,
  },
];

export const filledProductsMock: OrderItemProductDTO[] = [
  {
    unitNumber: 'W123123123123',
    productCode: 'E017800',
    unlicensed: false,
    orderItemInventory: { id: 2, orderItem: 1, order: 1, inventoryId: 1, filled: true },
  },
  {
    unitNumber: 'W123123123126',
    productCode: 'E017800',
    unlicensed: true,
    orderItemInventory: { id: 2, orderItem: 1, order: 1, inventoryId: 1, filled: false },
  },
];

export const orderProductsMock: OrderProduct[] = [
  {
    id: 1,
    quantity: 2,
    bloodType: { ...bloodTypesMock[2] },
    bloodTypeAndQuantity: [
      {
        quantity: 2,
        bloodType: { ...bloodTypesMock[2] },
      },
    ],
    productAttributes: [...productAttributesMock.slice(0, 2)],
    productFamily: { ...productFamiliesMock[0] },
    productComment: 'Comment',
    antigensTested: [...antigensTestedMock.slice(2, 5)],
    filledProducts: [...filledProductsMock],
  },
  {
    id: 2,
    bloodTypeAndQuantity: [
      {
        quantity: 2,
        bloodType: { ...bloodTypesMock[1] },
      },
    ],
    quantity: 4,
    bloodType: { ...bloodTypesMock[1] },
    productAttributes: [...productAttributesMock.slice(2, 4)],
    productFamily: { ...productFamiliesMock[1] },
    productComment: 'New Comment',
    antigensTested: [...antigensTestedMock.slice(6, 9)],
  },
];

export const shippedProductsMock: ShippedProduct[] = [...orderProductsMock.slice(0, 1)].map(prod => ({
  ...prod,
  shipmentDate: new Date(),
  employee: 'John Doe',
}));
