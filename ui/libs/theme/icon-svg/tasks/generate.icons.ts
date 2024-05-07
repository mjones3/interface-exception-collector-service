import {dest, src} from 'gulp';
import rename from 'gulp-rename';
import SVGO from 'svgo';
import {useTemplate} from '../plugins';
// @ts-ignore
import svgo from 'gulp-svgo';
import {UseTemplatePluginOptions} from '../plugins/useTemplate';
import {ThemeType} from "../templates/types";

export interface GenerateIconsOptions extends UseTemplatePluginOptions {
  from: string[];
  toDir: string;
  svgoConfig: SVGO.Options;
  theme: ThemeType;
  filename: (option: { name: string }) => string;
}

export const generateIcons = ({
                                from,
                                toDir,
                                svgoConfig,
                                theme,
                                template,
                                mapToInterpolate,
                                filename
                              }: GenerateIconsOptions) =>
  function GenerateIcons() {
    return src(from)
      .pipe(svgo(svgoConfig))
      .pipe(useTemplate({template, mapToInterpolate}))
      .pipe(
        rename((file) => {
          if (file.basename) {
            file.basename = filename({name: file.basename});
            file.extname = '.ts';
          }
        })
      )
      .pipe(dest(toDir));
  };
