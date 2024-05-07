import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { HgbsKitEntryDTO, HgbsKitEntryReviewDTO, QcEntryDTO, QcEntryReviewDTO, QcMonthlyReviewDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';
import { SpecialtyLabService } from './specialty-lab.service';

describe('SpecialtyLabService', () => {
  let service: SpecialtyLabService;
  let httpMock: HttpTestingController;
  let config: EnvironmentConfigService;
  const STATUS_OK = { status: 200, statusText: 'ok' };
  const STATUS_CREATED = { status: 201, statusText: 'created' };
  const STATUS_NO_CONTENT = { status: 204, statusText: 'no content' };
  const STATUS_ERROR = { status: 422, statusText: 'error' };
  const STATUS_NOT_FOUND = { status: 404, statusText: 'not found' };
  const HGBS_REVIEW_ENDPOINT = '/v1/hgbs-kit-entries';
  const QC_ENTRY_REVIEW_ENDPOINT = '/v1/qc-entries';
  const QC_MONTHLY_REVIEW_ENDPOINT = '/v1/qc-monthly-reviews';
  const criteria = {
    id: '1',
    month: '02',
    year: '2022',
    qcType: 'type',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SpecialtyLabService, ...getAppInitializerMockProvider('commons')],
    });

    service = TestBed.inject(SpecialtyLabService);
    httpMock = TestBed.inject(HttpTestingController);
    config = TestBed.inject(EnvironmentConfigService);
  });

  it('Should create HgbsKitEntry', () => {
    const entry = {};
    service.createHgbsEntry(entry as HgbsKitEntryDTO).subscribe(result => {
      expect(result).toBeDefined();
    });

    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}`).flush({}, STATUS_CREATED);
  });

  it('Should throw error when creating HgbsKitEntry', () => {
    const entry = {};
    service.createHgbsEntry(entry as HgbsKitEntryDTO).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(422);
        expect(error.statusText).toStrictEqual('error');
      }
    );
    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}`).flush({}, STATUS_ERROR);
  });

  it('Should get an hgbs entry by Id', () => {
    const entry = {
      id: 1,
    };

    service.findHgbsEntryById(1).subscribe(result => {
      expect(result).toStrictEqual(entry);
    });

    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/1`).flush(entry, STATUS_OK);
  });

  it('Should throw error when getting HgbsKitEntry by Id', () => {
    service.findHgbsEntryById(1).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(404);
        expect(error.statusText).toStrictEqual('not found');
      }
    );
    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/1`).flush({}, STATUS_NOT_FOUND);
  });

  it('Should create HgbsKitEntryReview', () => {
    const entry = {
      id: 1,
    };
    service.createHgbsEntryReview({ hgbsKitId: 1 } as HgbsKitEntryReviewDTO).subscribe(result => {
      expect(result).toStrictEqual(entry);
    });

    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/1/review`).flush(entry, STATUS_CREATED);
  });

  it('Should throw error when creating an HgbsKitEntry review', () => {
    service.createHgbsEntryReview({ hgbsKitId: 1 } as HgbsKitEntryReviewDTO).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(422);
        expect(error.statusText).toStrictEqual('error');
      }
    );
    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/1/review`).flush({}, STATUS_ERROR);
  });

  it('Should get list of HgbsEntry reviews', () => {
    const data = [
      {
        id: 121,
        kitNameKey: 'hgbs.qc.gld-sickleheme.kit.label',
        locationId: 3,
        locationName: 'Charlotte Main',
        qcType: 'HGBS_QC',
        status: 'qc-active.label',
        entryDate: '2022-11-08T10:35:33.631507-03:00',
        month: 11,
        year: 2022,
        qcInterpretation: 'Acceptable',
        workstations: [10],
      },
      {
        id: 120,
        kitNameKey: 'hgbs.qc.gld-sickleheme.kit.label',
        locationId: 7,
        locationName: 'Ft Lauderdale Main',
        qcType: 'HGBS_QC',
        status: 'qc-inactive.label',
        entryDate: '2022-11-08T10:25:19.286259-03:00',
        month: 11,
        year: 2022,
        qcInterpretation: 'Acceptable',
        workstations: [46],
      },
    ];
    const filter = {
      'entryDate.greaterThanOrEqual': '2022-11-08T00:00:00.000Z',
      'entryDate.lessThanOrEqual': '2022-11-09T00:00:00.000Z',
      'workstations.contains': '10,11',
      status: 'qc-inactive.label',
      'locationId.in': '3,5',
      qcInterpretation: 'Acceptable',
      year: '2022',
      month: 'nov',
      kitNameKey: 'kit',
    };
    service.getHgbsEntryReviewReports(filter).subscribe(results => {
      expect(results.body.length).toBe(data.length);
    });
    httpMock
      .expectOne(
        `${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/review-report?entryDate.greaterThanOrEqual=2022-11-08T00:00:00.000Z&entryDate.lessThanOrEqual=2022-11-09T00:00:00.000Z&workstations.contains=10,11&status=qc-inactive.label&locationId.in=3,5&qcInterpretation=Acceptable&year=2022&month=nov&kitNameKey=kit`
      )
      .flush(data, STATUS_OK);
  });

  it('Should get empty list of HgbsEntry reviews if none were found', () => {
    const data = [];
    const filter = {};
    service.getHgbsEntryReviewReports(filter).subscribe(results => {
      expect(results.body.length).toBe(data.length);
    });
    httpMock.expectOne(`${config.env.serverApiURL}${HGBS_REVIEW_ENDPOINT}/review-report`).flush(data, STATUS_OK);
  });

  it('Should create QcEntry', () => {
    const entry = {};
    service.createQcEntry(entry as QcEntryDTO).subscribe(result => {
      expect(result).toBeDefined();
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}`).flush({}, STATUS_CREATED);
  });

  it('Should get an qc entry by Id', () => {
    const entry = {
      id: 1,
    };

    service.findQcEntryById(1).subscribe(result => {
      expect(result).toStrictEqual(entry);
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/1`).flush(entry, STATUS_OK);
  });

  it('Should throw error when getting qc entry by Id', () => {
    service.findQcEntryById(1).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(404);
        expect(error.statusText).toStrictEqual('not found');
      }
    );
    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/1`).flush({}, STATUS_NOT_FOUND);
  });

  it('Should create a QcEntryReview', () => {
    const entry = {
      id: 1,
    };
    service.createQcEntryReview({ qcEntryId: 1 } as QcEntryReviewDTO).subscribe(result => {
      expect(result).toStrictEqual(entry);
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/1/review`).flush(entry, STATUS_CREATED);
  });

  it('Should throw error when creating a QcEntryReview review', () => {
    service.createQcEntryReview({ qcEntryId: 1 } as QcEntryReviewDTO).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(422);
        expect(error.statusText).toStrictEqual('error');
      }
    );
    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/1/review`).flush({}, STATUS_ERROR);
  });

  it('Should get list of qcEntry reviews', () => {
    const data = [
      {
        id: 121,
        kitNameKey: 'hgbs.qc.gld-sickleheme.kit.label',
        locationId: 3,
        locationName: 'Charlotte Main',
        qcType: 'HGBS_QC',
        status: 'qc-active.label',
        entryDate: '2022-11-08T10:35:33.631507-03:00',
        month: 11,
        year: 2022,
        qcInterpretation: 'Acceptable',
        workstations: [10],
      },
      {
        id: 120,
        kitNameKey: 'hgbs.qc.gld-sickleheme.kit.label',
        locationId: 7,
        locationName: 'Ft Lauderdale Main',
        qcType: 'HGBS_QC',
        status: 'qc-inactive.label',
        entryDate: '2022-11-08T10:25:19.286259-03:00',
        month: 11,
        year: 2022,
        qcInterpretation: 'Acceptable',
        workstations: [46],
      },
    ];
    const filter = {
      'entryDate.greaterThanOrEqual': '2022-11-08T00:00:00.000Z',
      'entryDate.lessThanOrEqual': '2022-11-09T00:00:00.000Z',
      'workstations.contains': '10,11',
      status: 'qc-inactive.label',
      'locationId.in': '3,5',
      antiseraLotNumber: 'lot_number',
      positiveLotNumber: 'lot_number',
      negativeLotNumber: 'lot_number',
      qcInterpretation: 'Acceptable',
      reagentId: '2',
      year: '2022',
      month: 'nov',
    };
    service.getQcEntryReviewReports(filter).subscribe(results => {
      expect(results.body.length).toBe(data.length);
    });
    httpMock
      .expectOne(
        `${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/review-report?entryDate.greaterThanOrEqual=2022-11-08T00:00:00.000Z&entryDate.lessThanOrEqual=2022-11-09T00:00:00.000Z&workstations.contains=10,11&status=qc-inactive.label&locationId.in=3,5&antiseraLotNumber=lot_number&positiveLotNumber=lot_number&negativeLotNumber=lot_number&qcInterpretation=Acceptable&reagentId=2&year=2022&month=nov`
      )
      .flush(data, STATUS_OK);
  });

  it('Should get empty list of qcEntry reviews if none were found', () => {
    const data = [];
    const filter = {};
    service.getQcEntryReviewReports(filter).subscribe(results => {
      expect(results.body.length).toBe(data.length);
    });
    httpMock.expectOne(`${config.env.serverApiURL}${QC_ENTRY_REVIEW_ENDPOINT}/review-report`).flush(data, STATUS_OK);
  });

  it('Should get a list of monthly reviews', () => {
    const result = [];

    service.getMonthlyReviews(criteria).subscribe(results => {
      expect(results.body.length).toBe(result.length);
    });

    httpMock
      .expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}?id=1&month=02&year=2022&qcType=type`)
      .flush(result, STATUS_OK);
  });

  it('Should find a monthly review by ID', () => {
    const result = {
      id: 0,
      month: 0,
      year: 0,
      locationId: 0,
      qc_type: 'string',
      review_result: 'string',
      review_employee_id: 'string',
      review_date: 'string',
      comments: 'string',
      review_date_timezone: 'string',
      createDate: 'string',
      createDateTimeZone: 'string',
      modificationDate: 'string',
      modificationDateTimeZone: 'string',
    };

    service.findQcMonthlyReviewById(1).subscribe(review => {
      expect(review).toBe(result);
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}/1`).flush(result, STATUS_OK);
  });

  it('Should create monthly review', () => {
    const entry = {};
    service.createMonthlyReview(entry as QcMonthlyReviewDTO).subscribe(result => {
      expect(result).toBeDefined();
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}`).flush({}, STATUS_CREATED);
  });

  it('Should throw error if monthly review doesnt exist', () => {
    service.findQcMonthlyReviewById(1).subscribe(
      _ => {},
      error => {
        expect(error.status).toBe(404);
        expect(error.statusText).toStrictEqual('not found');
      }
    );

    httpMock.expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}/1`).flush({}, STATUS_NOT_FOUND);
  });

  it('Should update monthly review', () => {
    const entry = {
      id: 1,
    };
    service.updateMonthlyReview(entry as QcMonthlyReviewDTO).subscribe(result => {
      expect(result).toBeDefined();
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}/1`).flush({}, STATUS_OK);
  });

  it('Should delete monthly review', () => {
    service.deleteMonthlyReview(1).subscribe(result => {
      expect(result).toBeDefined();
    });

    httpMock.expectOne(`${config.env.serverApiURL}${QC_MONTHLY_REVIEW_ENDPOINT}/1`).flush({}, STATUS_NO_CONTENT);
  });
});
