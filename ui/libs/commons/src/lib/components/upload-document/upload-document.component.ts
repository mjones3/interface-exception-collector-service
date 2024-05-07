import { Component, ElementRef, EventEmitter, forwardRef, Input, OnInit, Output, ViewChild } from '@angular/core';
import {
  ControlValueAccessor,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Subscription } from 'rxjs';
import { AutoUnsubscribe } from '../../decorators/auto-unsubscribe/auto-unsubscribe.decorator';
import { getDocumentExtensionIcon } from '../../shared/utils/utils';

@Component({
  selector: 'rsa-common-upload-document',
  templateUrl: './upload-document.component.html',
  styleUrls: ['./upload-document.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UploadDocumentComponent),
      multi: true,
    },
  ],
})
@AutoUnsubscribe()
export class UploadDocumentComponent implements OnInit, ControlValueAccessor {
  readonly fileNameMaxLength = 100;

  constructor(private formBuilder: FormBuilder) {}

  get documents(): FormArray {
    return this.documentsForm.get('documents') as FormArray;
  }

  @Input() uploadDocumentFiles: File[] = [];
  @Input() formatAllowed = '.doc,.docx,.xls,.xlsx,.pdf';
  @Input() showEmptyValidation = true;
  @Input() showTitlesWhenEmpty = true;
  @Input() addDocumentLabel = 'add-document.label';
  @Input() downloadLocalDocument = false;

  @Output() uploadedFilesChanged: EventEmitter<File[]> = new EventEmitter<File[]>();
  @Output() documentsFormData: EventEmitter<FormGroup> = new EventEmitter<FormGroup>();

  @ViewChild('fileUpload') fileUpload: ElementRef;

  documentsForm: FormGroup;
  documentsSubscription: Subscription;
  _onChange = (value: any) => {};

  ngOnInit(): void {
    this.initDocumentsForm();
  }

  initDocumentsForm(): void {
    this.documentsForm = this.formBuilder.group({
      documents: this.formBuilder.array([]),
    });

    this.documentsSubscription = this.documentsForm.controls['documents'].valueChanges.subscribe(value => {
      setTimeout(() => this.uploadedFilesChanged.emit(this.uploadDocumentFiles), 10);
      this.documentsFormData.emit(this.documentsForm);
      this._onChange(value);
    });
  }

  getIconType(index: number): string {
    return getDocumentExtensionIcon(this.documents.controls[index].value.format);
  }

  addNewDocument(document) {
    const group = {
      id: '',
      format: document.name.substr(document.name.lastIndexOf('.') + 1),
      documentName: new FormControl(document.name, Validators.required),
      file: document,
    };
    if (this.downloadLocalDocument) {
      group['tmpUrl'] = URL.createObjectURL(document);
    }

    const formGroup = this.formBuilder.group(group);
    formGroup.setValidators(this.validateFormGroup());

    return formGroup;
  }

  loadDocuments(event): void {
    const selectedFiles = event.target.files as FileList;
    const selectedFilesArray = Array.from(selectedFiles);
    const allWithAllowedFormats = selectedFilesArray.every(file => {
      const extension = file.name.substr(file.name.lastIndexOf('.'));
      if (extension) {
        return this.formatAllowed.includes(extension);
      }

      return false;
    });

    if (selectedFiles && allWithAllowedFormats) {
      this.uploadDocumentFiles.push(...selectedFilesArray);
      for (let i = 0; i < selectedFiles.length; i++) {
        this.documents.push(this.addNewDocument(selectedFiles[i]));
      }
    }
    this.fileUpload.nativeElement.value = '';
  }

  removeDocument(index): void {
    if (this.downloadLocalDocument) {
      // revoke the old object url to avoid using more memory than needed
      URL.revokeObjectURL(this.documents.at(index).get('tmpUrl').value);
    }
    this.documents.removeAt(index);
    this.uploadDocumentFiles.splice(index, 1);
  }

  /** Functions to use component with Control Value Accessor **/

  /**
   * Set Uploaded documents to form
   * @param obj - Uploaded Files
   */
  writeValue(obj): void {
    if (obj) {
      obj.forEach(doc => {
        const form = this.formBuilder.group({
          id: doc.id,
          format: doc.format,
          documentName: doc.documentName,
        });
        this.documents.push(form);
      });
    }
  }

  registerOnChange(fn: any): void {
    this._onChange = fn;
  }

  registerOnTouched(fn: any): void {}

  private validateFormGroup(): ValidatorFn {
    return (group: FormGroup): ValidationErrors => {
      const format = group.controls['format']?.value ?? '';
      const documentNameControl = group.controls['documentName'];
      const documentNameValue = documentNameControl?.value ?? '';

      const length = documentNameValue.endsWith(format)
        ? documentNameValue.length
        : `${documentNameValue}.${format}`.length;

      if (length > this.fileNameMaxLength) {
        documentNameControl.setErrors({ maxlength: true });
      } else if (!documentNameControl.hasError('required')) {
        documentNameControl.setErrors(null);
      }
      return;
    };
  }
}
