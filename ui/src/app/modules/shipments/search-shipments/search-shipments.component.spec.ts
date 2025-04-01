import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToastrModule } from 'ngx-toastr';
import { SearchShipmentsComponent } from './search-shipments.component';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('SearchShipmentsComponent', () => {
    let component: SearchShipmentsComponent;
    let fixture: ComponentFixture<SearchShipmentsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SearchShipmentsComponent,
                ApolloTestingModule,
                ToastrModule.forRoot(),
            ],
        });
        fixture = TestBed.createComponent(SearchShipmentsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
