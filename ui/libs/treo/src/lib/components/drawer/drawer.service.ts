import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class TreoDrawerService
{
    // Private
    private _componentRegistry: Map<string, any>;

    /**
     * Constructor
     */
    constructor()
    {
        // Set the defaults
        this._componentRegistry = new Map<string, any>();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Register drawer component
     *
     * @param name
     * @param component
     */
    registerComponent(name: string, component: any): void
    {
        this._componentRegistry.set(name, component);
    }

    /**
     * Deregister drawer component
     *
     * @param name
     */
    deregisterComponent(name: string): void
    {
        this._componentRegistry.delete(name);
    }

    /**
     * Get drawer component from the registry
     *
     * @param name
     */
    getComponent(name: string): any
    {
        return this._componentRegistry.get(name);
    }
}
