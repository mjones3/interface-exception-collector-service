# WSL Bash Scripts for GraphQL Subscription Testing

This guide covers the bash scripts created for testing GraphQL subscriptions in WSL (Windows Subsystem for Linux).

## 🚀 Quick Start

1. **Setup and check environment:**
   ```bash
   ./setup-wsl-testing.sh
   ```

2. **Run interactive menu:**
   ```bash
   ./run-tests.sh
   ```

3. **Quick test:**
   ```bash
   ./quick-test-subscriptions.sh
   ```

## 📋 Available Scripts

### Core Testing Scripts

| Script | Purpose | Description |
|--------|---------|-------------|
| `setup-wsl-testing.sh` | Environment setup | Checks dependencies and service availability |
| `run-tests.sh` | Interactive menu | Main menu for all testing options |
| `quick-test-subscriptions.sh` | Quick validation | Fast test of GraphQL subscriptions |

### Authentication & Connection

| Script | Purpose | Description |
|--------|---------|-------------|
| `test-with-auth.sh` | Auth testing | Tests JWT authentication with GraphQL |

### Monitoring & Watching

| Script | Purpose | Description |
|--------|---------|-------------|
| `watch-graphql.sh` | HTTP polling | Monitors exceptions via HTTP requests |
| `watch-simple.sh` | Simple polling | Basic exception monitoring |
| `watch-live-subscriptions.sh` | WebSocket real-time | Real-time WebSocket subscription monitoring |

### Event Generation

| Script | Purpose | Description |
|--------|---------|-------------|
| `trigger-events.sh` | Event creation | Interactive menu to create test exceptions |
| `debug-subscription-events.sh` | Debug testing | Creates events and monitors subscriptions |

## 🔧 Prerequisites

### Required Tools
- **curl** - For HTTP requests
- **bash** - Shell environment (WSL/Linux)

### Optional Tools
- **jq** - JSON parsing (highly recommended)
  ```bash
  sudo apt install jq
  ```
- **Node.js + ws module** - For WebSocket testing
  ```bash
  # Install Node.js
  curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
  sudo apt-get install -y nodejs
  
  # Install WebSocket module
  npm install ws
  ```

## 🎯 Usage Examples

### 1. Environment Check
```bash
# Check if everything is set up correctly
./setup-wsl-testing.sh
```

### 2. Quick Test
```bash
# Run a quick test to verify subscriptions work
./quick-test-subscriptions.sh
```

### 3. Live Monitoring
```bash
# Start real-time WebSocket monitoring
./watch-live-subscriptions.sh

# Or use HTTP polling if WebSocket isn't available
./watch-graphql.sh
```

### 4. Generate Test Events
```bash
# Open interactive menu to create test exceptions
./trigger-events.sh
```

### 5. Debug Session
```bash
# Run a complete debug session
./debug-subscription-events.sh
```

## 🔍 Script Details

### Authentication
All scripts use JWT tokens for authentication. They will:
1. Try to generate a fresh token using `generate-jwt-correct-secret.js`
2. Fall back to a hardcoded token if generation fails
3. Update the `watch-graphql.sh` script with new tokens automatically

### WebSocket vs HTTP Polling
- **WebSocket** (`watch-live-subscriptions.sh`): Real-time events, requires Node.js + ws module
- **HTTP Polling** (`watch-graphql.sh`): Polls every few seconds, only requires curl

### Error Handling
Scripts include comprehensive error handling:
- Check for required tools
- Validate service availability
- Provide fallback options
- Clear error messages with suggestions

## 🐛 Troubleshooting

### Common Issues

1. **"curl: command not found"**
   ```bash
   sudo apt update && sudo apt install curl
   ```

2. **"jq: command not found"**
   ```bash
   sudo apt install jq
   ```

3. **"WebSocket module not found"**
   ```bash
   npm install ws
   ```

4. **"GraphQL service not available"**
   - Make sure your application is running: `tilt up`
   - Check if port 8080 is accessible

5. **"Permission denied"**
   ```bash
   chmod +x *.sh
   ```

### Service Endpoints
- **GraphQL**: `http://localhost:8080/graphql`
- **Partner Order Service**: `http://localhost:8090`
- **WebSocket**: `ws://localhost:8080/graphql`

## 💡 Tips

1. **Use the interactive menu** (`./run-tests.sh`) for easy navigation
2. **Install jq** for better JSON output formatting
3. **Run setup script first** to check your environment
4. **Use WebSocket monitoring** for real-time events when possible
5. **Keep trigger script open** in another terminal while monitoring

## 🔄 Converting from PowerShell

These bash scripts are direct conversions of the PowerShell scripts with equivalent functionality:

| PowerShell | Bash Equivalent |
|------------|-----------------|
| `debug-subscription-events.ps1` | `debug-subscription-events.sh` |
| `test-with-auth.ps1` | `test-with-auth.sh` |
| `trigger-events.ps1` | `trigger-events.sh` |
| `quick-test-subscriptions.ps1` | `quick-test-subscriptions.sh` |
| `watch-live-subscriptions.ps1` | `watch-live-subscriptions.sh` |

The bash versions include additional error handling and WSL-specific optimizations.