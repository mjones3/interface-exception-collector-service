import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransferReceiptComponent } from './transfer-receipt.component';
import { provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { ToastrModule } from 'ngx-toastr';
import { MatNativeDateModule } from '@angular/material/core';

describe('TransferReceiptComponent', () => {
  let component: TransferReceiptComponent;
  let fixture: ComponentFixture<TransferReceiptComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ 
        TransferReceiptComponent,
        ApolloTestingModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        MatNativeDateModule,
        ToastrModule.forRoot()
      ],
      providers: [
        provideMockStore({
          initialState: {
              auth: {
                  id: 'testEmployeeId'
              }
          }
      }),
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransferReceiptComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
