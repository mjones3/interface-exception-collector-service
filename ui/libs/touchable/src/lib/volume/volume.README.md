# Volume Common Component

The Volume component can be used in different processes in the application for calculate volume step.

## TODO: Volume Refactor to add new weight input!!

Part of the new calculate volume shared component changes needs to be added weight input to enter manually the product
weight, the changes to the common component it is done in branch `feature/update-rsa-volume`

**Note**: I suggest if there is not too many breaking changes integrating `CalculateVolumeAphPlasmaComponent`
and `CalculateVolumeAphPlasmaComponent` into `CalculateVolumeEmbeddedProcessComponent` to have all calculate volume
logic integrated into one component (DRY)

### TODO Changes List

- [ ] All modules that use `CalculateVolumeEmbeddedProcessComponent` their services must be updated

  ```text
    WholeBloodFiltrationCalculateVolumeService
    CryoPreparationVolumeService
    CryoPoolingVolumeService
    LeukoReductionCalculateVolumeService
    WholeBloodLowTiterCalculateVolumeService
    PlasmaPreparationCalculateVolumeService
    PlateletPreparationVolumeService
    RbcPreparationVolumeService
    SourceLeukocytesPreparationCalculateVolumeService
    WholeBloodPreparationCalculateVolumeService
  ```

- [ ] Update `CalculateVolumeAphPlasmaComponent` component

- [ ] Update `CalculateVolumeRbcaComponent` component

### Inputs

`@Input headerTitle: string` Header title for the volume section.

`@Input subTitleInstructions: string` Instruction displayed after the header section in calculate volume area.

`@Input disabledVolume: boolean` Disable calculate volume and get default volume view.

### Output

`@Output() calculateVolumeFormSubmitted` Emits an event when a user clicks on **Calculate Volume Button**, this button
is only enable when useIntegratedScale form property is `true`, after calling the weight service the volume form have to
be updated with the value that comes from API.

## Ng-Content

Pass a template to the component, and it will be displayed in the top of Calculate Volume card in
the `<ng-content></ng-content>` section.

## Example

```ts
import { Component } from '@angular/core';

@Component({
  template: `
    <rsa-volume
      [headerTitle]="volumeTitle"
      [subTitleInstructions]="volumeInstructions"
      (calculateVolumeFormSubmitted)="calculateVolume($event)"
    >
      <rsa-information-card
        title="Donation Information"
        [maxRows]="3"
        [descriptions]="descriptions"
      ></rsa-information-card>
    </rsa-volume>
  `,
})
class TestComponent {
  volumeTitle: string = 'Title';
  volumeInstructions: string = 'Instructions';
  descriptions: any[] = [];

  calculateVolume($event) {}
}
```
