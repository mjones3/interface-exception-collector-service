#!/usr/bin/env python3
"""
Kafka utilities using confluent-kafka library (better Python 3.13 compatibility).
Alternative implementation for environments where kafka-python doesn't work.
"""

import json
import uuid
import sys
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional

# Try to import confluent-kafka
try:
    from confluent_kafka import Producer, KafkaError, KafkaException
    KAFKA_AVAILABLE = True
    KAFKA_ERROR_MSG = None
except ImportError as e:
    KAFKA_AVAILABLE = False
    KAFKA_ERROR_MSG = str(e)
    # Create dummy classes for type hints
    class Producer:
        pass
    class KafkaError:
        pass
    class KafkaException(Exception):
        pass

# Try to import colorama for colored output (optional)
try:
    from colorama import init, Fore, Style
    init(autoreset=True)
    HAS_COLORAMA = True
except ImportError:
    HAS_COLORAMA = False
    # Fallback: no colors
    class Fore:
        RED = GREEN = YELLOW = BLUE = CYAN = MAGENTA = WHITE = ""
    class Style:
        BRIGHT = RESET_ALL = ""

class ConfluentKafkaEventSender:
    """Utility class for sending events to Kafka using confluent-kafka library."""
    
    def __init__(self, bootstrap_servers: List[str], **producer_config):
        """
        Initialize Kafka producer.
        
        Args:
            bootstrap_servers: List of Kafka bootstrap servers
            **producer_config: Additional Kafka producer configuration
        """
        if not KAFKA_AVAILABLE:
            raise ImportError(f"Confluent Kafka library not available: {KAFKA_ERROR_MSG}")
        
        self.bootstrap_servers = bootstrap_servers
        self.producer_config = {
            'bootstrap.servers': ','.join(bootstrap_servers),
            'acks': 'all',
            'retries': 3,
            'retry.backoff.ms': 1000,
            **producer_config
        }
        self.producer = None
    
    def __enter__(self):
        """Context manager entry."""
        self.connect()
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.close()
    
    def connect(self):
        """Create Kafka producer connection."""
        try:
            self.producer = Producer(self.producer_config)
            self._print_success("Connected to Kafka successfully")
        except Exception as e:
            self._print_error(f"Failed to connect to Kafka: {e}")
            self._print_info("Make sure Kafka is running and accessible")
            raise
    
    def send_event(self, topic: str, event: Dict[Any, Any], key: Optional[str] = None) -> bool:
        """
        Send an event to Kafka topic.
        
        Args:
            topic: Kafka topic name
            event: Event data as dictionary
            key: Optional message key for partitioning
            
        Returns:
            bool: True if successful, False otherwise
        """
        if not self.producer:
            self._print_error("Producer not connected. Call connect() first.")
            return False
        
        try:
            # Serialize the event to JSON
            value = json.dumps(event).encode('utf-8')
            key_bytes = key.encode('utf-8') if key else None
            
            # Send message
            self.producer.produce(
                topic=topic,
                value=value,
                key=key_bytes,
                callback=self._delivery_callback
            )
            
            # Wait for message to be delivered
            self.producer.flush(timeout=10)
            
            return True
            
        except KafkaException as e:
            self._print_error(f"Kafka error sending event: {e}")
            return False
        except Exception as e:
            self._print_error(f"Unexpected error sending event: {e}")
            return False
    
    def _delivery_callback(self, err, msg):
        """Callback for message delivery confirmation."""
        if err:
            self._print_error(f"Message delivery failed: {err}")
        # Success is handled by the caller
    
    def close(self):
        """Close Kafka producer connection."""
        if self.producer:
            self.producer.flush()
            self.producer = None
    
    def test_connection(self) -> bool:
        """Test Kafka connection without sending messages."""
        if not KAFKA_AVAILABLE:
            return False
        
        try:
            test_producer = Producer({
                'bootstrap.servers': ','.join(self.bootstrap_servers),
                'socket.timeout.ms': 5000
            })
            # Get cluster metadata to test connection
            metadata = test_producer.list_topics(timeout=5)
            return True
        except Exception:
            return False
    
    @staticmethod
    def _print_success(message: str):
        """Print success message with color if available."""
        if HAS_COLORAMA:
            print(f"{Fore.GREEN}✅ {message}{Style.RESET_ALL}")
        else:
            print(f"✅ {message}")
    
    @staticmethod
    def _print_error(message: str):
        """Print error message with color if available."""
        if HAS_COLORAMA:
            print(f"{Fore.RED}❌ {message}{Style.RESET_ALL}")
        else:
            print(f"❌ {message}")
    
    @staticmethod
    def _print_info(message: str):
        """Print info message with color if available."""
        if HAS_COLORAMA:
            print(f"{Fore.CYAN}ℹ️  {message}{Style.RESET_ALL}")
        else:
            print(f"ℹ️  {message}")

# Alias for compatibility
KafkaEventSender = ConfluentKafkaEventSender

class EventGenerator:
    """Base class for generating events with common functionality."""
    
    @staticmethod
    def generate_uuid() -> str:
        """Generate a UUID string."""
        return str(uuid.uuid4())
    
    @staticmethod
    def generate_timestamp() -> str:
        """Generate ISO timestamp string."""
        return datetime.now(timezone.utc).isoformat()
    
    @staticmethod
    def create_base_event(event_type: str, source: str) -> Dict[str, Any]:
        """
        Create base event structure with common fields.
        
        Args:
            event_type: Type of the event
            source: Source service name
            
        Returns:
            Dict with base event fields
        """
        return {
            'eventId': EventGenerator.generate_uuid(),
            'eventType': event_type,
            'eventVersion': '1.0',
            'occurredOn': EventGenerator.generate_timestamp(),
            'source': source,
            'correlationId': EventGenerator.generate_uuid(),
            'causationId': EventGenerator.generate_uuid()
        }

def check_kafka_connectivity(bootstrap_servers: List[str]) -> bool:
    """
    Check if Kafka is accessible.
    
    Args:
        bootstrap_servers: List of Kafka bootstrap servers
        
    Returns:
        bool: True if Kafka is accessible, False otherwise
    """
    if not KAFKA_AVAILABLE:
        print(f"❌ Confluent Kafka library not available: {KAFKA_ERROR_MSG}")
        return False
    
    try:
        sender = ConfluentKafkaEventSender(bootstrap_servers)
        return sender.test_connection()
    except Exception as e:
        print(f"❌ Error testing Kafka connectivity: {e}")
        return False

def print_event_summary(event: Dict[str, Any], event_number: int, total_events: int):
    """
    Print a summary of the event being sent.
    
    Args:
        event: Event dictionary
        event_number: Current event number
        total_events: Total number of events
    """
    if HAS_COLORAMA:
        print(f"{Fore.GREEN}✅ Event {event_number:2d}/{total_events} sent successfully:{Style.RESET_ALL}")
    else:
        print(f"✅ Event {event_number:2d}/{total_events} sent successfully:")
    
    # Extract common fields
    event_id = event.get('eventId', 'N/A')[:8] + '...'
    event_type = event.get('eventType', 'N/A')
    
    print(f"   📋 Event ID: {event_id}")
    print(f"   📝 Event Type: {event_type}")
    
    # Try to extract payload information
    payload = event.get('payload', {})
    if payload:
        transaction_id = payload.get('transactionId', 'N/A')
        print(f"   🔗 Transaction ID: {transaction_id}")
        
        # Print additional payload-specific info
        if 'customerId' in payload:
            print(f"   🏥 Customer: {payload['customerId']}")
        if 'locationCode' in payload:
            print(f"   📍 Location: {payload['locationCode']}")
        if 'rejectedReason' in payload:
            reason = payload['rejectedReason']
            print(f"   🚫 Reason: {reason[:50]}{'...' if len(reason) > 50 else ''}")
        if 'orderItems' in payload:
            print(f"   🩸 Items: {len(payload['orderItems'])} items")

if __name__ == "__main__":
    # Test the utilities
    print("🧪 Testing Confluent Kafka utilities...")
    
    # Test connection
    bootstrap_servers = ['localhost:29092']
    if check_kafka_connectivity(bootstrap_servers):
        print("✅ Kafka connectivity test passed")
    else:
        print("❌ Kafka connectivity test failed")
        sys.exit(1)
    
    # Test event generation
    base_event = EventGenerator.create_base_event("TestEvent", "test-service")
    print(f"✅ Base event generated: {base_event['eventId']}")
    
    print("🎉 All tests passed!")