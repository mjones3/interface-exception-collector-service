import { IndividualConfig, ToastrService } from 'ngx-toastr';

class ToastrServiceMock {
  show(_message?: string, _title?: string, _override?: Partial<IndividualConfig>, type?: string) {}

  /** show successful toast */
  success(_message?: string, _title?: string, _override?: Partial<IndividualConfig>) {}

  /** show error toast */
  error(_message?: string, _title?: string, _override?: Partial<IndividualConfig>) {}

  /** show info toast */
  info(_message?: string, _title?: string, _override?: Partial<IndividualConfig>) {}

  /** show warning toast */
  warning(_message?: string, _title?: string, _override?: Partial<IndividualConfig>) {}

  /**
   * Remove all or a single toast by id
   */
  clear(_toastId?: number): void {}

  /**
   * Remove and destroy a single toast by id
   */
  remove(_toastId: number) {}

  /**
   * Determines if toast _message is already shown
   */
  findDuplicate(_message?: string, _resetOnDuplicate?: boolean, _countDuplicates?: boolean) {}
}

export const toasterMockProvider = [{ provide: ToastrService, useClass: ToastrServiceMock }];
