// @ts-check
const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = tseslint.config(
    {
        files: ['**/*.ts'],
        extends: [
            eslint.configs.recommended,
            ...tseslint.configs.recommended,
            ...tseslint.configs.stylistic,
            ...angular.configs.tsRecommended,
        ],
        processor: angular.processInlineTemplates,
        rules: {
            '@angular-eslint/directive-selector': [
                'error',
                {
                    type: 'attribute',
                    style: 'camelCase',
                },
            ],
            '@angular-eslint/component-selector': [
                'error',
                {
                    type: 'element',
                    style: 'kebab-case',
                },
            ],
            '@typescript-eslint/no-explicit-any': 'warn',
            '@typescript-eslint/no-unused-vars': 'warn',
            '@typescript-eslint/no-empty-function': 'warn',
            '@typescript-eslint/prefer-for-of': 'off',
            '@angular-eslint/template/interactive-supports-focus': 'off',
            '@angular-eslint/template/click-events-have-key-events': 'off',
            'prefer-rest-params': 'off',
        },
    },
    {
        files: ['**/*.html'],
        extends: [
            ...angular.configs.templateRecommended,
            ...angular.configs.templateAccessibility,
        ],
        rules: {},
    },
    {
        ignores: [
            '.idea',
            '.git',
            '*.sh',
            '.*ignore',
            'package*.json',
            '.husky',
            'node_modules',
            'public',
            'build',
            'dist',
        ],
    }
);
