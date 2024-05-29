import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
  styleUrls: ['./view-pick-list.component.scss']
})
export class ViewPickListComponent implements OnInit {

  constructor(
    private matDialogRef: MatDialogRef<ViewPickListComponent>
  ) { }

  ngOnInit(): void {
  }

}
