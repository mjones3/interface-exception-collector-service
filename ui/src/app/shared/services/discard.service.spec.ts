import { TestBed } from '@angular/core/testing';

import { ApolloModule } from 'apollo-angular';
import {
    ApolloTestingController,
    ApolloTestingModule,
} from 'apollo-angular/testing';
import { DiscardService } from './discard.service';

describe('DiscardService', () => {
    let service: DiscardService;
    let controller: ApolloTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [ApolloTestingModule, ApolloModule],
        });
        service = TestBed.inject(DiscardService);
        controller = TestBed.inject(ApolloTestingController);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
