import { NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/core/user/user.types';
import { AuthService } from 'app/shared/services';
import { Subject } from 'rxjs';

@Component({
    selector: 'user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    exportAs: 'user',
    standalone: true,
    imports: [
        MatButtonModule,
        MatMenuModule,
        MatIconModule,
        NgClass,
        MatDividerModule,
    ],
})
export class UserComponent implements OnInit, OnDestroy {
    @Input()
    showAvatar: boolean;
  
    // Private
    private _unsubscribeAll: Subject<any>;
  
    /**
     * Constructor
     *
     * @param {ChangeDetectorRef} _changeDetectorRef
     * @param {Router} _router
     * @param {AuthService} _authService
     * @param {UserService} _userService
     */
    constructor(
      private _changeDetectorRef: ChangeDetectorRef,
      private _router: Router,
      private _authService: AuthService,
      private _userService: UserService
    ) {
      // Set the private defaults
      this._unsubscribeAll = new Subject();
  
      // Set the defaults
      this.showAvatar = true;
    }
  
    private _user: User;
  
    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------
  
    get user(): User {
      return this._user;
    }
  
    /**
     * Setter & getter for user
     *
     * @param value
     */
    @Input()
    set user(value: User) {
      // Save the user
      this._user = value;
  
      // Store the user in the service
      this._userService.user = value;
    }
  
    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------
  
    /**
     * On init
     */
    ngOnInit(): void {
      this._authService.loadUserProfile().then(profile => {
        this.user = {
          id: profile.id,
          name: profile.username,
          email: profile.email,
        };
      });
    }
  
    /**
     * On destroy
     */
    ngOnDestroy(): void {
      // Unsubscribe from all subscriptions
      this._unsubscribeAll.next(null);
      this._unsubscribeAll.complete();
    }
  
    /**
     * Sign out
     */
    async signOut(): Promise<void> {
      await this._authService.logout();
      this._router.navigate(['/sign-out']);
    }
  }
  