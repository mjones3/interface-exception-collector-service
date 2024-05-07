// Custom configuration Hammerjs
import { Injectable } from '@angular/core';
import { HammerGestureConfig } from '@angular/platform-browser';

@Injectable()
export class HammerConfig extends HammerGestureConfig {
  overrides = <any>{
    // I will only use the swipe gesture so I will deactivate the others to avoid overlaps
    swipe: { enable: true },
    pinch: { enable: false },
    rotate: { enable: false },
  };
}
