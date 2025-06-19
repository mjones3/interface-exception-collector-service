import { DatePipe } from '@angular/common';
import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideIcons } from '../../../../core/icons/icons.provider';
import { OrderWidgetsSidebarComponent } from './order-widgets-sidebar.component';

describe('OrderWidgetsSidebarComponent', () => {
    let component: OrderWidgetsSidebarComponent;
    let componentRef: ComponentRef<OrderWidgetsSidebarComponent>;
    let fixture: ComponentFixture<OrderWidgetsSidebarComponent>;
    let datePipe: DatePipe;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrderWidgetsSidebarComponent, NoopAnimationsModule],
            providers: [DatePipe, provideIcons()],
        }).compileComponents();

        fixture = TestBed.createComponent(OrderWidgetsSidebarComponent);
        component = fixture.componentInstance;
        componentRef = fixture.componentRef;

        datePipe = TestBed.inject(DatePipe);

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should render Order Cancelation Card', () => {
        // Arrange/Act
        const cancelEmployeeId = 'Employee ID';
        const cancelDate = new Date().toISOString();
        const cancelReason =
            'Lorem ipsum dolor sit amet, consectetur adipiscing ' +
            'elit. Praesent vel metus sem. Vivamus tristique interdum nisl, vel ' +
            'accumsan quam suscipit non. Mauris purus nulla, pellentesque ac turpis ' +
            'id, viverra rutrum lorem. Phasellus dolor nisi est.';
        componentRef.setInput('orderInput', {
            cancelEmployeeId: cancelEmployeeId,
            cancelDate: cancelDate,
            cancelReason: cancelReason,
        });
        fixture.detectChanges();

        // Assert
        const orderCancelInfoDescriptions =
            fixture.debugElement.nativeElement.querySelector(
                '#orderCancelInfoDescriptions'
            ) as HTMLElement;

        const canceledByValue = orderCancelInfoDescriptions.querySelector(
            '#informationDetails-Canceled-by-value'
        ) as HTMLSpanElement;
        expect(canceledByValue.textContent).toContain(
            cancelEmployeeId.toUpperCase()
        );

        const canceledDateAndTime = orderCancelInfoDescriptions.querySelector(
            '#informationDetails-Canceled-Date-and-Time-value'
        ) as HTMLSpanElement;
        expect(canceledDateAndTime.textContent).toContain(
            datePipe.transform(cancelDate, 'MM/dd/yyyy HH:mm:ss')
        );

        const cancelReasonValue = orderCancelInfoDescriptions.querySelector(
            '#orderCancelInfoReason > mat-expansion-panel > div.mat-expansion-panel-content > div.mat-expansion-panel-body > div'
        ) as HTMLDivElement;
        expect(cancelReasonValue.textContent).toContain(
            cancelReason.toUpperCase()
        );
    });

    it('should render Order modification Card', () => {
        const modifyEmployeeId = 'Employee ID';
        const modifyDate = new Date().toISOString();
        const modifyReason = 'Modification Reason';
        const displayModificationDetails = true;
        componentRef.setInput('orderInput', {
            modifyEmployeeId: modifyEmployeeId,
            modifyDate: modifyDate,
            modifyReason: modifyReason,
            displayModificationDetails: displayModificationDetails,
        });
        fixture.detectChanges();

        const orderModificationInfoDescriptions =
            fixture.debugElement.nativeElement.querySelector(
                '#orderModificationInfoDescriptions'
            ) as HTMLElement;

        const modifyByValue = orderModificationInfoDescriptions.querySelector(
            '#informationDetails-Modified-by-value'
        ) as HTMLSpanElement;
        expect(modifyByValue.textContent).toContain(
            modifyEmployeeId.toUpperCase()
        );

        const modifyDateAndTime =
            orderModificationInfoDescriptions.querySelector(
                '#informationDetails-Modified-Date-and-Time-value'
            ) as HTMLSpanElement;
        expect(modifyDateAndTime.textContent).toContain(
            datePipe.transform(modifyDate, 'MM/dd/yyyy HH:mm:ss')
        );

        const modifyReasonValue =
            orderModificationInfoDescriptions.querySelector(
                '#orderModificationReason > mat-expansion-panel > div.mat-expansion-panel-content > div.mat-expansion-panel-body > div'
            ) as HTMLDivElement;
        expect(modifyReasonValue.textContent).toContain(
            modifyReason.toUpperCase()
        );
    });

    it('should hide order modification card info when displayModificationDetails is false', () => {
        const displayModificationDetails = false;
        componentRef.setInput('orderInput', {
            displayModificationDetails: displayModificationDetails,
        });
        fixture.detectChanges();
        fixture.detectChanges();
        expect(
            fixture.debugElement.nativeElement.querySelector(
                '#modificationOrderId'
            )
        ).toBeFalsy();
    });
});
