import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'sanitizer' })
export class SanitizerPipe implements PipeTransform {
  transform(value: string, ...args: any[]): string {
    if (/^[a-zA-Z_]+$/.test(value)) {
      const sanitizedValue = value.replace(/_/g, ' ').toLowerCase();
      return sanitizedValue.charAt(0).toUpperCase() + sanitizedValue.slice(1);
    }
    return value;
  }
}
