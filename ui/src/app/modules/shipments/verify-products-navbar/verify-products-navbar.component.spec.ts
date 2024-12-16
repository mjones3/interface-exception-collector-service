import { ComponentFixture, TestBed } from '@angular/core/testing';

import { model } from '@angular/core';
import { Router } from '@angular/router';
import {
    LinkLabel,
    LinkRoute,
    VerifyProductsNavbarComponent,
} from './verify-products-navbar.component';

describe('VerifyProductsNavbarComponent', () => {
    let component: VerifyProductsNavbarComponent;
    let fixture: ComponentFixture<VerifyProductsNavbarComponent>;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [VerifyProductsNavbarComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyProductsNavbarComponent);
        component = fixture.componentInstance;
        TestBed.runInInjectionContext(() => {
            component.links = model<Record<LinkLabel, LinkRoute>>({
                Verified: '/',
            });
        });

        router = TestBed.inject(Router);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
