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
    CRYOPRECIPITATE
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
    PLASMA_MFG_INJECTABLE: 'Plasma Manufacturing Injectable',
    PLASMA_MFG_NONINJECTABLE: 'Plasma Manufacturing Non-Injectable',
    RED_BLOOD_CELLS: 'Red Blood Cells',
    LEUKOREDUCED: 'Leukoreduced',
    RED_BLOOD_CELLS_LEUKOREDUCED: 'Red Blood Cells Leukoreduced',
    WHOLE_BLOOD: 'Whole Blood',
    WHOLE_BLOOD_LEUKOREDUCED: 'Whole Blood Leukoreduced',
    RP_FROZEN_WITHIN_120_HOURS: 'RP FROZEN WITHIN 120 HOURS',
    RP_FROZEN_WITHIN_24_HOURS: 'RP FROZEN WITHIN 24 HOURS',
    RP_NONINJECTABLE_FROZEN: 'RP NONINJECTABLE FROZEN',
    RP_NONINJECTABLE_LIQUID_RT: 'RP NONINJECTABLE LIQUID RT',
    RP_FROZEN_WITHIN_72_HOURS: 'RP FROZEN WITHIN 72 HOURS',
    RP_NONINJECTABLE_REFRIGERATED: 'RP NONINJECTABLE REFRIGERATED',
    APHERESIS_PLATELETS_LEUKOREDUCED: 'Apheresis Platelets Leukoreduced',
    PRT_APHERESIS_PLATELETS: 'PRT Apheresis Platelets',
    CRYOPRECIPITATE: 'CRYOPRECIPITATE'
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
