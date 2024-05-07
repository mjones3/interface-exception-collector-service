import { Injectable } from '@angular/core';
import { AntigenBatchEntryDto, TestingMethodTranslate, Workstation } from '../models/antigen-batch.dto';

@Injectable({
  providedIn: 'root',
})
export class AntigenBatchStorageService {
  private readonly DOWNTIME_ENTRY = 'ANTIGEN-DOWN-TIME-ENTRY';
  private readonly TESTING_METHOD = 'ANTIGEN-TESTING-METHOD';
  private readonly WORKSTATIONS = 'ANTIGEN-WORKSTATIONS';
  private readonly UNIT_NUMBERS = 'ANTIGEN-UNIT-NUMBERS';
  private storage: Storage;

  constructor() {
    this.storage = window.localStorage;
  }

  set(key: string, value: any) {
    this.storage.setItem(key, JSON.stringify(value));
  }

  get(key: string): any {
    return JSON.parse(this.storage.getItem(key));
  }

  setCurrentTestingMethod(optionValue: string, description: string) {
    this.storage.setItem(this.TESTING_METHOD, JSON.stringify({ optionValue, description }));
  }

  getCurrentTestingMethod(): TestingMethodTranslate {
    return JSON.parse(this.storage.getItem(this.TESTING_METHOD));
  }

  getCurrentDowntimeEntry(): boolean {
    return JSON.parse(this.storage.getItem(this.DOWNTIME_ENTRY));
  }

  setCurrentDowntimeEntry(downtimeEntry: boolean) {
    this.storage.setItem(this.DOWNTIME_ENTRY, JSON.stringify(downtimeEntry));
  }

  getCurrentWorkstations(): Workstation[] {
    return JSON.parse(this.storage.getItem(this.WORKSTATIONS));
  }

  setCurrentWorkstations(workstations: Workstation[]) {
    this.storage.setItem(this.WORKSTATIONS, JSON.stringify(workstations));
  }

  getCurrentUnitNumbers(): AntigenBatchEntryDto[] {
    return JSON.parse(this.storage.getItem(this.UNIT_NUMBERS));
  }

  setCurrentUnitNumbers(unitNumbers: AntigenBatchEntryDto[]) {
    this.storage.setItem(this.UNIT_NUMBERS, JSON.stringify(unitNumbers));
  }
}
