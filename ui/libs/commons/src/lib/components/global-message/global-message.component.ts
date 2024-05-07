import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, Input, NgModule, OnInit, Renderer2, ViewChild } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { isNotNullOrUndefined } from 'codelyzer/util/isNotNullOrUndefined';
import { MessageType } from './message-type';

@Component({
  selector: 'rsa-global-message',
  templateUrl: './global-message.component.html',
  styleUrls: ['./global-message.component.scss'],
})
export class GlobalMessageComponent implements OnInit, AfterViewInit {
  @Input() messageType: MessageType;
  @Input() messageTitle: string;
  @Input() message: string;
  @Input() icon: string;
  @Input() backgroundColor: string;
  @Input() borderColor: string;
  @Input() iconColor: string;
  @Input() closable = false;

  @Input() displayed = true;
  globalMessageType = MessageType;

  @ViewChild('messageBox', { static: false }) messageBox: any;
  @ViewChild('messageIcon', { static: false }) messageIcon: any;

  constructor(private renderer: Renderer2) {}

  ngOnInit(): void {
    if (!isNotNullOrUndefined(this.messageType)) {
      this.messageType = MessageType.primary;
    }
    this.iconColor = `${this.iconColor}!important`;
  }

  closeMessage(): void {
    this.displayed = false;
  }

  ngAfterViewInit() {
    if (
      (this.messageType === MessageType.primary && isNotNullOrUndefined(this.borderColor)) ||
      isNotNullOrUndefined(this.backgroundColor)
    ) {
      this.renderer.setStyle(this.messageBox.nativeElement, 'border-color', this.borderColor);
      this.renderer.setStyle(this.messageBox.nativeElement, 'background-color', this.backgroundColor);
    }
  }
}

@NgModule({
  declarations: [GlobalMessageComponent],
  imports: [MatIconModule, CommonModule, TranslateModule],
  exports: [GlobalMessageComponent],
})
export class GlobalMessageComponentModule {}
