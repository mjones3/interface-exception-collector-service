import { SelectOptionDto } from '../models';

export const SELECT_ALL_VALUE = 'select-all';

export const firstSelectedOption = (
    selectedOptions: string[],
    allOptions: SelectOptionDto[]
) => {
    return selectedOptions?.length
        ? allOptions.find((opt) => selectedOptions.includes(opt.optionKey))
              ?.optionDescription || ''
        : '';
};

export const selectedOptionsCount = (selectedOptions: string[]) => {
    const optionsWithouthSelectAll =
        selectedOptions?.filter((value) => value !== SELECT_ALL_VALUE) ?? [];
    return optionsWithouthSelectAll.length > 1
        ? `(+${optionsWithouthSelectAll.length - 1} ${optionsWithouthSelectAll.length === 2 ? 'other' : 'others'})`
        : '';
};
