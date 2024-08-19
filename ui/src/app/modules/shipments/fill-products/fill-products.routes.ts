import { Routes } from "@angular/router";
import { EmptyLayoutComponent } from "app/layout/layouts/empty/empty.component";
import { FillProductsComponent } from "./fill-products.component";


export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Shipment'
        },
        children: [
            {
              path: ':id/fill-products/:productId',
              component: FillProductsComponent,
              data: {
                subTitle: 'Fill Products',
              },
            },
        ]

    },
] as Routes;