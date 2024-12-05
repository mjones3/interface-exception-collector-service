import { TemplateRef } from '@angular/core';

export interface Column {
    field: string;
    header: string;
    headerCls?: string;
    hideHeader?: boolean;
    templateRef?: string;
    hidden?: boolean;
    sortable?: boolean;
    sortFieldName?: string;
    default?: boolean;
}

export interface Sort {
    sort: string;
}

export interface TableConfiguration {
    showDeleteBtn?: boolean;
    showViewBtn?: boolean;
    showPagination?: boolean;
    showSorting?: boolean;
    expandableRows?: boolean;
    expandableKey?: string;
    pageSize?: number[];
    title?: string;
    noPaddingTitle?: boolean;
    hasMenu?: boolean;
    menus?: HeaderMenu[];
    showExpandAllRows?: boolean;
    selectRowOnClick?: boolean;
    component?: string;
}

export interface ColumnConfig {
    columnId: string;
    columnHeader: string;
    columnHeaderRefKey?: string;
    columnTemplateRefKey?: string;
    class?: string;
    headerClass?: string;
    sort?: boolean;
    filter?: TableColumnFilter;
    actions?: TableColumnAction[];
    hidden?: boolean;
    customHeaderTempRefKey?: string;
}

export interface TableColumnAction {
    label: string;
    click: Function;
    class?: string;
}

export interface HeaderMenu {
    label: string;
    click?: Function;
    icon?: string;
    disabled?: boolean;
    submenu?: HeaderMenu[];
}

export interface TableColumnFilterOption {
    id: number;
    name: string;
}

export interface TableColumnFilter {
    callback: Function;
    selected?: TableColumnFilterOption[];
    multiple: boolean;
    options: TableColumnFilterOption[];
}

export interface TableColumnAction {
    label: string;
    click: Function;
    class?: string;
}

export interface AngularMaterialTableConfiguration {
    title?: string;
    menus?: TableMenu[];
    showPagination?: boolean;
    pageSize?: number;
    showExpandAll?: boolean;
    expandableKey?: string;
    selectable?: boolean;
    columns: TableColumn[];
    showSorting?: boolean;
}

export interface TableMenu {
    id: string;
    label: string;
    subMenu?: TableMenu;
    disabled?: boolean;
    icon?: string;
    click?: () => void;
}

export interface TableColumn {
    id: string;
    header?: string;
    sort?: boolean;
    icon?: boolean;
    headerTempRef?: TemplateRef<Element>;
    columnTempRef?: TemplateRef<Element>;
}

export interface TableDataSource {
    expanded?: boolean;
}

export interface Tag {
    label: string;
    icon?: string;
    classes?: TagClass;
}

export interface TagClass {
    wrapper?: string;
    text?: string;
    icon?: string;
}
