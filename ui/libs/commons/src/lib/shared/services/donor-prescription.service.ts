import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonorDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DonorPrescriptionService {
  serverApiURL: string;
  donorInformationEndpoint: string;
  drawInformationEndpoint: string;
  donationInformationEndpoint: string;
  patientInformationEndpoint: string;
  prescriptionInformationEndpoint: string;
  productInformationEndpoint: string;
  doctorInformationEndpoint: string;
  doctorAddressEndpoint: string;
  doctorContactEndpoint: string;
  hospitalInformationEndpoint: string;
  hospitalAddressEndpoint: string;
  hospitalContactEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.serverApiURL = config.env.serverApiURL;
    this.donorInformationEndpoint = this.serverApiURL + '/v1/donors'; // /{donorId}
    this.drawInformationEndpoint = this.serverApiURL + '/v1/draws'; // /{donationId}
    this.donationInformationEndpoint = this.serverApiURL + '/v1/donations';
    this.patientInformationEndpoint = this.serverApiURL + '/v1/patients'; // /{patientId}
    this.prescriptionInformationEndpoint = this.serverApiURL + '/v1/prescriptions?donationId='; //{selectedDonationId}
    this.productInformationEndpoint = this.serverApiURL + '';
    this.doctorInformationEndpoint = this.serverApiURL + '/v1/practitioners'; // /{practitionerId}
    this.doctorAddressEndpoint = this.serverApiURL + '/v1/practitioner-addresses?practitionerId='; //{practitionerId}
    this.doctorContactEndpoint = this.serverApiURL + '/v1/practitioner-contact-points?practitionerId='; //{practitionerId}
    this.hospitalInformationEndpoint = this.serverApiURL + '/v1/hospitals'; // /{hospitalId}
    this.hospitalAddressEndpoint = this.serverApiURL + '/v1/hospital-addresses?hospitalId='; //{hospitalId}
    this.hospitalContactEndpoint = this.serverApiURL + '/v1/hospital-contact-points?hospitalId='; //{hospitalId}
  }

  public getDonorInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.donorInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDrawInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.drawInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonationInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.donationInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPatientInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.patientInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPrescriptionInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.prescriptionInformationEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getProductInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.productInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDoctorInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.doctorInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDoctorAddress(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.doctorAddressEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDoctorContact(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.doctorContactEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getHospitalInformation(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.hospitalInformationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getHospitalAddress(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.hospitalAddressEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getHospitalContact(id: number): Observable<any> {
    return this.httpClient
      .get<DonorDto>(this.hospitalContactEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
