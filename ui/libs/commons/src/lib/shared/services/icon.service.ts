import { Injectable } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { IconDefinition } from '../models/icon-types.model';
import { NameSpaceIsNotSpecifyError } from '../utils/icon.error';

@Injectable({
  providedIn: 'root',
})
export class IconService {
  constructor(private domSanitizer: DomSanitizer, private matIconRegistry: MatIconRegistry) {}

  /**
   * Add icon definition array
   * @param icons Icons
   */
  addIcon(...icons: IconDefinition[]) {
    icons.forEach(icon => {
      this.matIconRegistry.addSvgIconLiteralInNamespace(
        icon.theme,
        icon.name,
        this.domSanitizer.bypassSecurityTrustHtml(icon.icon)
      );
    });
  }

  /**
   * Register an icon. Namespace is required.
   * @param type
   * @param literal
   */
  addIconLiteral(type: string, literal: string): void {
    if (!type) {
      throw NameSpaceIsNotSpecifyError();
    }
    this.addIcon({ name: type, icon: literal });
  }

  /**
   * Add icon set
   * @param setUrl set url
   */
  addIconSet(setUrl: string): void {
    this.matIconRegistry.addSvgIconSet(this.domSanitizer.bypassSecurityTrustResourceUrl(setUrl));
  }

  addIconSetInNamespace(namespace: string, url: string): void {
    this.matIconRegistry.addSvgIconSetInNamespace(namespace, this.domSanitizer.bypassSecurityTrustResourceUrl(url));
  }
}
