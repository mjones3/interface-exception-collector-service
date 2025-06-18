import { CommonModule } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { ButtonOption } from 'app/shared/models/custom-button-toggle.model';

@Component({
  selector: 'biopro-custom-button-toggle',
  standalone: true,
  imports: [
    MatButtonToggleModule,
    MatIconModule,
    ReactiveFormsModule,
    CommonModule,
  ],
  templateUrl: './custom-button-toggle.component.html',
  styleUrl: './custom-button-toggle.component.scss'
})
export class CustomButtonToggleComponent {
  private defaultButtonOptions: ButtonOption[] = [
    {
        value: 'YES',
        iconName: 'hand-thumb-up',
        label: 'YES'
    },
    {
        value: 'NO',
        iconName: 'hand-thumb-down',
        label: 'NO'
    }
];

showLabel = input<boolean>(false);
label = input<string>();
hideSingleSelectionIndicator = input<boolean>(false);
required = input<boolean>(false);
isDisabled = input<boolean>(false);
options = input<ButtonOption[]>(this.defaultButtonOptions);
toggleId = input.required<string>();
control = input.required<FormControl>();

optionSelected = output<void>();


getIconName(optionValue: string): string {
    return this.iconName()[optionValue] || '';
}

selectedOption() {
    this.optionSelected.emit();
}

private iconName = computed(() => {
    const names = {};
    this.options().forEach(option => {
        names[option.value] = option.iconName;
    });
    return names;
});

}
