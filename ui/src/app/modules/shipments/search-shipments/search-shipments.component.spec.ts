import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ApolloModule } from 'apollo-angular';
import { ToastrModule } from 'ngx-toastr';
import { SearchShipmentsComponent } from './search-shipments.component';

describe('SearchShipmentsComponent', () => {
    let component: SearchShipmentsComponent;
    let fixture: ComponentFixture<SearchShipmentsComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                SearchShipmentsComponent,
                ApolloModule,
                ToastrModule.forRoot(),
            ],
        });
        fixture = TestBed.createComponent(SearchShipmentsComponent);
        component = fixture.componentInstance;
    }));

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
