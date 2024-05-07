import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LookUpDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type ethnicTypes = 'ETHNIC';
type genderTypes = 'GENDER';
type raceTypes = 'RACE';
type languageTypes = 'PREFERRED_LANGUAGE';
type plateletTypes = 'BACTERIAL_TESTING_TIME_RULE';
type positiveBactTypes =
  | 'GRAM_STAIN'
  | 'BACT_STATUS'
  | 'BACT_SAMPLE_STATUS'
  | 'BACT_TRUE_POSITIVE_INTERPRETATION'
  | 'BACT_INDETERMINATE_INTERPRETATION'
  | 'BACT_FALSE_POSITIVE_INTERPRETATION';
type researchTypes =
  | 'RESEARCH_PROJECT_STATUS'
  | 'RESEARCH_PRODUCT_FAMILIES_TO_ASSIGN'
  | 'RESEARCH_EXPIRED_PRODUCT'
  | 'RESEARCH_BLOOD_TYPE'
  | 'RESEARCH_IRRADIATION'
  | 'RESEARCH_FREQUENCY'
  | 'RESEARCH_FINAL_LABEL_TYPE'
  | 'RESEARCH_GENDER';
type irradiationTypes = 'IRRADIATION_STATUS';
type visualInspectionTypes = 'PRT_VISUAL_INSPECTION';
type orderTypes =
  | 'ORDER_STATUS'
  | 'ORDER_LABEL_STATUS'
  | 'ORDER_SHIPPING_METHOD'
  | 'ORDER_SHIPMENT_TYPE'
  | 'ORDER_DELIVERY_TYPE'
  | 'ORDER_SERVICE_FEE'
  | 'ORDER_PRODUCT_CATEGORY'
  | 'ORDER_SHIPMENT_TYPE';
type returnTypes = 'RETURNS_TRANSIT_TIME_ZONE' | 'RETURNS_INSPECTION_STATUS';
type importTypes = 'IMPORTS_BLOOD_TYPES';
type aboRhExceptionTypes = 'ABO_RH_UNIT_DISPOSITION' | 'ABO_RH_EXCEPTION_STATUS';
type rogueUnitTypes = 'ROGUE_UNIT_REPORT_CRITERIA_STATUS' | 'ROGUE_UNIT_REPORT_STATUS';
type earlyDonationTypes = 'EARLY_DONATION_REPORT_CRITERIA_STATUS' | 'EARLY_DONATION_REPORT_STATUS';
type dhqDeferredDonorTypes = 'DHQ_DEF_DONOR_REVIEW_DECISION' | 'DHQ_DEF_DONOR_STATUS';
type donationReviewReportTypes = 'DONATION_REVIEW_REPORT_CRITERIA_STATUS' | 'DONATION_REVIEW_REPORT_STATUS';
type donorCounselingStatusTypes = 'DONOR_COUNSELING_STATUS';
type testResultGroupTypes = 'TEST_RESULT_GROUP';
type eventManagementTypes =
  | 'EVENT_MANAGEMENT_EVENT_TYPE'
  | 'EVENT_MANAGEMENT_STATUS'
  | 'EVENT_MANAGEMENT_SOURCE'
  | 'EVENT_MANAGEMENT_PRODUCT_ACTION'
  | 'EVENT_MANAGEMENT_ACTION_SOURCE'
  | 'EVENT_MANAGEMENT_EMPLOYEE_TASK'
  | 'EVENT_MANAGEMENT_DONOR_ACTION';
type donorNotificationTypes =
  | 'DONOR_NOTIFICATION_STATUS'
  | 'DONOR_NOTIFICATION_THIRD_PARTY'
  | 'DONOR_NOTIFICATION_CREATION_TYPE';
type donorNoteTypes = 'DONOR_NOTE_TYPES';
type duplicateDonorReportStatusTypes = 'DUPLICATE_DONOR_REPORT_CRITERIA_STATUS';
type duplicateDonorAlgorithms = 'DUPLICATE_DONOR_ALGORITHM';
type duplicateDonorJoinedDonorInformationStatus = 'DUPLICATE_DONOR_REPORT_STATUS';
type storageLevelStatus = 'STORAGE_LEVEL_STATUS';
type arbovirusTypes = 'ARBOVIRUS_CASE_STATUS' | 'ARBOVIRUS_REVIEW_DECISION' | 'ARBOVIRUS_SOURCES';
type samplingPlanTypes =
  | 'SAMPLING_PLAN_MODE'
  | 'SAMPLING_PLAN_AUTOMATED_BLOOD_SEPARATOR'
  | 'SAMPLING_PLAN_STORAGE_SOLUTION'
  | 'SAMPLING_PLAN_PARENT_PRODUCT_DESIGNATION'
  | 'SAMPLING_PLAN_REVIEW_TYPE'
  | 'SAMPLING_PLAN_RESOLUTION'
  | 'SAMPLING_PLAN_ALLOWABLE_FAILURES'
  | 'SAMPLING_PLAN_REASON'
  | 'SAMPLING_PLAN_INTERPRETATION'
  | 'SAMPLING_PLAN_STATUS'
  | 'REQUESTED_SAMPLE_STATUS'
  | 'REQUESTED_SAMPLE_PQC_TEST_OUTCOME';

type specialTestingConfigurationTypes =
  | 'SPECIAL_TEST_CONFIGURATION_DONOR_TYPE'
  | 'SPECIAL_TEST_CONFIGURATION_ETHNICITY'
  | 'SPECIAL_TEST_CONFIGURATION_RACE';
type lookbackTypes = 'LOOKBACK_REVIEW_DECISION' | 'LOOKBACK_TEST_GROUPS' | 'LOOKBACK_STATUS';

type specialtyLabControlReport = 'SPECIALTY_LAB_CONTROL_REPORT_QC_TYPE';
type pooledAndDividevPlasmaPocesses = 'PDP_PROCESS';
type antigenEntryAndEdit = 'ANTIGEN_TESTING_STATUS' | 'ANTIGEN_TESTING_METHOD';
type hgbsTestMethod = 'SL_DISCREPANCY_REPORT_RESULT_HGBS_TEST_METHOD';
type pooledPlateletsReport = 'PP_PLATELET_COUNT_YIELD_OUTCOME' | 'PP_SAMPLE_TIME_STATUS';
type testResultDiscrepancyReport =
  | 'TEST_RESULT_DISCREPANCY_STATUS'
  | 'TEST_RESULT_DISCREPANCY_QUARANTINE_STATUS_FILTER'
  | 'TEST_RESULT_DISCREPANCY_UPDATE_STATUS'
  | 'TEST_RESULT_DISCREPANCY_QUARANTINE_STATUS_ACTION';

type countries = 'COUNTRY';
type usStates = 'US_STATE';

type LookUpType =
  | ethnicTypes
  | genderTypes
  | raceTypes
  | languageTypes
  | positiveBactTypes
  | researchTypes
  | irradiationTypes
  | visualInspectionTypes
  | orderTypes
  | returnTypes
  | importTypes
  | aboRhExceptionTypes
  | plateletTypes
  | rogueUnitTypes
  | earlyDonationTypes
  | dhqDeferredDonorTypes
  | donationReviewReportTypes
  | duplicateDonorReportStatusTypes
  | duplicateDonorAlgorithms
  | donorCounselingStatusTypes
  | donorNotificationTypes
  | donorNoteTypes
  | testResultGroupTypes
  | eventManagementTypes
  | duplicateDonorJoinedDonorInformationStatus
  | storageLevelStatus
  | arbovirusTypes
  | samplingPlanTypes
  | specialTestingConfigurationTypes
  | specialtyLabControlReport
  | lookbackTypes
  | antigenEntryAndEdit
  | hgbsTestMethod
  | pooledAndDividevPlasmaPocesses
  | pooledPlateletsReport
  | testResultDiscrepancyReport
  | countries
  | usStates;

declare var dT_: any;

@Injectable({
  providedIn: 'root',
})
export class LookUpService {
  lookUpEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.lookUpEndpoint = config.env && config.env.serverApiURL ? config.env.serverApiURL + '/v1/look-ups' : '';
  }

  public getLookUpData(type: string): Observable<HttpResponse<LookUpDto[]>> {
    return this.httpClient
      .get<LookUpDto[]>(this.lookUpEndpoint, {
        params: { type, page: '0', size: '1000', sort: 'orderNumber,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLookUpDataByTypes(types: LookUpType[]): Observable<HttpResponse<LookUpDto[]>> {
    return this.httpClient
      .get<LookUpDto[]>(this.lookUpEndpoint, {
        params: {
          'type.in': types.join(','),
          size: '1000',
          sort: 'orderNumber,ASC',
        },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
