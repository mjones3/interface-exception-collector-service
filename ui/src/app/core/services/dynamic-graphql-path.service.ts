import { Injectable } from '@angular/core';
import { Apollo, ApolloBase } from 'apollo-angular';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class DynamicGraphqlPathService {

    constructor(private apolloProvider: Apollo) {}

    private createApolloClient(uri: string): ApolloBase {
        const clientName = uri.split("\/")?.[1] ?? 'default';
        return this.apolloProvider.use(clientName);
    }

    public executeQuery(uri: string, query: any, variables?: any): Observable<any> {
        const apollo = this.createApolloClient(uri);
        return apollo.query({
            query: query,
            variables: variables
        });
    }

    public executeMutation(uri: string, mutation: any, variables?: any): Observable<any> {
        const apollo = this.createApolloClient(uri);
        return apollo.mutate({
            mutation,
            variables
        });
    }

}
