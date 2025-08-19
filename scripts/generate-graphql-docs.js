#!/usr/bin/env node

/**
 * GraphQL Schema Documentation Generator
 * 
 * This script generates comprehensive documentation from GraphQL schema introspection.
 * It creates markdown documentation for types, queries, mutations, and subscriptions.
 */

const fs = require('fs');
const path = require('path');
const { buildClientSchema, getIntrospectionQuery, printSchema } = require('graphql');

// Configuration
const CONFIG = {
    graphqlEndpoint: process.env.GRAPHQL_ENDPOINT || 'http://localhost:8080/graphql',
    outputDir: path.join(__dirname, '..', 'docs', 'generated'),
    authToken: process.env.JWT_TOKEN || null
};

/**
 * Fetch GraphQL schema introspection
 */
async function fetchIntrospection() {
    const fetch = (await import('node-fetch')).default;
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (CONFIG.authToken) {
        headers['Authorization'] = `Bearer ${CONFIG.authToken}`;
    }
    
    try {
        const response = await fetch(CONFIG.graphqlEndpoint, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                query: getIntrospectionQuery()
            })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const result = await response.json();
        
        if (result.errors) {
            throw new Error(`GraphQL errors: ${JSON.stringify(result.errors)}`);
        }
        
        return result.data;
    } catch (error) {
        console.error('Failed to fetch schema introspection:', error.message);
        
        // Fallback to local schema files if introspection fails
        console.log('Attempting to read local schema files...');
        return readLocalSchema();
    }
}

/**
 * Read local GraphQL schema files as fallback
 */
function readLocalSchema() {
    const schemaDir = path.join(__dirname, '..', 'interface-exception-collector', 'src', 'main', 'resources', 'graphql');
    
    try {
        const schemaFiles = [
            'schema.graphqls',
            'exception.graphqls',
            'inputs.graphqls',
            'scalars.graphqls',
            'subscriptions.graphqls'
        ];
        
        let combinedSchema = '';
        
        for (const file of schemaFiles) {
            const filePath = path.join(schemaDir, file);
            if (fs.existsSync(filePath)) {
                combinedSchema += fs.readFileSync(filePath, 'utf8') + '\n\n';
            }
        }
        
        if (!combinedSchema.trim()) {
            throw new Error('No schema files found');
        }
        
        // Return a mock introspection result for local schema
        return {
            __schema: {
                types: [],
                queryType: { name: 'Query' },
                mutationType: { name: 'Mutation' },
                subscriptionType: { name: 'Subscription' }
            },
            _localSchema: combinedSchema
        };
    } catch (error) {
        throw new Error(`Failed to read local schema: ${error.message}`);
    }
}

/**
 * Generate type documentation
 */
function generateTypeDoc(type) {
    let doc = `### ${type.name}\n\n`;
    
    if (type.description) {
        doc += `${type.description}\n\n`;
    }
    
    if (type.kind === 'OBJECT' || type.kind === 'INTERFACE') {
        doc += '**Fields:**\n\n';
        doc += '| Field | Type | Description |\n';
        doc += '|-------|------|-------------|\n';
        
        for (const field of type.fields || []) {
            const fieldType = getTypeString(field.type);
            const description = field.description || '';
            doc += `| ${field.name} | \`${fieldType}\` | ${description} |\n`;
        }
        doc += '\n';
    }
    
    if (type.kind === 'ENUM') {
        doc += '**Values:**\n\n';
        doc += '| Value | Description |\n';
        doc += '|-------|-------------|\n';
        
        for (const value of type.enumValues || []) {
            const description = value.description || '';
            doc += `| ${value.name} | ${description} |\n`;
        }
        doc += '\n';
    }
    
    if (type.kind === 'INPUT_OBJECT') {
        doc += '**Input Fields:**\n\n';
        doc += '| Field | Type | Required | Description |\n';
        doc += '|-------|------|----------|-------------|\n';
        
        for (const field of type.inputFields || []) {
            const fieldType = getTypeString(field.type);
            const required = field.type.kind === 'NON_NULL' ? 'Yes' : 'No';
            const description = field.description || '';
            doc += `| ${field.name} | \`${fieldType}\` | ${required} | ${description} |\n`;
        }
        doc += '\n';
    }
    
    return doc;
}

/**
 * Get type string representation
 */
function getTypeString(type) {
    if (type.kind === 'NON_NULL') {
        return `${getTypeString(type.ofType)}!`;
    }
    
    if (type.kind === 'LIST') {
        return `[${getTypeString(type.ofType)}]`;
    }
    
    return type.name;
}

/**
 * Generate query documentation
 */
function generateQueryDoc(queryType, schema) {
    let doc = '## Queries\n\n';
    
    for (const field of queryType.fields || []) {
        doc += `### ${field.name}\n\n`;
        
        if (field.description) {
            doc += `${field.description}\n\n`;
        }
        
        // Arguments
        if (field.args && field.args.length > 0) {
            doc += '**Arguments:**\n\n';
            doc += '| Argument | Type | Required | Description |\n';
            doc += '|----------|------|----------|-------------|\n';
            
            for (const arg of field.args) {
                const argType = getTypeString(arg.type);
                const required = arg.type.kind === 'NON_NULL' ? 'Yes' : 'No';
                const description = arg.description || '';
                doc += `| ${arg.name} | \`${argType}\` | ${required} | ${description} |\n`;
            }
            doc += '\n';
        }
        
        // Return type
        const returnType = getTypeString(field.type);
        doc += `**Returns:** \`${returnType}\`\n\n`;
        
        // Example usage
        doc += '**Example:**\n\n';
        doc += '```graphql\n';
        doc += generateExampleQuery(field);
        doc += '```\n\n';
        
        doc += '---\n\n';
    }
    
    return doc;
}

/**
 * Generate mutation documentation
 */
function generateMutationDoc(mutationType, schema) {
    let doc = '## Mutations\n\n';
    
    for (const field of mutationType.fields || []) {
        doc += `### ${field.name}\n\n`;
        
        if (field.description) {
            doc += `${field.description}\n\n`;
        }
        
        // Arguments
        if (field.args && field.args.length > 0) {
            doc += '**Arguments:**\n\n';
            doc += '| Argument | Type | Required | Description |\n';
            doc += '|----------|------|----------|-------------|\n';
            
            for (const arg of field.args) {
                const argType = getTypeString(arg.type);
                const required = arg.type.kind === 'NON_NULL' ? 'Yes' : 'No';
                const description = arg.description || '';
                doc += `| ${arg.name} | \`${argType}\` | ${required} | ${description} |\n`;
            }
            doc += '\n';
        }
        
        // Return type
        const returnType = getTypeString(field.type);
        doc += `**Returns:** \`${returnType}\`\n\n`;
        
        // Example usage
        doc += '**Example:**\n\n';
        doc += '```graphql\n';
        doc += generateExampleMutation(field);
        doc += '```\n\n';
        
        doc += '---\n\n';
    }
    
    return doc;
}

/**
 * Generate subscription documentation
 */
function generateSubscriptionDoc(subscriptionType, schema) {
    let doc = '## Subscriptions\n\n';
    
    for (const field of subscriptionType.fields || []) {
        doc += `### ${field.name}\n\n`;
        
        if (field.description) {
            doc += `${field.description}\n\n`;
        }
        
        // Arguments
        if (field.args && field.args.length > 0) {
            doc += '**Arguments:**\n\n';
            doc += '| Argument | Type | Required | Description |\n';
            doc += '|----------|------|----------|-------------|\n';
            
            for (const arg of field.args) {
                const argType = getTypeString(arg.type);
                const required = arg.type.kind === 'NON_NULL' ? 'Yes' : 'No';
                const description = arg.description || '';
                doc += `| ${arg.name} | \`${argType}\` | ${required} | ${description} |\n`;
            }
            doc += '\n';
        }
        
        // Return type
        const returnType = getTypeString(field.type);
        doc += `**Returns:** \`${returnType}\`\n\n`;
        
        // Example usage
        doc += '**Example:**\n\n';
        doc += '```graphql\n';
        doc += generateExampleSubscription(field);
        doc += '```\n\n';
        
        doc += '---\n\n';
    }
    
    return doc;
}

/**
 * Generate example query
 */
function generateExampleQuery(field) {
    let query = `query {\n  ${field.name}`;
    
    if (field.args && field.args.length > 0) {
        const args = field.args.map(arg => {
            const example = getExampleValue(arg.type);
            return `${arg.name}: ${example}`;
        }).join(', ');
        query += `(${args})`;
    }
    
    query += ' {\n    # Add fields here\n  }\n}';
    return query;
}

/**
 * Generate example mutation
 */
function generateExampleMutation(field) {
    let mutation = `mutation {\n  ${field.name}`;
    
    if (field.args && field.args.length > 0) {
        const args = field.args.map(arg => {
            const example = getExampleValue(arg.type);
            return `${arg.name}: ${example}`;
        }).join(', ');
        mutation += `(${args})`;
    }
    
    mutation += ' {\n    success\n    # Add other fields here\n  }\n}';
    return mutation;
}

/**
 * Generate example subscription
 */
function generateExampleSubscription(field) {
    let subscription = `subscription {\n  ${field.name}`;
    
    if (field.args && field.args.length > 0) {
        const args = field.args.map(arg => {
            const example = getExampleValue(arg.type);
            return `${arg.name}: ${example}`;
        }).join(', ');
        subscription += `(${args})`;
    }
    
    subscription += ' {\n    # Add fields here\n  }\n}';
    return subscription;
}

/**
 * Get example value for a type
 */
function getExampleValue(type) {
    if (type.kind === 'NON_NULL') {
        return getExampleValue(type.ofType);
    }
    
    if (type.kind === 'LIST') {
        return `[${getExampleValue(type.ofType)}]`;
    }
    
    switch (type.name) {
        case 'String':
            return '"example"';
        case 'Int':
            return '123';
        case 'Float':
            return '123.45';
        case 'Boolean':
            return 'true';
        case 'ID':
            return '"id123"';
        case 'DateTime':
            return '"2024-01-01T10:00:00Z"';
        default:
            return `{ /* ${type.name} fields */ }`;
    }
}

/**
 * Generate complete schema documentation
 */
async function generateDocumentation() {
    console.log('Fetching GraphQL schema...');
    
    try {
        const introspectionResult = await fetchIntrospection();
        
        // Create output directory
        if (!fs.existsSync(CONFIG.outputDir)) {
            fs.mkdirSync(CONFIG.outputDir, { recursive: true });
        }
        
        if (introspectionResult._localSchema) {
            // Handle local schema fallback
            console.log('Using local schema files...');
            
            const schemaDoc = `# GraphQL Schema Documentation\n\n` +
                             `Generated from local schema files on ${new Date().toISOString()}\n\n` +
                             `## Schema Definition\n\n` +
                             `\`\`\`graphql\n${introspectionResult._localSchema}\`\`\`\n`;
            
            fs.writeFileSync(path.join(CONFIG.outputDir, 'schema.md'), schemaDoc);
            console.log('✅ Generated schema.md from local files');
            return;
        }
        
        const schema = buildClientSchema(introspectionResult);
        const typeMap = schema.getTypeMap();
        
        // Generate main schema documentation
        let mainDoc = `# GraphQL Schema Documentation\n\n`;
        mainDoc += `Generated on ${new Date().toISOString()}\n\n`;
        mainDoc += `## Overview\n\n`;
        mainDoc += `This documentation is automatically generated from the GraphQL schema introspection.\n\n`;
        
        // Generate queries documentation
        const queryType = schema.getQueryType();
        if (queryType) {
            mainDoc += generateQueryDoc(queryType, schema);
        }
        
        // Generate mutations documentation
        const mutationType = schema.getMutationType();
        if (mutationType) {
            mainDoc += generateMutationDoc(mutationType, schema);
        }
        
        // Generate subscriptions documentation
        const subscriptionType = schema.getSubscriptionType();
        if (subscriptionType) {
            mainDoc += generateSubscriptionDoc(subscriptionType, schema);
        }
        
        // Write main documentation
        fs.writeFileSync(path.join(CONFIG.outputDir, 'schema.md'), mainDoc);
        
        // Generate types documentation
        let typesDoc = `# GraphQL Types Reference\n\n`;
        typesDoc += `Generated on ${new Date().toISOString()}\n\n`;
        
        const customTypes = Object.values(typeMap).filter(type => 
            !type.name.startsWith('__') && // Exclude introspection types
            !['String', 'Int', 'Float', 'Boolean', 'ID'].includes(type.name) // Exclude scalars
        );
        
        for (const type of customTypes) {
            typesDoc += generateTypeDoc(type);
        }
        
        fs.writeFileSync(path.join(CONFIG.outputDir, 'types.md'), typesDoc);
        
        // Generate raw schema SDL
        const schemaSDL = printSchema(schema);
        fs.writeFileSync(path.join(CONFIG.outputDir, 'schema.graphql'), schemaSDL);
        
        console.log('✅ Documentation generated successfully!');
        console.log(`📁 Output directory: ${CONFIG.outputDir}`);
        console.log('📄 Files generated:');
        console.log('   - schema.md (Main documentation)');
        console.log('   - types.md (Types reference)');
        console.log('   - schema.graphql (Raw schema SDL)');
        
    } catch (error) {
        console.error('❌ Failed to generate documentation:', error.message);
        process.exit(1);
    }
}

/**
 * Main execution
 */
if (require.main === module) {
    generateDocumentation();
}

module.exports = {
    generateDocumentation,
    CONFIG
};