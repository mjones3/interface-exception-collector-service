# Phase Selection Guide

This guide helps you quickly identify which phases from @dev_phases.md to execute based on your story requirements and existing codebase state.

## 🎯 Quick Decision Tree

### 1. **New Entity/Table Required?**
- **YES** → Start with **Phase 1** (Database Schema)
- **NO** → Go to question 2

### 2. **Modifying Existing Entity/Table?**
- **YES** → Start with **Update Phase 1** (Database Schema Changes)
- **NO** → Go to question 3

### 3. **New Business Logic/Use Case?**
- **YES** → **Phase 4** (Application Layer - Use Cases)
- **NO** → Go to question 4

### 4. **New API Endpoint/GraphQL Query?**
- **YES** → **Phase 5** (Adapter Layer - GraphQL API)
- **NO** → Go to question 5

### 5. **Updating Existing Functionality?**
- **Domain Logic** → **Update Phase 2** (Domain Entity Changes)
- **Use Cases** → **Update Phase 3** (Application Layer Changes)
- **API/GraphQL** → **Update Phase 4** (Adapter Layer Changes)

---

## 📋 Common Story Patterns

### **Pattern 1: Brand New Feature**
**Example**: *"As a user, I want to manage sample inventory"*
- **Phases**: 1 → 2 → 3 → 4 → 5 → 6 → 7
- **Reason**: New domain, new tables, new everything

### **Pattern 2: New Functionality on Existing Entity**
**Example**: *"As a user, I want to view batch history with filters"*
- **Existing**: Batch entity, basic CRUD
- **Missing**: History logic, filtering
- **Phases**: 4 → 5 → 7
- **Reason**: Entity exists, need new use cases and endpoints

### **Pattern 3: Extend Existing Entity**
**Example**: *"Add expiration_date field to antigen table"*
- **Phases**: Update Phase 1 → Update Phase 2 → Update Phase 3 → Update Phase 4 → Update Phase 5
- **Reason**: Modify existing entity across all layers

### **Pattern 4: New Business Rules**
**Example**: *"Add validation: batch cannot exceed 25 units"*
- **Phases**: Update Phase 2 → Update Phase 3 → Update Phase 5
- **Reason**: Domain logic change, use case update, test update

### **Pattern 5: New API Endpoint Only**
**Example**: *"Add GraphQL query to get workstation by location"*
- **Existing**: Workstation entity, repository, use case
- **Missing**: GraphQL endpoint
- **Phases**: 5 → 7
- **Reason**: Infrastructure exists, just need API layer

### **Pattern 6: Performance/Technical Improvement**
**Example**: *"Add caching to antigen lookup queries"*
- **Phases**: 6 → 7
- **Reason**: Configuration and testing changes only

---

## 🔍 Story Analysis Questions

### **Before Starting, Ask Yourself:**

1. **What entities does this story involve?**
    - New entities → Phase 1, 2, 3
    - Existing entities → Update phases or skip to Phase 4/5

2. **What's the primary user action?**
    - Create/Update data → Need command use cases (Phase 4)
    - View/Search data → Need query use cases (Phase 4)
    - Both → Need both command and query use cases

3. **What already exists in the codebase?**
    - Database tables → Skip Phase 1
    - Domain entities → Skip Phase 2
    - Repositories → Skip Phase 3
    - Use cases → Skip Phase 4
    - GraphQL endpoints → Skip Phase 5

4. **What's missing?**
    - Focus phases on missing components only

---

## 🚀 Quick Start Commands

### **For New Stories:**
```
"Based on this story: [story description], which phases should I execute from @dev_phases.md?"
```

### **For Updates:**
```
"Update [entity_name] to [change description], execute Update Phase [X] from @dev_phases.md"
```

### **For Analysis:**
```
"Analyze this story and recommend phases: [story description]"
```

---

## 💡 Pro Tips

- **Always end with Phase 7** (Testing) unless it's a pure configuration change
- **Phase 6** (Configuration) is only needed for new services or complex wiring
- **Update phases cascade** - if you update database (Update Phase 1), you likely need Update Phase 2, 3, 4
- **Skip phases liberally** - only execute what you actually need to change
- **Combine phases** - you can execute multiple phases in one request: "Execute Phase 4 and 5"

---

## 🎯 Example Usage

**Story**: *"As a lab technician, I want to cancel a batch that's in progress"*

**Analysis**:
- Entity: Batch (exists)
- Action: Update batch status (new business logic)
- API: New mutation needed

**Recommended Phases**: Update Phase 2 → Phase 4 → Phase 5 → Phase 7

**Command**:
```
"Add cancel batch functionality - execute Update Phase 2, Phase 4, Phase 5, and Phase 7 from @dev_phases.md"
```
