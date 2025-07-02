import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-start-irradiation',
  standalone: true,
  imports: [],
  templateUrl: './start-irradiation.component.html',
  styleUrl: './start-irradiation.component.scss'
})
export class StartIrradiationComponent implements OnInit{

    constructor() { }

    ngOnInit(): void {
        console.log('start irradiation');
    }

}
