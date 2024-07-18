import type { Config } from 'jest';

const config: Config = {
    preset: 'jest-preset-angular',
    coverageDirectory: '../../coverage/apps/distribution-ui',
    moduleFileExtensions: ['ts', 'js', 'html'],
    setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'],
    "moduleNameMapper": {
        "@fuse/(.*)": "<rootDir>/src/@fuse/",
        '^/(.*)$': '<rootDir>/scr/app/',
        "^lodash-es$": "lodash"
    },
    transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)'],
};

export default config;
