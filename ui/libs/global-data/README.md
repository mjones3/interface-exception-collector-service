# global-data

Global Data is a library that contains the default root state across application handle by NgRx.

## Default Store State

```text
{
  "auth": AuthState,
  "facility": FacilityState,
  "configuration": ConfigState
}
```

## Import Global Data Lib into CoreModule

```typescript
import { GlobalDataModule } from '@rsa/global-data';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

@NgModule({
  imports: [
    // ....
    GlobalDataModule,
  ],
  providers: [
    // ...
  ],
})
export class CoreModule {}
```

## Getting Auth Data

```typescript
import { Component } from '@angular/core';
import { getAuthState } from '@rsa/global-data';
import { Store } from '@ngrx/store';

@Component({
  selector: 'rsa-example',
  templateUrl: './example.component.html',
})
export class ExampleComponent implements OnInit, AfterViewInit {
  constructor(private store: Store) {
    store.select(getAuthState).subscribe(auth => console.log(auth));
  }
}
```

## Running unit tests

Run `nx test global-data` to execute the unit tests.
