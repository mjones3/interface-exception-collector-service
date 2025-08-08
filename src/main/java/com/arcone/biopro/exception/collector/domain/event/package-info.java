/**
 * Event schema classes for Kafka message processing in the Interface Exception
 * Collector Service.
 * 
 * <p>
 * This package contains all event classes used for Kafka message serialization
 * and deserialization,
 * organized into the following sub-packages:
 * </p>
 * 
 * <ul>
 * <li><strong>base</strong> - Base classes with common event fields</li>
 * <li><strong>inbound</strong> - Events consumed from other BioPro
 * services</li>
 * <li><strong>outbound</strong> - Events published by this service</li>
 * </ul>
 * 
 * <h3>Event Structure</h3>
 * <p>
 * All events extend
 * {@link com.arcone.biopro.exception.collector.domain.event.base.BaseEvent}
 * which provides standard metadata fields including:
 * </p>
 * <ul>
 * <li>eventId - Unique identifier for the event</li>
 * <li>eventType - Type of the event (e.g., "OrderRejected")</li>
 * <li>eventVersion - Schema version for backward compatibility</li>
 * <li>occurredOn - Timestamp when the event occurred</li>
 * <li>source - Service that published the event</li>
 * <li>correlationId - ID for tracing related events</li>
 * <li>causationId - ID of the event that caused this event</li>
 * </ul>
 * 
 * <h3>Serialization</h3>
 * <p>
 * All event classes use Jackson annotations for JSON
 * serialization/deserialization
 * and include validation annotations for data integrity.
 * </p>
 * 
 * <h3>Mapping</h3>
 * <p>
 * Event-to-entity mapping is handled by
 * {@link com.arcone.biopro.exception.collector.domain.mapper.EventMapper}
 * using MapStruct for type-safe transformations.
 * </p>
 * 
 * @since 1.0.0
 */
package com.arcone.biopro.exception.collector.domain.event;