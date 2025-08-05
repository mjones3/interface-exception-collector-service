import { EventEmitter } from '@angular/core';
import { defaultPageSize, Pageable } from '../models/pageable.model';

export const getPagination = paginationInfo => {
  return {page: paginationInfo.pageIndex, size: paginationInfo.pageSize};
};

export class Paginator {

  private _currentPage: number;
  private _totalItems: number;
  private _size: number;

  pageChange: EventEmitter<Pageable> = new EventEmitter<Pageable>();

  /**
   * @param pageable Pageable object.
   */
  constructor(pageable?: Pageable) {
    this._size = pageable?.size || defaultPageSize;
    this._currentPage = pageable?.page || 0;
    this._totalItems = pageable?.totalItems || 0;
  }


  /**
   * Returns the current page number.
   */
  public getCurrentPage(): number {
    return this._currentPage;
  }

  /**
   * Sets the current page number.
   */
  private setCurrentPage(page: number) {
    const maxPage = Math.ceil(this._totalItems / this._size);
    if (page < maxPage && 0 <= page) {
      this._currentPage = page;
      this.pageChange.emit({
        page: this._currentPage,
        size: this._size,
        totalItems: this._totalItems
      });
    }
  }

  /**
   * Returns true if current page is first page
   */
  isFirstPage(): boolean {
    return this.currentPage === 0;
  }

  /**
   * Returns true if current page is last page
   */
  isLastPage(): boolean {
    return this.getLastPage() === this.currentPage;
  }

  /**
   * Returns the last page number
   */
  getLastPage(): number {
    return Math.ceil(this.totalItems / this.size);
  }

  /**
   * Go to the next page
   */
  next() {
    const maxPage = Math.ceil(this._totalItems / this._size);
    if (this.currentPage + 1 < maxPage) {
      this.setCurrentPage(this.currentPage + 1);
    }
  }

  /**
   * Go to the previous page
   */
  previous() {
    if (this.currentPage > 0) {
      this.setCurrentPage(this.currentPage - 1);
    }
  }

  get totalItems(): number {
    return this._totalItems;
  }

  set totalItems(value: number) {
    this._currentPage = 0;
    this._totalItems = value;
  }

  get size(): number {
    return this._size;
  }

  set size(value: number) {
    this._currentPage = 0;
    this._size = value;
  }

  get currentPage(): number {
    return this._currentPage;
  }

  set currentPage(value: number) {
    this._currentPage = value;
  }
}
