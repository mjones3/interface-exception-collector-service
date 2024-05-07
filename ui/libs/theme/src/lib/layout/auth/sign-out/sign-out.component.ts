import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@rsa/commons';
import { interval, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'rsa-auth-sign-out',
  templateUrl: './sign-out.component.html',
  styleUrls: ['./sign-out.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class AuthSignOutComponent implements OnInit, OnDestroy {
  countdown: number;
  countdownMapping: any;

  // Private
  private _unsubscribeAll: Subject<any>;

  /**
   * Constructor
   *
   * @param {AuthService} _authService
   * @param {Router} _router
   */
  constructor(private _authService: AuthService, private _router: Router) {
    // Set the private default
    this._unsubscribeAll = new Subject();

    // Set the defaults
    this.countdown = 5;
    this.countdownMapping = {
      '=1': '# second',
      other: '# seconds',
    };
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Lifecycle hooks
  // -----------------------------------------------------------------------------------------------------

  /**
   * On init
   */
  async ngOnInit(): Promise<void> {
    // Get the duration
    const duration = this.countdown;

    // Redirect after the countdown
    interval(1000)
      .pipe(take(duration), takeUntil(this._unsubscribeAll))
      .subscribe(
        () => {
          this.countdown--;
        },
        () => {},
        () => {
          // Sign out
          this._authService.logout();
        }
      );
  }

  /**
   * On destroy
   */
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    this._unsubscribeAll.next();
    this._unsubscribeAll.complete();
  }
}
