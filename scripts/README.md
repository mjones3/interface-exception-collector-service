# Event Generation Scripts

This directory contains scripts to generate test events for the Interface Exception Collector Service with realistic blood banking domain data.

## 🩸 Blood Banking Domain Data

The scripts generate realistic `OrderRejected` events with:

- **Blood Products**: Red Blood Cells, Platelets, Fresh Frozen Plasma, Cryoprecipitate, Whole Blood
- **Hospital Locations**: Major medical centers across the US
- **Customer IDs**: Healthcare institutions and blood banks
- **Rejection Reasons**: Common blood banking rejection scenarios
- **Operations**: Blood order processing operations

## 📁 Available Scripts

### 1. Environment Setup (First Time)
**File**: `setup_python_env.sh`

Sets up Python virtual environment and dependencies.

```bash
# First time setup
./setup_python_env.sh

# Force recreation of environment
./setup_python_env.sh --force
```

### 2. Event Generator (Recommended)
**File**: `send_test_events.sh`

Main script that uses virtual environment for reliable message delivery.

```bash
# Run the event generator
./send_test_events.sh
```

**Features**:
- ✅ Automatic virtual environment activation
- ✅ Dependency validation
- ✅ Kafka connectivity check
- ✅ Reliable message delivery with retries
- ✅ Detailed progress output with colors
- ✅ Proper error handling

### 3. Environment Testing
**File**: `test_environment.py`

Validates the Python environment setup.

```bash
# Test the environment (run after setup)
source .venv/bin/activate
python test_environment.py
```

### 4. Direct Python Script
**File**: `send_order_rejected_events.py`

Direct Python script for advanced users.

```bash
# Activate virtual environment first
source .venv/bin/activate

# Run the script
python send_order_rejected_events.py
```

### 5. Curl Script (Alternative)
**File**: `send_events_curl.sh`

Uses curl and Kafka REST Proxy (requires additional setup).

```bash
# Requires Kafka REST Proxy running on port 8082
./send_events_curl.sh
```

## 🚀 Quick Start

1. **Ensure Kafka is running**:
   ```bash
   docker-compose up -d kafka
   ```

2. **Setup Python environment** (first time only):
   ```bash
   cd scripts
   ./setup_python_env.sh
   ```

3. **Test the environment** (optional):
   ```bash
   source .venv/bin/activate
   python test_environment.py
   deactivate
   ```

4. **Run the event generator**:
   ```bash
   ./send_test_events.sh
   ```

5. **Verify events were processed**:
   ```bash
   # Check application logs
   tail -f /tmp/spring-boot.log
   
   # Query the API
   curl -H "Authorization: Bearer <your-jwt-token>" \
        "http://localhost:8080/api/v1/exceptions"
   ```

## 📊 Generated Event Structure

Each `OrderRejected` event contains:

```json
{
  "eventId": "uuid",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-08T20:30:00.000Z",
  "source": "order-service",
  "correlationId": "uuid",
  "causationId": "uuid",
  "payload": {
    "transactionId": "TXN-123456",
    "externalId": "EXT-ORD-12345",
    "operation": "CREATE_BLOOD_ORDER",
    "rejectedReason": "Insufficient inventory for blood type O-negative",
    "customerId": "CUST-MOUNT-SINAI-001",
    "locationCode": "HOSP-NYC-001",
    "orderItems": [
      {
        "itemId": "RBC-O-NEG-001",
        "itemType": "Red Blood Cells",
        "quantity": 2,
        "unitPrice": 245.50
      }
    ]
  }
}
```

## 🔧 Configuration

### Kafka Settings
- **Bootstrap Servers**: `localhost:29092`
- **Topic**: `OrderRejected`
- **Partitions**: 3
- **Replication Factor**: 1

### Event Generation
- **Number of Events**: 10
- **Delay Between Events**: 0.5 seconds
- **Message Key**: Transaction ID (for partitioning)

## 🩺 Blood Banking Domain Details

### Blood Products
- **RBC**: Red Blood Cells ($245.50/unit)
- **PLT**: Platelets ($520.75/unit)
- **FFP**: Fresh Frozen Plasma ($185.25/unit)
- **CRYO**: Cryoprecipitate ($165.00/unit)
- **WB**: Whole Blood ($425.00/unit)

### Common Rejection Reasons
- Insufficient inventory for specific blood types
- Product expiration issues
- Cross-match incompatibility
- Invalid customer credentials
- Order quantity limits exceeded
- Product recalls
- Temperature control breaches
- Duplicate order detection

### Hospital Locations
- Major medical centers in NYC, LA, Chicago, Houston, Phoenix, Philadelphia, San Antonio, San Diego

## 🔧 Virtual Environment Management

### Manual Virtual Environment Operations
```bash
# Activate virtual environment
source .venv/bin/activate

# Check installed packages
pip list

# Install additional packages
pip install package-name

# Deactivate virtual environment
deactivate

# Remove virtual environment (if needed)
rm -rf .venv
```

### Helper Scripts
```bash
# Quick activation (opens new shell with activated environment)
./activate_env.sh

# Environment validation
source .venv/bin/activate && python test_environment.py
```

## 🐛 Troubleshooting

### Virtual Environment Issues
```bash
# Recreate virtual environment
./setup_python_env.sh --force

# Check Python version
python3 --version

# Verify virtual environment
source .venv/bin/activate
which python
which pip
```

### Kafka Connection Issues
```bash
# Check if Kafka is running
docker ps | grep kafka

# Start Kafka if not running
docker-compose up -d kafka

# Check Kafka logs
docker-compose logs kafka

# Test Kafka connectivity
source .venv/bin/activate
python -c "from kafka_utils import check_kafka_connectivity; print('✅ Connected' if check_kafka_connectivity(['localhost:29092']) else '❌ Failed')"
```

### Python Dependencies
```bash
# Upgrade pip in virtual environment
source .venv/bin/activate
python -m pip install --upgrade pip

# Reinstall dependencies
pip install -r requirements.txt --force-reinstall

# Check for missing dependencies
python test_environment.py
```

### Permission Issues
```bash
# Make all scripts executable
chmod +x *.sh

# Fix virtual environment permissions (if needed)
chmod -R u+w .venv/
```

## 📈 Monitoring

After running the scripts, monitor:

1. **Application Logs**: Check for event processing messages
2. **Database**: Verify exceptions are stored
3. **API Endpoints**: Query for captured exceptions
4. **Kafka Topics**: Check message delivery

```bash
# Check Kafka topic
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic OrderRejected \
  --from-beginning
```