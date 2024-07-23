
module.exports = {
    preset: 'jest-preset-angular',
    coverageDirectory: '../../coverage/apps/distribution-ui',
    moduleFileExtensions: ['ts', 'js', 'html'],
    setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'],
    "moduleNameMapper": {
        "@fuse/(.*)$": "<rootDir>/src/@fuse/$1",
        '^src/(.*)$': '<rootDir>/src/$1',
        "^lodash-es$": "lodash"
    },
    transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)'],
};

