import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { RecoveredPlasmaShippingDetailsComponent } from './recovered-plasma-shipping-details.component';

describe('RecoveredPlasmaShippingDetailsComponent', () => {
    let component: RecoveredPlasmaShippingDetailsComponent;
    let fixture: ComponentFixture<RecoveredPlasmaShippingDetailsComponent>;
    let router: Router;
    let toastr: ToastrImplService;
    let recoveredPlasmaService: RecoveredPlasmaService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RecoveredPlasmaShippingDetailsComponent,
                ApolloTestingModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                provideMockStore({}),
                { provide: ActivatedRoute, useValue: {} },
                {
                    provide: RecoveredPlasmaService,
                    useValue: {
                        getShipmentById: jest.fn(),
                    },
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({}),
                        snapshot: {
                            params: { id: 1 },
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(
            RecoveredPlasmaShippingDetailsComponent
        );
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        recoveredPlasmaService = TestBed.inject(RecoveredPlasmaService);
        toastr = TestBed.inject(ToastrImplService);
        jest.spyOn(recoveredPlasmaService, 'getShipmentById').mockReturnValue(
            of()
        );
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
