# Information Card Component

The Information Card component can be used to display a collection of information in the way of key (label) - value (description).

## Properties

### Inputs

`@Input() embedded: boolean` If set to `true` it does not display the Title section and, the card background appearance.

`@Input() labelClasses: string` CSS classes for the label.

`@Input() valueClasses: string` CSS classes for the description label. 

`@Input() title: string` Text used for the card Title.

`@Input() showColon: boolean` If set to `false` displays a colon in the label. 

`@Input() maxRows: number` Max number of rows. If max rows reached then displays columns. If descriptions length doesn't fit in 2 columns, 
                           this value will be calculated based on the descriptions array. Default value is 4

`@Input() maxCols: number` Max number of columns. Set when using horizontal layout. Default value is 3
 
`@Input() descriptions: Description[]` Information to show in the card, where key is the label and value is the description.

`@Input() layout: InformationCardLayout` Layout to use. Permitted values 'vertical' | 'horizontal'. Default value is 'vertical' 



