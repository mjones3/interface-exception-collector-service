import { Injectable } from '@angular/core';
import { BrowserPrintingModel } from '../../models/browser-printing.model';

@Injectable({
    providedIn: 'root',
})
export class BrowserPrintingService {
    /**
     * Updates the default values for input elements.
     *
     * @param {HTMLCollectionOf<HTMLInputElement>} elements - Collection of input elements.
     * @private
     */
    private updateInputDefaults(
        elements: HTMLCollectionOf<HTMLInputElement>
    ): void {
        for (let i = 0; i < elements.length; i++) {
            const element = elements[i];
            element['defaultValue'] = element.value;
            if (element['checked']) element['defaultChecked'] = true;
        }
    }

    /**
     * Updates the default values for select elements.
     *
     * @param {HTMLCollectionOf<HTMLSelectElement>} elements - Collection of select elements.
     * @private
     */
    private updateSelectDefaults(
        elements: HTMLCollectionOf<HTMLSelectElement>
    ): void {
        for (let i = 0; i < elements.length; i++) {
            const element = elements[i];
            const selectedIdx = element.selectedIndex;
            const selectedOption: HTMLOptionElement =
                element.options[selectedIdx];

            selectedOption.defaultSelected = true;
        }
    }

    /**
     * Updates the default values for textarea elements.
     *
     * @param {HTMLCollectionOf<HTMLTextAreaElement>} elements - Collection of textarea elements.
     * @private
     */
    private updateTextAreaDefaults(
        elements: HTMLCollectionOf<HTMLTextAreaElement>
    ): void {
        for (let i = 0; i < elements.length; i++) {
            const element = elements[i];
            element['defaultValue'] = element.value;
        }
    }

    /**
     * Converts a canvas element to an image and returns its HTML string.
     *
     * @param {HTMLCanvasElement} element - The canvas element to convert.
     * @returns {string} - HTML string of the image.
     * @private
     */
    private canvasToImageHtml(element: HTMLCanvasElement): string {
        const dataUrl = element.toDataURL();
        return `<img src="${dataUrl}" style="max-width: 100%;">`;
    }

    /**
     * Includes canvas contents in the print section via img tags.
     *
     * @param {HTMLCollectionOf<HTMLCanvasElement>} elements - Collection of canvas elements.
     * @private
     */
    private updateCanvasToImage(
        elements: HTMLCollectionOf<HTMLCanvasElement>
    ): void {
        for (let i = 0; i < elements.length; i++) {
            const element = this.canvasToImageHtml(elements[i]);
            elements[i].insertAdjacentHTML('afterend', element);
            elements[i].remove();
        }
    }

    /**
     * Retrieves the HTML content of a specified print section.
     *
     * @param {string} printSectionId - Id of the print section.
     * @returns {string | null} - HTML content of the print section, or null if not found.
     * @private
     */
    private getHtmlContents(printSectionId: string): string | null {
        const printContents = document.getElementById(printSectionId);
        if (!printContents) return null;

        const inputEls = printContents.getElementsByTagName('input');
        const selectEls = printContents.getElementsByTagName('select');
        const textAreaEls = printContents.getElementsByTagName('textarea');
        const canvasEls = printContents.getElementsByTagName('canvas');

        this.updateInputDefaults(inputEls);
        this.updateSelectDefaults(selectEls);
        this.updateTextAreaDefaults(textAreaEls);
        this.updateCanvasToImage(canvasEls);

        return printContents.innerHTML;
    }

    /**
     * Retrieves the HTML content of elements with the specified tag.
     *
     * @param {keyof HTMLElementTagNameMap} tag - HTML tag name.
     * @returns {string} - Concatenated outerHTML of elements with the specified tag.
     * @private
     */
    private getElementTag(tag: keyof HTMLElementTagNameMap): string {
        const html: string[] = [];
        const elements = document.getElementsByTagName(tag);
        for (let index = 0; index < elements.length; index++) {
            html.push(elements[index].outerHTML);
        }
        return html.join('\r\n');
    }
    //#endregion

    /**
     * Prints the specified content using the provided print options.
     *
     * @param {string} printSectionId - the HTML tag that is used as section to print
     * @param {BrowserPrintingModel} options - printing customization options
     */
    public print(
        printSectionId: string,
        options?: Partial<BrowserPrintingModel>
    ): void {
        const printOptions = {
            printTitle: '&nbsp;',
            useExistingCss: true,
            bodyClass: '',
            openNewTab: true,
            previewOnly: false,
            closeWindow: true,
            printDelay: 0,
            pageSize: 'auto',
            ...options,
        };

        let styles = '';
        let links = '';

        const baseTag = this.getElementTag('base');

        if (printOptions.useExistingCss) {
            styles = this.getElementTag('style');
            links = this.getElementTag('link');
        }

        // If the openNewTab option is set to true, then set the popOut option to an empty string.
        // This will cause the print dialog to open in a new tab.
        const popOut = !printOptions.openNewTab
            ? 'top=0,left=0,height=auto,width=auto'
            : '';

        const printContents = this.getHtmlContents(printSectionId);
        if (!printContents) {
            // Handle the case where the specified print section is not found.
            console.error(`Print section with id ${printSectionId} not found.`);
            return;
        }

        const popupWin = window.open(' ', '_blank', popOut);
        if (!popupWin) {
            // the popup window could not be opened.
            console.error('Could not open print window.');
            return;
        }

        popupWin.document.open();
        popupWin.document.write(`
      <!DOCTYPE html>
      <html lang="en">
        <head>
          <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
          <title>${printOptions.printTitle}</title>
          ${baseTag}
          ${styles}
          ${links}
          <style>
          body {
            /* Background and pictures fidelity for Firefox */
            print-color-adjust: exact !important;
            /* Background and pictures fidelity For Chromium-based browsers */
            -webkit-print-color-adjust: exact !important;
          }
          @page {
            size: ${printOptions.pageSize};
          }
          </style>
        </head>
        <body class="${printOptions.bodyClass}">
          ${printContents}
          <script defer>
            function triggerPrint(event) {
              window.removeEventListener('load', triggerPrint, false);
              ${printOptions.previewOnly ? '' : `setTimeout(function() { closeWindow(window.print()); }, ${printOptions.printDelay});`}
            }
            function closeWindow() {
              ${printOptions.closeWindow ? 'window.close();' : ''}
            }
            window.addEventListener('load', triggerPrint, false);
          </script>
        </body>
      </html>
    `);
        popupWin.document.close();
    }
}
