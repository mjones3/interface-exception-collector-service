import { Routes } from '@angular/router';
import {EmptyLayoutComponent} from "../../layout/layouts/empty/empty.component";
import {irradiationResolver} from "./irradiation.resolver";
import {StartIrradiationComponent} from "./components/start-irradiation/start-irradiation.component";
import {CloseIrradiationComponent} from "./components/close-irradiation/close-irradiation.component";

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: { title: 'Irradiation' },
        resolve: { initialData: irradiationResolver },
        children: [
            {
                path: 'start-irradiation',
                component: StartIrradiationComponent,
                data: {
                    subTitle: 'Start Irradiation',
                },
            },
            {
                path: 'close-irradiation',
                component: CloseIrradiationComponent,
                data: {
                    subTitle: 'Close Irradiation',
                },
            }
        ],
    }
] as Routes;
