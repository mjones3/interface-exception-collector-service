#!/usr/bin/env python3
"""
Test script to validate the Python virtual environment setup.
This script checks all dependencies and configurations.
"""

import sys
import os
from pathlib import Path

def check_python_version():
    """Check Python version compatibility."""
    print("🐍 Python Version Check")
    print(f"   Version: {sys.version}")
    print(f"   Executable: {sys.executable}")
    
    # Check if we're in a virtual environment
    if sys.prefix != sys.base_prefix:
        print("   ✅ Running in virtual environment")
        print(f"   Virtual env: {sys.prefix}")
    else:
        print("   ⚠️  Not running in virtual environment")
        return False
    
    # Check Python version
    if sys.version_info >= (3, 8):
        print("   ✅ Python version is compatible")
        return True
    else:
        print("   ❌ Python 3.8+ required")
        return False

def check_dependencies():
    """Check if all required dependencies are installed."""
    print("\n📦 Dependency Check")
    
    # Check for Kafka libraries (either kafka-python or confluent-kafka)
    kafka_available = False
    kafka_lib = None
    
    try:
        import kafka
        kafka_available = True
        kafka_lib = "kafka-python"
        print(f"   ✅ kafka-python")
    except ImportError:
        try:
            import confluent_kafka
            kafka_available = True
            kafka_lib = "confluent-kafka"
            print(f"   ✅ confluent-kafka")
        except ImportError:
            print(f"   ❌ kafka library (neither kafka-python nor confluent-kafka)")
    
    # Check other required packages
    other_packages = [
        ('requests', 'requests'),
        ('dateutil', 'python-dateutil'),
        ('colorama', 'colorama')
    ]
    
    missing_packages = []
    
    for import_name, package_name in other_packages:
        try:
            __import__(import_name)
            print(f"   ✅ {package_name}")
        except ImportError:
            print(f"   ❌ {package_name} (missing)")
            missing_packages.append(package_name)
    
    if not kafka_available:
        missing_packages.append("kafka library")
    
    if missing_packages:
        print(f"\n   Missing packages: {', '.join(missing_packages)}")
        python_version = f"{sys.version_info.major}.{sys.version_info.minor}"
        if python_version == "3.13":
            print("   For Python 3.13, run: pip install -r requirements-py313.txt")
        else:
            print("   Run: pip install -r requirements.txt")
        return False
    
    if kafka_available:
        print(f"\n   ℹ️  Using {kafka_lib} for Kafka connectivity")
    
    return True

def check_kafka_utils():
    """Check if our custom utilities are available."""
    print("\n🛠️  Utility Module Check")
    
    try:
        from kafka_utils import KafkaEventSender, EventGenerator, check_kafka_connectivity
        print("   ✅ kafka_utils module imported successfully")
        
        # Test utility functions
        event_gen = EventGenerator()
        test_event = event_gen.create_base_event("TestEvent", "test-service")
        print("   ✅ EventGenerator working")
        
        return True
        
    except ImportError as e:
        print(f"   ❌ Failed to import kafka_utils: {e}")
        return False
    except Exception as e:
        print(f"   ❌ Error testing utilities: {e}")
        return False

def check_kafka_connectivity():
    """Test Kafka connectivity."""
    print("\n🔗 Kafka Connectivity Check")
    
    try:
        from kafka_utils import check_kafka_connectivity
        
        bootstrap_servers = ['localhost:29092']
        if check_kafka_connectivity(bootstrap_servers):
            print("   ✅ Kafka is accessible")
            return True
        else:
            print("   ❌ Cannot connect to Kafka")
            print("   💡 Make sure Kafka is running: docker-compose up -d kafka")
            return False
            
    except Exception as e:
        print(f"   ❌ Error checking Kafka: {e}")
        return False

def check_file_structure():
    """Check if all required files are present."""
    print("\n📁 File Structure Check")
    
    script_dir = Path(__file__).parent
    required_files = [
        'requirements.txt',
        'kafka_utils.py',
        'send_order_rejected_events.py',
        'send_test_events.sh',
        'setup_python_env.sh'
    ]
    
    all_present = True
    
    for file_name in required_files:
        file_path = script_dir / file_name
        if file_path.exists():
            print(f"   ✅ {file_name}")
        else:
            print(f"   ❌ {file_name} (missing)")
            all_present = False
    
    return all_present

def main():
    """Run all environment tests."""
    print("🧪 Python Environment Test Suite")
    print("=" * 40)
    
    tests = [
        ("Python Version", check_python_version),
        ("Dependencies", check_dependencies),
        ("Utility Modules", check_kafka_utils),
        ("File Structure", check_file_structure),
        ("Kafka Connectivity", check_kafka_connectivity)
    ]
    
    results = []
    
    for test_name, test_func in tests:
        try:
            result = test_func()
            results.append((test_name, result))
        except Exception as e:
            print(f"   💥 Test failed with error: {e}")
            results.append((test_name, False))
    
    # Summary
    print("\n📊 Test Results Summary")
    print("-" * 30)
    
    passed = 0
    total = len(results)
    
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"   {test_name:<20} {status}")
        if result:
            passed += 1
    
    print(f"\n🎯 Overall: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 All tests passed! Environment is ready.")
        print("🚀 You can now run: ./send_test_events.sh")
        return 0
    else:
        print("⚠️  Some tests failed. Please fix the issues above.")
        print("💡 Try running: ./setup_python_env.sh --force")
        return 1

if __name__ == "__main__":
    exit(main())