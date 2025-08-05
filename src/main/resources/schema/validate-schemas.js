#!/usr/bin/env node

/**
 * Schema Validation Test Script
 * Validates all event schemas and example payloads
 */

const fs = require('fs');
const path = require('path');
const Ajv = require('ajv');
const addFormats = require('ajv-formats');

const ajv = new Ajv({ allErrors: true, strict: false });
addFormats(ajv);

const schemaDir = __dirname;
const schemas = [
  'OrderRejected-Inbound.json',
  'OrderCancelled-Inbound.json', 
  'CollectionRejected-Inbound.json',
  'DistributionFailed-Inbound.json',
  'ValidationError-Inbound.json',
  'ExceptionCaptured-Outbound.json',
  'ExceptionRetryCompleted-Outbound.json',
  'ExceptionResolved-Outbound.json',
  'CriticalExceptionAlert-Outbound.json'
];

console.log('🔍 Validating Event Schemas...\n');

let allValid = true;

schemas.forEach(schemaFile => {
  const schemaPath = path.join(schemaDir, schemaFile);
  
  try {
    const schemaContent = fs.readFileSync(schemaPath, 'utf8');
    const schema = JSON.parse(schemaContent);
    
    // Validate schema structure
    const validate = ajv.compile(schema);
    
    // Test with example data if present
    if (schema.examples && schema.examples.length > 0) {
      schema.examples.forEach((example, index) => {
        const valid = validate(example);
        if (valid) {
          console.log(`✅ ${schemaFile} - Example ${index + 1}: Valid`);
        } else {
          console.log(`❌ ${schemaFile} - Example ${index + 1}: Invalid`);
          console.log(`   Errors: ${ajv.errorsText(validate.errors)}`);
          allValid = false;
        }
      });
    } else {
      console.log(`⚠️  ${schemaFile}: No examples to validate`);
    }
    
  } catch (error) {
    console.log(`❌ ${schemaFile}: Failed to parse - ${error.message}`);
    allValid = false;
  }
});

console.log(`\n${allValid ? '🎉' : '💥'} Schema validation ${allValid ? 'completed successfully' : 'failed'}`);
process.exit(allValid ? 0 : 1);
