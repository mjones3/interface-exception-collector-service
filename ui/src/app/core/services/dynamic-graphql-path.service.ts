import { Injectable } from '@angular/core';
import { Apollo, ApolloBase, MutationResult } from 'apollo-angular';
import { Observable } from 'rxjs';
import type { DocumentNode } from 'graphql/index';
import type { TypedDocumentNode } from '@graphql-typed-document-node/core';
import { EmptyObject } from 'apollo-angular/types';
import { ApolloQueryResult, OperationVariables, QueryOptions, MutationOptions } from '@apollo/client';

@Injectable({
    providedIn: 'root',
})
export class DynamicGraphqlPathService {

    static readonly DEFAULT_APOLLO_INSTANCE_GRAPHQL_SUFFIX: string = '/graphql';
    static readonly DEFAULT_QUERY_OPTIONS: Omit<QueryOptions, 'query'> = Object.freeze({
        fetchPolicy: 'network-only', // 'network-only' performs a network fetch by default (ignoring local cache)
    });
    static readonly DEFAULT_MUTATION_OPTIONS: Omit<MutationOptions, 'mutation'> = Object.freeze({
        fetchPolicy: 'network-only', // 'network-only' performs a network fetch by default (ignoring local cache)
    });

    constructor(private apolloProvider: Apollo) {}

    /**
     * Performs a GraphQL QUERY operation.
     *
     * @param uri suffix that is used to search for the proper service client instance
     * @param query document node definition
     * @param variables object with input values and request customization
     */
    public executeQuery<TData, TVariables = OperationVariables>(
        uri: string,
        query: DocumentNode | TypedDocumentNode<TData, TVariables>,
        variables?: TVariables
    ): Observable<ApolloQueryResult<TData>> {
        return this.getClientInstanceFor(uri)
            .query<TData, TVariables>({
                ...DynamicGraphqlPathService.DEFAULT_QUERY_OPTIONS,
                query, variables
            });
    }

    /**
     * Performs a GraphQL MUTATION operation.
     *
     * @param uri suffix that is used to search for the proper service client instance
     * @param mutation document node definition
     * @param variables object with input values and request customization
     */
    public executeMutation<TData, TVariables = EmptyObject>(
        uri: string,
        mutation: DocumentNode | TypedDocumentNode<TData, TVariables>,
        variables?: TVariables
    ): Observable<MutationResult<TData>> {
        return this.getClientInstanceFor(uri)
            .mutate<TData, TVariables>({
                ...DynamicGraphqlPathService.DEFAULT_MUTATION_OPTIONS,
                mutation, variables
            });
    }

    /**
     * Internal method that searches for the proper service Apollo instance based on GraphQL URI definition.
     *
     * @param uri
     * @private
     */
    private getClientInstanceFor(uri?: string): ApolloBase {
        return this.apolloProvider.use(uri ?? DynamicGraphqlPathService.DEFAULT_APOLLO_INSTANCE_GRAPHQL_SUFFIX);
    }

}
