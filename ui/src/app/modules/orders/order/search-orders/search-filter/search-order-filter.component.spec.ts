import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { Apollo } from 'apollo-angular';
import { toasterMockProvider } from '@testing';
import { ActivatedRoute } from '@angular/router';
import { SearchOrderFilterComponent } from './search-order-filter.component';
import { SelectOptionDto } from '../../../../shared/models';

const SINGLE_SEARCH_FILTER_KEYS: string[] = ['bloodCenterId', 'serialNumber'];

const deviceCategories: SelectOptionDto[] = [
    {
        "optionKey": "PRIMARY_STORAGE",
        "optionDescription": "Primary Storage",
    },
    {
        "optionKey": "PRODUCT_SCALE",
        "optionDescription": "Product Scale",
    },
    {
        "optionKey": "SECONDARY_STORAGE",
        "optionDescription": "Secondary Storage",
    },
    {
        "optionKey": "STERILE_CONNECTION",
        "optionDescription": "Sterile Connection",
    },
    {
        "optionKey": "TEMPERATURE",
        "optionDescription": "Temperature",
    },
    {
        "optionKey": "TESTING",
        "optionDescription": "Testing",
    }
];
const locationTypes: SelectOptionDto[] = [
    {
        "optionKey": "COLLECTIONS_FIXED_SITE_AND_MOBILES",
        "optionDescription": "Collections Fixed Site (Fixed and Mobiles)",
        "optionParentKey":	"2FSM",
    },
    {
        "optionKey": "COLLECTIONS_FIXED_SITE",
        "optionDescription": "Collections Fixed Site (Fixed Site)",
        "optionParentKey":	"1FS",
    },
    {
        "optionKey": "COLLECTIONS_FIXED_SITE_MDL_HUB_2",
        "optionDescription": "Collections Fixed Site (MDL Hub 2)",
        "optionParentKey":	"234567891",
    },
    {
        "optionKey": "COLLECTIONS_MOBILE_STAGING_SITE_AND_MOBILES",
        "optionDescription": "Collections Mobile Staging (Fixed and Mobiles)",
        "optionParentKey":	"2FSM",
    },
    {
        "optionKey": "COLLECTIONS_MOBILES_ONLY",
        "optionDescription": "Collections Mobile Staging (Mobiles Only)",
        "optionParentKey":	"MSA1",
    },
    {
        "optionKey": "DISTRIBUTION_MDL_HUB_1",
        "optionDescription": "Distribution (MDL Hub 1)",
        "optionParentKey":	"123456789",
    },
    {
        "optionKey": "DISTRIBUTION_MDL_HUB_2",
        "optionDescription": "Distribution (MDL Hub 2)",
        "optionParentKey":	"234567891",
    },
    {
        "optionKey": "DISTRIBUTION_DISTRIBUTION_LABELING",
        "optionDescription": "Distribution (Distribution and Labeling)",
        "optionParentKey":	"DL1",
    },
    {
        "optionKey": "DISTRIBUTION_DISTRIBUTION_ONLY",
        "optionDescription": "Distribution (Distribution Only)",
        "optionParentKey":	"DO1",
    },
    {
        "optionKey": "LABELING_MDL_HUB_2",
        "optionDescription": "Labeling (MDL Hub 2)",
        "optionParentKey":	"234567891",
    },
    {
        "optionKey": "LABELING_MDL_HUB_1",
        "optionDescription": "Labeling (MDL Hub 1)",
        "optionParentKey":	"123456789",
    },
    {
        "optionKey": "LABELING_DISTRIBUTION_LABELING",
        "optionDescription": "Labeling (Distribution and Labeling)",
        "optionParentKey":	"DL1",
    },
    {
        "optionKey": "MANUFACTURING_MDL_HUB_2",
        "optionDescription": "Manufacturing (MDL Hub 2)",
        "optionParentKey":	"234567891",
    },
    {
        "optionKey": "MANUFACTURING_MDL_HUB_1",
        "optionDescription": "Manufacturing (MDL Hub 1)",
        "optionParentKey":	"123456789",
    },
    {
        "optionKey": "REGION_REGION_2",
        "optionDescription": "Region (Region 2)",
        "optionParentKey":	"R2",
    },
    {
        "optionKey": "REGION_REGION_1",
        "optionDescription": "Region (Region 1)",
        "optionParentKey":	"R1",
    }
];
const deviceTypes: SelectOptionDto[] = [
    {
        "optionKey": "BIN",
        "optionParentKey": "SECONDARY_STORAGE",
        "optionDescription": "Bin",
    },
    {
        "optionKey": "FREEZER",
        "optionParentKey": "PRIMARY_STORAGE",
        "optionDescription": "Freezer",
    },
    {
        "optionKey": "PQC",
        "optionParentKey": "TESTING",
        "optionDescription": "PQC",
    },
    {
        "optionKey": "PRODUCT_SCALE_DEVICE",
        "optionParentKey": "PRODUCT_SCALE",
        "optionDescription": "Product Scale",
    },
    {
        "optionKey": "RACK",
        "optionParentKey": "SECONDARY_STORAGE",
        "optionDescription": "Rack",
    },
    {
        "optionKey": "REFRIGERATOR",
        "optionParentKey": "PRIMARY_STORAGE",
        "optionDescription": "Refrigerator",
    },
    {
        "optionKey": "ROOM_TEMPERATURE",
        "optionParentKey": "PRIMARY_STORAGE",
        "optionDescription": "Room Temperature",
    },
    {
        "optionKey": "SHELF",
        "optionParentKey": "SECONDARY_STORAGE",
        "optionDescription": "Shelf",
    },
    {
        "optionKey": "STERILE_CONNECTION_DEVICE",
        "optionParentKey": "STERILE_CONNECTION",
        "optionDescription": "Sterile Connection Device",
    },
    {
        "optionKey": "THERMOMETER",
        "optionParentKey": "TEMPERATURE",
        "optionDescription": "Thermometer",
    },
    {
        "optionKey": "TRAY",
        "optionParentKey": "SECONDARY_STORAGE",
        "optionDescription": "Tray",
    },
    {
        "optionKey": "TREE",
        "optionParentKey": "SECONDARY_STORAGE",
        "optionDescription": "Tree",
    }
];
const deviceUses: SelectOptionDto[] = [
    {
        "optionKey": "FILTRATION_REFRIGERATOR",
        "optionParentKey": "REFRIGERATOR",
        "optionDescription": "Filtration Refrigerator",
    },
    {
        "optionKey": "FILTRATION_ROOM_TEMPERATURE",
        "optionParentKey": "ROOM_TEMPERATURE",
        "optionDescription": "Filtration Room Temperature",
    },
    {
        "optionKey": "INITIAL_FREEZER",
        "optionParentKey": "FREEZER",
        "optionDescription": "Initial Freezer",
    },
    {
        "optionKey": "FREEZER",
        "optionParentKey": "FREEZER",
        "optionDescription": "Storage Freezer",
    },
    {
        "optionKey": "REFRIGERATOR",
        "optionParentKey": "REFRIGERATOR",
        "optionDescription": "Storage Refrigerator",
    },
    {
        "optionKey": "ROOM_TEMPERATURE",
        "optionParentKey": "ROOM_TEMPERATURE",
        "optionDescription": "Storage Room Temperature",
    }
];
const locations: SelectOptionDto[] = [
    {
        "optionKey": "DL1",
        "optionDescription": "Distribution and Labeling",
    },
    {
        "optionKey": "DO1",
        "optionDescription": "Distribution Only",
    },
    {
        "optionKey": "2FSM",
        "optionDescription": "Fixed and Mobiles",
    },
    {
        "optionKey": "1FS",
        "optionDescription": "Fixed Site",
    },
    {
        "optionKey": "123456789",
        "optionDescription": "MDL Hub 1",
    },
    {
        "optionKey": "234567891",
        "optionDescription": "MDL Hub 2",
    },
    {
        "optionKey": "MSA1",
        "optionDescription": "Mobiles Only",
    },
    {
        "optionKey": "R1",
        "optionDescription": "Region 1",
    },
    {
        "optionKey": "R2",
        "optionDescription": "Region 2",
    }
];


describe('SearchDeviceFilterComponent', () => {
    let component: SearchOrderFilterComponent;
    let fixture: ComponentFixture<SearchOrderFilterComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SearchOrderFilterComponent,
                NoopAnimationsModule
            ],
            providers: [
                provideHttpClient(),
                Apollo,
                ...toasterMockProvider,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            data: {
                                searchData: {
                                    locations: locations,
                                    deviceCategories: deviceCategories,
                                    deviceTypes: deviceTypes,
                                    deviceUses: deviceUses,
                                    locationTypes: locationTypes,
                                }
                            }
                        },
                    },
                }
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchOrderFilterComponent);
        component = fixture.componentInstance;
        component.ngOnInit();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should clear form when reset is triggered', () => {
        Object.keys(component.searchForm.controls).forEach(filterKey => {
            component.searchForm.controls[filterKey].setValue('Test');
            expect(component.enableSubmit).toBeTruthy();

            component.resetFilters();

            expect(component.searchForm.controls[filterKey].value).toBe(null);
            expect(component.enableSubmit).toBeFalsy();
        });

    });

    it('should disable apply button', () => {
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should enable apply button when blood center Id is entered', () => {
        component.searchForm.controls['bloodCenterId'].setValue("Test");

        expect(component.enableSubmit).toBeTruthy();
    });

    it('should keep only single search field enabled', () => {
        SINGLE_SEARCH_FILTER_KEYS.forEach(singleFilter => {
            component.resetFilters();
            component.searchForm.controls[singleFilter].setValue("Test");
            component.onChangeSingleSearchFilter(singleFilter)

            expect(component.enableSubmit).toBeTruthy();
            Object.keys(component.searchForm.controls).forEach(filterKey => {
                expect(component.searchForm.controls[filterKey].enabled).toBe(filterKey === singleFilter);
            });
        });
    });

    it('should show filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(true);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, true);
    });

    it('should hide filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(false);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, false);
    });

    it('should apply filters', () => {
        const applySearchFiltersEvent = jest.spyOn(component.applySearchFilters, 'emit');
        component.searchForm.controls['deviceCategories'].setValue(['select-all', 'PRIMARY_STORAGE']);
        component.searchForm.controls['deviceTypes'].setValue(['select-all', 'BIN','SHELF']);
        const expectedValue = {"bloodCenterId": "", "deviceCategories": ["PRIMARY_STORAGE"], "deviceStatuses": "", "deviceTypes": ["BIN", "SHELF"], "deviceUses": "", "locationTypes": "", "locations": "", "names": "", "serialNumber": ""};

        component.applyFilterSearch();

        expect(applySearchFiltersEvent).toHaveBeenNthCalledWith(1, expectedValue);
        expect(component.appliedFiltersCounter).toEqual(2);
    });

    it('should display correct location type description', () => {
        locationTypes.forEach(type => {
           const locationName = locations.find(location => location.optionKey === type.optionParentKey).optionDescription;
           expect(type.optionDescription).toContain(locationName);
        });
    });

    it('should return the keys of the select option', () => {
        const locationTypeKeys = locationTypes.map(type => type.optionKey);
        expect(component.selectOptionKeys(locationTypes)).toEqual(locationTypeKeys);
    });

    it('should filter select list by description', () => {
        const filterBy = 'MdL';
        const expectedLocations = [locations[4], locations[5]];
        expect(component.search(locations, filterBy)).toEqual(expectedLocations);
    });

    it('should show all when the filter is empty', () => {
        expect(component.search(locations, '')).toEqual(locations);
    });

    it('should show All when the selected option is all', () => {
        component.searchForm.controls['deviceCategories'].setValue(['select-all', 'PRIMARY_STORAGE']);
        component.searchForm.controls['deviceTypes'].setValue(['select-all', 'BIN','SHELF']);

        expect(component.firstSelectedValue('deviceCategories', deviceCategories)).toEqual('All');
        expect(component.firstSelectedValue('deviceTypes', deviceTypes)).toEqual('All');
    });

    it('should show selected values separated by comma when All is not selected', () => {
        component.searchForm.controls['deviceCategories'].setValue(['PRIMARY_STORAGE']);
        component.searchForm.controls['deviceTypes'].setValue(['BIN','SHELF']);

        const expetedCategory = deviceCategories.find(category => category.optionKey === 'PRIMARY_STORAGE').optionDescription;
        const expetedType = deviceTypes.filter(type => ['BIN','SHELF'].includes(type.optionKey)).map(type => type.optionDescription).toString();

        expect(component.firstSelectedValue('deviceCategories', deviceCategories)).toEqual(expetedCategory);
        expect(component.firstSelectedValue('deviceTypes', deviceTypes)).toEqual(expetedType);
    });
});
