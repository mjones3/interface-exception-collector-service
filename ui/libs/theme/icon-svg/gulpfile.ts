import {readFileSync} from 'fs';
import {parallel, series} from 'gulp';
import {resolve} from 'path';
import {clean, copy} from './tasks';
import {generateAllIcons} from "./tasks/generate.all.icons";
import {generateEntry} from "./tasks/generate.entry";
import {themeDefinitions} from './utils';

const iconTemplate = readFileSync(
  resolve(__dirname, './templates/icon.ts.ejs'),
  'utf8'
);

const basePath = '../src/lib';

// @ts-ignore
export default series(
  // 1. clean
  clean([`${basePath}/icons`]),

  parallel(
    // 2.1 copy helpers.ts, types.ts
    copy({
      from: ['templates/*.ts'],
      toDir: basePath
    }),

    // 2.2 generate all icons
    ...generateAllIcons(basePath, iconTemplate, themeDefinitions)()
  ),
  // 3.1 generate entry file: src/index.ts
  generateEntry({
    entryName: 'public_api.ts',
    from: [`${basePath}/icons/**/*.ts`],
    toDir: basePath,
    banner: '// This public_api.ts file is generated automatically.\n\n',
    template: `export { <%= identifier %> } from '<%= path %>';`,
    mapToInterpolate: ({name: identifier, theme}) => {
      return {
        identifier,
        path: `./icons/${theme}/${identifier}`
      };
    }
  }),
);
