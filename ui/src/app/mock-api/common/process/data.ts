import { ProcessProductVersionModel } from 'app/shared/models';
import { ProcessProductModel } from 'app/shared/models/process-product.model';

export const process: ProcessProductModel = {
    id: '3638168f-f78e-4541-8055-af8fdb6f9623',
    descriptionKey: 'Distribution',
    orderNumber: 1,
    active: true,
    properties: new Map([
        ['arc-default-logo', 'assets/images/logo/ARC-logo.PNG'],
        [
            'INVESTIGATIONAL_DEVICE',
            'CAUTION – Investigational device. Limited by Federal (or United States) law to investigational use.',
        ],
        ['release_number', 'V1.0 15.01.09282022'],
    ]),

};

export const productVersion: ProcessProductVersionModel = {
    id: '1',
    buildVersion: '1.0.0',
    productId: '3638168f-f78e-4541-8055-af8fdb6f9623',
    releaseNotes: '1.0.0',
    releaseVersion: '1.0.0',
    createDate: new Date(),
};
