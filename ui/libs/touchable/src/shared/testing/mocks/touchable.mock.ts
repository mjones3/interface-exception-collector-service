import {Description, Option} from '@rsa/commons';

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

export const bagTypesMock: Option[] = [{
  name: 'RC PL TRIPLE',
  selectionKey: '1'
}, {
  name: 'RC PL QUAD',
  selectionKey: '2'
}, {
  name: 'RC2D TRIPLE',
  selectionKey: '3'
}, {
  name: 'RC TRIPLE',
  selectionKey: '4'
}, {
  name: 'RC DOUBLE',
  selectionKey: '5'
}];

export const discardReasonsMock: Option[] = [
  {name: 'POSITIVE HBSAG', selectionKey: '0'},
  {name: 'SARS 28 DAY DISCARD', selectionKey: '1'},
  {name: 'EXP./SOLD TO TBS', selectionKey: '2'},
  {name: 'EXP/SOLD TO PBL', selectionKey: '3'},
  {name: '2 LOW ELEVATED ALT', selectionKey: '4'},
  {name: 'NO TUBES', selectionKey: '5'},
  {name: 'TESTING NOT COMPLETE', selectionKey: '6'},
  {name: 'POSITIVE STS', selectionKey: '7'},
  {name: 'POSITIVE DAT', selectionKey: '8'},
  {name: 'COLD AGGLUTININ', selectionKey: '9'},
  {name: 'OTHER MEDICAL REASON', selectionKey: '10'},
  {name: 'LAB. ACCIDENT', selectionKey: '11'},
  {name: 'AIR CONTAMINATED', selectionKey: '12'},
  {name: 'AUTOLOGOUS - NOT USED', selectionKey: '13'},
  {name: 'ICTERUS/HEMOLYSIS', selectionKey: '14'},
  {name: 'MISCELLANEOUS', selectionKey: '15'},
  {name: 'OUT OF TEMP.CONTROL', selectionKey: '16'},
  {name: 'QNS OR SHORT DRAW', selectionKey: '17'},
  {name: 'UNIT EXPIRED', selectionKey: '18'},
  {name: 'POOR YIELD', selectionKey: '19'},
  {name: 'INCORRECT PRODUCT DESIG.', selectionKey: '20'},
  {name: 'EXCESSIVE VOLUME', selectionKey: '21'},
  {name: 'EXCESSIVE RBC\'S', selectionKey: '22'},
  {name: 'CLOTS', selectionKey: '23'},
  {name: 'ANTIBODY PRESENT', selectionKey: '24'},
  {name: 'THERAPEUTIC', selectionKey: '25'},
  {name: 'CONTAMINATED', selectionKey: '26'},
  {name: 'LIPEMIC-DISCARDED', selectionKey: '27'},
  {name: 'UNIT MISSING-NO PRODUCTS', selectionKey: '28'},
  {name: 'HIGH RISK HIV', selectionKey: '29'},
  {name: 'QUALITY CONTROL TESTING', selectionKey: '30'},
  {name: 'HIV-1 POSITIVE', selectionKey: '31'},
  {name: 'SELF-EXCLUSION', selectionKey: '32'},
  {name: 'HIVC', selectionKey: '33'},
  {name: 'HIV-2 POSITIVE', selectionKey: '34'},
  {name: 'NON-NEUTRALIZABLE', selectionKey: '35'},
  {name: 'SUNDRY', selectionKey: '36'},
  {name: 'POSITIVE HCV', selectionKey: '37'},
  {name: 'POSITIVE HBCAB', selectionKey: '38'},
  {name: '12 MONTHS DEFERRAL', selectionKey: '39'},
  {name: 'HTLV POSITIVE/1X', selectionKey: '40'},
  {name: 'HTLV POSITIVE/2X', selectionKey: '41'},
  {name: 'HIV TEST RESULTS REQ.', selectionKey: '42'},
  {name: 'HIV-1-AG', selectionKey: '43'},
  {name: 'HBSAG-ORTHO RR/ABBOTT NR', selectionKey: '44'},
  {name: 'POSITIVE NATHCV', selectionKey: '45'},
  {name: 'WESTERN BLOT INDETERM.', selectionKey: '46'},
  {name: 'RIBA CONF.POSITIVE', selectionKey: '47'},
  {name: 'POSITIVE NATHIV', selectionKey: '48'},
  {name: 'UNRELIABLE DONOR', selectionKey: '49'},
  {name: 'WEST NILE VIRUS RISK', selectionKey: '50'},
  {name: 'TEMPORARY QUARANTINE', selectionKey: '51'},
  {name: 'POSITIVE TEST RESULT', selectionKey: '52'},
  {name: 'FAILED FILTRATION SICKLE TRAIT POSITIVE', selectionKey: '53'},
  {name: 'POS BACTERIAL DETECTION', selectionKey: '54'},
  {name: 'INITIAL HBCAB POSITIVE', selectionKey: '55'},
  {name: 'CHAGAS POSITIVE', selectionKey: '56'},
  {name: 'RIPA POSITIVE', selectionKey: '57'},
  {name: 'DISCARDED IN HOSPITAL', selectionKey: '58'},
  {name: 'SARS 14 DAY DISCARD', selectionKey: '59'},
  {name: 'HIGH ELEVATED ALT', selectionKey: '60'},
  {name: 'LOW ELEVATED ALT', selectionKey: '61'},
  {name: 'DISCARD MW/R', selectionKey: '62'},
  {name: 'Post transfusion care', selectionKey: '63'},
  {name: 'STS-1 INDEFINITE DEFERRAL', selectionKey: '64'},
  {name: 'TRALI', selectionKey: '65'},
  {name: 'WEST NILE VIRUS', selectionKey: '66'},
  {name: 'DENGUE FEVER', selectionKey: '67'},
  {name: 'ZIKA POSITIVE', selectionKey: '68'},
  {name: 'POSITIVE BABESIA - IND', selectionKey: '69'}
];

export const quarantineReasonsMock: Option[] = [
  {name: 'ABO DISCREPANCY', selectionKey: '0'},
  {name: 'ABS POSITIVE', selectionKey: '1'},
  {name: 'ABS POSITIVE', selectionKey: '2'},
  {name: 'BACT POSITIVE', selectionKey: '3'},
  {name: 'BCA UNIT NEEDED', selectionKey: '4'},
  {name: 'CCP ELIGIBLE', selectionKey: '5'},
  {name: 'COMPLIANCE', selectionKey: '6'},
  {name: 'DEFERRAL', selectionKey: '7'},
  {name: 'FAILED VISUAL INSPECTION', selectionKey: '8'},
  {name: 'IN PROCESS HOLD', selectionKey: '9'},
  {name: 'PENDING FURTHER REVIEW / INSPECTION', selectionKey: '10'},
  {name: 'QC FAILURE', selectionKey: '11'},
  {name: 'QC IN PROCESS HOLD', selectionKey: '12'},
  {name: 'SAVE PLASMA FOR CTS', selectionKey: '13'}
];

export const productsMock: Option[] = [
  {
    name: 'PLASMA APHERESIS - BAG 1',
    selectionKey: '1',
    icon: 'product-plasma'
  }, {
    name: 'PLASMA APHERESIS - BAG 2',
    selectionKey: '2',
    icon: 'product-plasma'
  }, {
    name: 'PLATELET APHERESIS - BAG A',
    selectionKey: '3',
    icon: 'product-platelets'
  }, {
    name: 'PLATELET APHERESIS - BAG B',
    selectionKey: '4',
    icon: 'product-platelets'
  }, {
    name: 'RBC APHERESIS - BAG A',
    selectionKey: '5',
    icon: 'product-rbc'
  }, {
    name: 'RBC APHERESIS - BAG B',
    selectionKey: '6',
    icon: 'product-rbc'
  }
];

export const centrifugationProcessTypesMock: Option[] = [{
  name: 'Hard Spin',
  selectionKey: '1',
  icon: 'product-rbc',
  url: '/centrifugation/hard-spin'
}, {
  name: 'First Soft Spin',
  selectionKey: '2',
  icon: 'product-rbc',
  url: '/centrifugation/first-soft-spin'
}, {
  name: 'Second Soft Spin',
  selectionKey: '2',
  icon: 'product-rbc',
  url: '/centrifugation/second-soft-spin'
}];

export const descriptionsMock: Description[] = [{
  label: 'Unit Number',
  value: 'WXXXXXXXXXXX'
}, {
  label: 'Donation Date',
  value: '07/24/20220'
}, {
  label: 'Location',
  value: 'Lauderhill Donor'
}, {
  label: 'Staging Area',
  value: 'Miami'
}, {
  label: 'Machine Type',
  value: 'ALYX'
}, {
  label: 'Serial Number',
  value: 'D40061'
}, {
  label: 'Product Type',
  value: 'RBC Apheresis Bag A'
}];

export const additiveSolutionMock: Option[] = [
  {name: 'QC', selectionKey: '0', icon: 'product-rbc'},
  {name: 'Validation', selectionKey: '1', icon: 'product-whole-blood'},
  {name: 'None', selectionKey: '2', icon: 'product-platelets'},
  {name: 'Monthly QC', selectionKey: '3', icon: 'product-platelets'},
  {name: 'Training QC', selectionKey: '4', icon: 'product-platelets'},
  {name: 'Qualification', selectionKey: '5', icon: 'product-platelets'},
  {name: 'Requalification', selectionKey: '6', icon: 'product-platelets'},
];
