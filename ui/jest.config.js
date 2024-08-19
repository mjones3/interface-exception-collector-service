module.exports = {
    preset: 'jest-preset-angular',
    setupFilesAfterEnv: ['<rootDir>/src/test/setup-jest.ts'],
    testEnvironment: 'jsdom',
    testPathIgnorePatterns: ['<rootDir>/node_modules/', '<rootDir>/dist/'],
    transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)'],
    transform: {
        '^.+\\.(ts|js|mjs|html|svg)$': [
            'jest-preset-angular',
            {
                tsconfig: '<rootDir>/tsconfig.spec.json',
                stringifyContentPathRegex: '\\.(html|svg)$',
            },
        ],
    },
    moduleFileExtensions: ['ts', 'js', 'html'],
    moduleNameMapper: {
        '^app/(.*)$': '<rootDir>/src/app/$1',
        '@shared': '<rootDir>/src/app/shared',
        '@testing': '<rootDir>/src/test',
        '@fuse/(.*)$': '<rootDir>/src/@fuse/$1',
        '^lodash-es$': 'lodash',
    },
};
