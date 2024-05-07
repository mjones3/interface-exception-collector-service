import {getTextWidth} from '@rsa/commons';
import 'jest-canvas-mock';

describe('Util', () => {

  it('should create component', () => {
    expect(getTextWidth('Sample text', '14px Arial')).toBeTruthy();
  });

});
