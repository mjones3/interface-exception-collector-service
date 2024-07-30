module.exports = {
    extends: ['stylelint-config-standard'],
    plugins: ['stylelint-scss'],
    rules: {
        'selector-type-no-unknown': null,
        'selector-pseudo-element-no-unknown': [
            true,
            {
                ignorePseudoElements: ['ng-deep']
            },
        ],
        'at-rule-no-unknown': null,
        'scss/at-rule-no-unknown': [
            true,
            {
                "ignoreAtRules": ["tailwind"]
            }
        ],
        'no-descending-specificity': null,
        'selector-class-pattern': null,
        "no-duplicate-selectors": null,
        "block-no-empty": null,
    },
};
