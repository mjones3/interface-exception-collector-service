# Database standards:

## Migration File Naming and Structure
- Create migration scripts following this pattern:
  - MANDATORY: Always use current date and next sequence number
  - yyyy: Current year in 4 digits format
  - MM: Current month of the year in 2 digits format
  - DD: Current day of the month in 2 digits format
  - XXXX: sequence number starting by 0001 per day (resets daily)
  - Pattern: V{yyyy}.{MM}.{DD}.{XXXX}__{file_name}.sql
  - Example: V2025.12.19.0001__create_tables.sql
  - Place under main.db.sql.common

## Migration File Requirements
- **NO rollback files required** - migrations should be forward-only
- **Separate DDL and DML**: Create separate migration files for structure vs data
  - DDL file: V{yyyy}.{MM}.{DD}.{XXXX}__create_{feature}_tables.sql
  - DML file: V{yyyy}.{MM}.{DD}.{XXXX+1}__insert_{feature}_data.sql
- **Sequence numbering**: Each day starts with 0001, increment for each file on same day
- **Idempotent operations**: Use IF NOT EXISTS, ON CONFLICT DO NOTHING, etc.
- **Check existing tables**: Before creating tables, verify they don't already exist in baseline or previous migrations
- **LK table sequence management**: When inserting data with explicit IDs into LK tables, update the sequence to prevent ID conflicts:
  ```sql
  -- After INSERT statements for LK tables
  SELECT setval('lk_table_name_id_seq', (SELECT MAX(id) FROM lk_table_name));
  ```

## Table Standards
- prefix [configuration] tables with **lk**
- prefix none-configuration tables with **bld**
- **ID Types**: LK tables use SMALLSERIAL, BLD tables use BIGSERIAL
- Add create_date instead of created_at to all tables
- Add modification_date instead of updated_at to all tables
- Add delete_date to only bld tables
- All LK must have an active column (boolean)
- Use 4th normalization form or higher
- Add FK, UQ and any other constrain need in the tables
- Foreign key naming: fk_{table}_{referenced_table}
- Index naming convention: idx_{table}_{column(s)}
- **All DATE columns must use TIMESTAMP WITH TIME ZONE**
