export enum ProductFamily {
    FROZEN_PLASMA,
    PLASMA_TRANSFUSABLE,
    PLASMA_MFG_INJECTABLE,
    PLASMA_MFG_NONINJECTABLE,
    RED_BLOOD_CELLS,
    LEUKOREDUCED,
    RED_BLOOD_CELLS_LEUKOREDUCED,
    WHOLE_BLOOD,
    WHOLE_BLOOD_LEUKOREDUCED,
    RP_FROZEN_WITHIN_120_HOURS,
    RP_FROZEN_WITHIN_24_HOURS,
    RP_NONINJECTABLE_FROZEN,
    RP_NONINJECTABLE_LIQUID_RT,
    RP_FROZEN_WITHIN_72_HOURS,
    RP_NONINJECTABLE_REFRIGERATED,
    APHERESIS_PLATELETS_LEUKOREDUCED,
    PRT_APHERESIS_PLATELETS,
    CRYOPRECIPITATE,
    WASHED_RED_BLOOD_CELLS,
    WASHED_APHERESIS_PLATELETS,
    WASHED_PRT_APHERESIS_PLATELETS
}

export enum ProductType {
    RP_FROZEN_WITHIN_120_HOURS,
    RP_FROZEN_WITHIN_24_HOURS,
    RP_NONINJECTABLE_FROZEN,
    RP_NONINJECTABLE_LIQUID_RT,
    RP_FROZEN_WITHIN_72_HOURS,
    RP_NONINJECTABLE_REFRIGERATED,
}

export enum Priority {
    DATE_TIME,
    STAT,
    ROUTINE,
    SCHEDULED,
    ASAP,
}

export const ProductFamilyMap: Record<keyof typeof ProductFamily, string> = {
    FROZEN_PLASMA: 'Frozen Plasma',
    PLASMA_TRANSFUSABLE: 'Plasma Transfusable',
    PLASMA_MFG_INJECTABLE: 'Plasma Mfg Injectable',
    PLASMA_MFG_NONINJECTABLE: 'Plasma Mfg Noninjectable',
    RED_BLOOD_CELLS: 'Red Blood Cells',
    LEUKOREDUCED: 'Leukoreduced',
    RED_BLOOD_CELLS_LEUKOREDUCED: 'Red Blood Cells Leukoreduced',
    WHOLE_BLOOD: 'Whole Blood',
    WHOLE_BLOOD_LEUKOREDUCED: 'Whole Blood Leukoreduced',
    RP_FROZEN_WITHIN_120_HOURS: 'RP Frozen Within 120 Hours',
    RP_FROZEN_WITHIN_24_HOURS: 'RP Frozen Within 24 Hours',
    RP_NONINJECTABLE_FROZEN: 'RP Noninjectable Frozen',
    RP_NONINJECTABLE_LIQUID_RT: 'RP Noninjectable Liquid RT',
    RP_FROZEN_WITHIN_72_HOURS: 'RP Frozen Within 72 Hours',
    RP_NONINJECTABLE_REFRIGERATED: 'RP Noninjectable Refrigerated',
    APHERESIS_PLATELETS_LEUKOREDUCED: 'Apheresis Platelets Leukoreduced',
    PRT_APHERESIS_PLATELETS: 'PRT Apheresis Platelets',
    CRYOPRECIPITATE: 'Cryoprecipitate',
    WASHED_RED_BLOOD_CELLS: 'Washed Red Blood Cells',
    WASHED_APHERESIS_PLATELETS: 'Washed Apheresis Platelets',
    WASHED_PRT_APHERESIS_PLATELETS: 'Washed PRT Apheresis Platelets'
};

export const PriorityMap: Record<keyof typeof Priority, string> = {
    DATE_TIME: 'DATE-TIME',
    STAT: 'STAT',
    ROUTINE: 'ROUTINE',
    SCHEDULED: 'SCHEDULED',
    ASAP: 'ASAP',
};

export const ProductTypeMap: Record<keyof typeof ProductType, string> = {
    RP_FROZEN_WITHIN_120_HOURS: 'Recovered Plasma Frozen within 120 hours',
    RP_FROZEN_WITHIN_24_HOURS: 'Recovered Plasma Frozen within 24 hours',
    RP_NONINJECTABLE_FROZEN: 'Recovered Plasma Noninjectable Frozen',
    RP_NONINJECTABLE_LIQUID_RT: 'Recovered Plasma Noninjectable Liquid RT',
    RP_FROZEN_WITHIN_72_HOURS: 'Recovered Plasma Frozen within 72 hours',
    RP_NONINJECTABLE_REFRIGERATED:
        'Recovered Plasma Noninjectable Refrigerated',
};
