export enum ProductFamily {
    FROZEN_PLASMA,
    PLASMA_TRANSFUSABLE,
    PLASMA_MFG_INJECTABLE,
    PLASMA_MFG_NONINJECTABLE,
    RED_BLOOD_CELLS,
    LEUKOREDUCED,
    RED_BLOOD_CELLS_LEUKOREDUCED,
}

export const ProductFamilyMap: Record<keyof typeof ProductFamily, string> = {
    FROZEN_PLASMA: 'Frozen Plasma',
    PLASMA_TRANSFUSABLE: 'Plasma Transfusable',
    PLASMA_MFG_INJECTABLE: 'Plasma Manufacturing Injectable',
    PLASMA_MFG_NONINJECTABLE: 'Plasma Manufacturing Non-Injectable',
    RED_BLOOD_CELLS: 'Red Blood Cells',
    LEUKOREDUCED: 'Leukoreduced',
    RED_BLOOD_CELLS_LEUKOREDUCED: 'Red Blood Cells Leukoreduced',
};
