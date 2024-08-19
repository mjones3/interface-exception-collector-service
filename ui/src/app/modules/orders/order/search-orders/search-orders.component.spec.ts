import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { ApolloModule } from 'apollo-angular';
import { ToastrModule } from 'ngx-toastr';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
    let component: SearchOrdersComponent;
    let fixture: ComponentFixture<SearchOrdersComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                SearchOrdersComponent,
                ApolloModule,
                ToastrModule.forRoot(),
                MatIconTestingModule,
            ],
            providers: [provideHttpClient()],
        });
        fixture = TestBed.createComponent(SearchOrdersComponent);
        component = fixture.componentInstance;
    }));

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
