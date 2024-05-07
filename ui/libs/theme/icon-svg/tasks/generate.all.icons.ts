import {base} from "../plugins/svgo/presets/base";
import {ThemeDefinition, ThemeType} from "../templates/types";
import {getIdentifier} from "../utils";
import {generateIcons} from "./index";

export const generateAllIcons = (basePath: string, iconTemplate: string, defs: ThemeDefinition[]) =>
  function GenerateAllIcons() {
    const fns: (() => any)[] = [];
    defs.forEach(def => {
      // 2.2 generate icons with the themes
      fns.push(
        generateIcons({
          theme: def.theme as ThemeType,
          from: [`svg/${def.theme}/*.svg`],
          toDir: `${basePath}/icons/${def.theme}`,
          svgoConfig: base,
          template: iconTemplate,
          mapToInterpolate: ({name, inlineIcon}) => ({
            identifier: getIdentifier({name, themeSuffix: def.themeSuffix}),
            name,
            theme: def.theme,
            inlineIcon
          }),
          filename: ({name}) => getIdentifier({name, themeSuffix: def.themeSuffix})
        })
      );
    });
    return fns;
  };
