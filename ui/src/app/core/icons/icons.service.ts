import { inject, Injectable } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { DRIP_ICONS, HEROIC_ICONS, RSA_ICONS } from 'app/shared/icons/icons';

@Injectable({ providedIn: 'root' })
export class IconsService {
    /**
     * Constructor
     */
    constructor() {
        const domSanitizer = inject(DomSanitizer);
        const matIconRegistry = inject(MatIconRegistry);

        // Register icon sets
        matIconRegistry.addSvgIconSet(
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/material-twotone.svg'
            )
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'mat_outline',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/material-outline.svg'
            )
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'mat_solid',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/material-solid.svg'
            )
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'feather',
            domSanitizer.bypassSecurityTrustResourceUrl('icons/feather.svg')
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'heroicons_outline',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/heroicons-outline.svg'
            )
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'heroicons_solid',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/heroicons-solid.svg'
            )
        );

        matIconRegistry.addSvgIconSetInNamespace(
            'heroicons_mini',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/heroicons-mini.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'product-platelets',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-product-platelets.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'product-rbc',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-product-rbc.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'product-whole-blood',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-product-whole-blood.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'product-plasma',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-product-plasma.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'temperature-deep-freeze',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-temperature-deep-freeze.svg'
            )
        );
        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'temperature-freeze',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-temperature-freeze.svg'
            )
        );
        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'temperature-refrigerator',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-temperature-refrigerator.svg'
            )
        );
        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'temperature-room-temperature',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-temperature-room-temperature.svg'
            )
        );

        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'visual-inspection',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-visual-inspection.svg'
            )
        );


        matIconRegistry.addSvgIconInNamespace(
            'biopro',
            'cryo',
            domSanitizer.bypassSecurityTrustResourceUrl(
                'icons/biopro/biopro-cryo.svg'
            )
        );

        // Adding icons
        [...RSA_ICONS, ...DRIP_ICONS, ...HEROIC_ICONS].forEach((icon) => {
            matIconRegistry.addSvgIconLiteralInNamespace(
                icon.theme,
                icon.name,
                domSanitizer.bypassSecurityTrustHtml(icon.icon)
            );
        });
    }
}
