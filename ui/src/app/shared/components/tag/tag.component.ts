import { NgClass } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export enum TagType {
    'info',
    'primary',
    'warning',
    'caution',
    'success',
}

export interface TagColorDefinition {
    backgroundColor: string;
    textColor: string;
    leftIconColor: string;
    rightIconColor: string;
}

/**
 * For icon references: "leftIconRef" and "rightIconRef",
 * check IconsService icons registry definitions.
 */
@Component({
    selector: 'app-tag',
    standalone: true,
    imports: [NgClass, MatIconModule],
    templateUrl: './tag.component.html',
})
export class TagComponent {
    @Input({ required: true }) label!: string;
    @Input() type: keyof typeof TagType = 'info';
    @Input() leftIconRef: string;
    @Input() rightIconRef: string;

    readonly TagTypeMap: Record<keyof typeof TagType, TagColorDefinition> = {
        info: {
            backgroundColor: 'bg-gray-100',
            textColor: 'text-gray-700',
            leftIconColor: 'fill-gray-700',
            rightIconColor: 'fill-gray-700',
        },
        primary: {
            backgroundColor: 'bg-indigo-200',
            textColor: 'text-gray-700',
            leftIconColor: 'fill-gray-700',
            rightIconColor: 'fill-gray-700',
        },
        warning: {
            backgroundColor: 'bg-red-100',
            textColor: 'text-gray-700',
            leftIconColor: 'fill-gray-700',
            rightIconColor: 'fill-gray-700',
        },
        caution: {
            backgroundColor: 'bg-amber-100',
            textColor: 'text-gray-700',
            leftIconColor: 'fill-gray-700',
            rightIconColor: 'fill-gray-700',
        },
        success: {
            backgroundColor: 'bg-green-100',
            textColor: 'text-gray-700',
            leftIconColor: 'fill-gray-700',
            rightIconColor: 'fill-gray-700',
        },
    };
}
