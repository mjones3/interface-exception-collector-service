import { inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { MenuService } from './shared/services';

export const initialDataResolver = () => {
    const menuService = inject(MenuService);

    // Fork join multiple API endpoint calls to wait all of them to finish
    return forkJoin([menuService.get()]);
};
