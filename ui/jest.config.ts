import type { Config } from 'jest';

const config: Config = {
    preset: 'jest-preset-angular',
    coverageDirectory: '../../coverage/apps/manufacturing-ui',
    moduleFileExtensions: ['ts', 'js', 'html'],
    setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'],
    transform: {
        '^.+\\.(ts|js|html)$': 'jest-preset-angular',
    },
};

export default config;
