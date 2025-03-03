import { TestBed } from '@angular/core/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ExternalTransferService } from './external-transfer.service';

describe('ExternalTransferService', () => {
    let service: ExternalTransferService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ApolloTestingModule],
        });
        service = TestBed.inject(ExternalTransferService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
