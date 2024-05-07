import { SanitizerPipe } from './sanitizer.pipe';
import { TranslateInterpolationPipe } from './translate-interpolation.pipe';
import { ValidationPipe } from './validation.pipe';

export const COMMONS_PIPES = [ValidationPipe, TranslateInterpolationPipe, SanitizerPipe];
