import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AppliedFilters } from '../models/qc-report.dto';

@Injectable({
  providedIn: 'root',
})
export class QCReportsService {
  constructor() {}

  reportUnderProcess = new BehaviorSubject<boolean>(false);

  monthAlreadyReviewed = new BehaviorSubject<boolean>(false);

  reportReviewResult = new BehaviorSubject<{ id: number; status: string }>({ id: 0, status: '' });
  reportModeResult = new BehaviorSubject<{ mode: string; filterType: string }>({ mode: '', filterType: '' });

  appliedFilters: any;

  setMonthReviewStatus() {
    this.monthAlreadyReviewed.next(true);
  }

  setFilters(filters: AppliedFilters) {
    this.appliedFilters = filters;
  }

  setReportStatus(underReview: boolean) {
    this.reportUnderProcess.next(underReview);
  }

  getReportStatus() {
    return this.reportUnderProcess;
  }

  setReportResult(id: number, status: string) {
    this.reportReviewResult.next({ id: id, status: status });
  }

  setModeResult(mode: string, filterType: string) {
    this.reportModeResult.next({ mode: mode, filterType: filterType });
  }
}
