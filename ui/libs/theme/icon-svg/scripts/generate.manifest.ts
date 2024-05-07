import * as allIconDefs from '../../src/lib/public_api';
import {promises as fsPromises} from 'fs';
import {template} from 'lodash';
import * as path from 'path';
import {IconDefinition} from "../templates/types";

function walk<T>(fn: (iconDef: IconDefinition) => Promise<T>) {
  return Promise.all(
    Object.keys(allIconDefs).map(svgIdentifier => {
      const iconDef = (allIconDefs as { [id: string]: IconDefinition })[
        svgIdentifier
        ];

      return fn(iconDef);
    })
  );
}

async function generateManifest() {
  const manifestRender = template(`
// This manifest file is generated automatically.
import { Manifest } from './types';

export const manifest: Manifest = {
  rsa: [
    <%= rsa %>
  ],
  hi_outline: [
    <%= hi_outline %>
  ],
  hi_solid: [
    <%= hi_solid %>
  ],
  dripicons: [
    <%= dripicons %>
  ]
};`);
  const manifestContent: {
    rsa: string[];
    hi_outline: string[];
    hi_solid: string[];
    dripicons: string[];
  } = {
    rsa: [],
    hi_outline: [],
    hi_solid: [],
    dripicons: [],
  };

  await walk(async ({name, theme}) => {
    if (theme) {
      manifestContent[theme].push(`'${name}'`);
    }
  });

  await fsPromises.writeFile(
    path.resolve(__dirname, `../../src/lib/manifest.ts`),
    manifestRender({
      rsa: manifestContent.rsa.join(', '),
      hi_outline: manifestContent.hi_outline.join(', '),
      hi_solid: manifestContent.hi_solid.join(', '),
      dripicons: manifestContent.dripicons.join(', '),
    })
  );
}

generateManifest();
