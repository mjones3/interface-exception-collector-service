## THEME LIB API

### Icons Types

| Theme        | Description                   | Usage                                                         |
| ------------ | ----------------------------- | ------------------------------------------------------------- |
| `dripicons`  | Drip Icons Theme              | `<mat-icon [svgIcon]="'dripicons:alarm'"></mat-icon>`         |
| `hi_outline` | Heroicons Outline Icons Theme | `<mat-icon [svgIcon]="'hi_outline:academic-cap'"></mat-icon>` |
| `hi_solid`   | Heroicons Solid Icons Theme   | `<mat-icon [svgIcon]="'hi_solid:academic-cap'"></mat-icon>`   |
| `rsa`        | Rsa Icons Theme               | `<mat-icon [svgIcon]="'rsa:product-plasma'"></mat-icon>`      |

### Generate new icon set

1. Copy the icons to `libs/theme/icon-svg/svg/${theme_name}` e.g. `${theme_name} = new_icons`
2. Add the new icon set to icon-types.model.ts file in `libs/theme/icon-svg/templates/icon-types.model.ts`

   ```
   export interface Manifest {
    ...
    new_icons: string[];
   }

   export type ThemeType = ... | 'new_icons';
   export type ThemeTypeUpperCase = ... | 'NewIcons';
   ```

3. Add the new icon set themeDefinitions array in `libs/theme/icon-svg/utils/index.ts`

   ```
   // Add new 'newIconsThemeDef' variable
   export const newIconsThemeDef: ThemeDefinition = {
    theme: 'new_icons',
    themeSuffix: 'NewIcons'
   };
   export const themeDefinitions: ThemeDefinition[] = [
       ...
       newIconsThemeDef
   ];

   export const getThemeByPath = (path: string) => {
       ...
       else if (path.search(newIconsThemeDef.theme as string) > 0) {
           theme = newIconsThemeDef.theme;
       }
       return theme;
   };
   ```

4. Update manifest in `libs/theme/icon-svg/scripts/generate.manifest.ts`

   ```
   const manifestRender = template(`
   // This manifest file is generated automatically.
   import { Manifest } from './types';

   export const manifest: Manifest = {
     ...
     new_icons: [
       <%= new_icons %>
     ]
   };`);
   const manifestContent: {
       ...
       new_icons: string[];
   } = {
       ...
       new_icons: [],
   };
   ...

   await fsPromises.writeFile(
       path.resolve(__dirname, `../../src/lib/manifest.ts`),
       manifestRender({
         ...
         new_icons: manifestContent.new_icons.join(', '),
       })
   );
   ```

5. Run this commands to generate new icon set
   ```
   cd libs/theme/icon-svg
   npm install
   npm run g
   ```

### Add new icons to an existing theme

1. Copy the icons to `libs/theme/icon-svg/svg/${theme_name}` e.g. `${theme_name} = rsa`

2. Run this commands to generate the new icon definitions
   ```
   cd libs/theme/icon-svg
   npm install
   npm run g
   ```
3. Add the icon to the icons.ts in the project where you want to use it.
   Ex. manufacturing-ui/src/icons.ts.

###Include an existing Icon on a Project

Add the icon to the icons.ts in the project where you want to use it.
Ex. manufacturing-ui/src/icons.ts.
