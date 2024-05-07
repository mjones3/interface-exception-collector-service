import template from 'lodash.template';
import {getThemeByPath} from "../../utils";
import {createTransformStream} from '../creator';

export interface UseTemplatePluginOptions {
  template: string;
  mapToInterpolate: MapToInterpolate;
}

export type MapToInterpolate = (meta: { name: string; theme: string, inlineIcon: string; path?: string }) => object;


export const useTemplate = ({
                              template: tplContent,
                              mapToInterpolate
                            }: UseTemplatePluginOptions) => {
  const executor = template(tplContent);
  return createTransformStream((content, {stem: name, theme, path}) => {
      if (!theme) {
        theme = getThemeByPath(path);
        path = `./icons/${theme}/${name}`;
      }
      const data = {name, theme, inlineIcon: content, path};
      return executor(mapToInterpolate(data));
    }
  );
};
