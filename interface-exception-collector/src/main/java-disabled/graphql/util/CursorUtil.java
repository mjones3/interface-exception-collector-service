package com.arcone.biopro.exception.collector.api.graphql.util;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Utility class for handling cursor-based pagination in GraphQL queries.
 * Implements cursor encoding/decoding for stable pagination.
 */
@Slf4j
public class CursorUtil {

    private static final String CURSOR_SEPARATOR = ":";
    private static final DateTimeFormatter CURSOR_DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Creates a cursor from an InterfaceException entity.
     * The cursor contains timestamp and ID for stable sorting.
     * 
     * @param exception the exception to create cursor for
     * @return base64 encoded cursor string
     */
    public static String createCursor(InterfaceException exception) {
        if (exception == null || exception.getTimestamp() == null || exception.getId() == null) {
            return null;
        }

        String cursorData = exception.getTimestamp().format(CURSOR_DATE_FORMAT) +
                CURSOR_SEPARATOR +
                exception.getId();

        return Base64.getEncoder().encodeToString(cursorData.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses a cursor to extract timestamp and ID information.
     * 
     * @param cursor the base64 encoded cursor string
     * @return CursorData containing parsed information, or null if invalid
     */
    public static CursorData parseCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return null;
        }

        try {
            String decodedCursor = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decodedCursor.split(CURSOR_SEPARATOR);

            if (parts.length != 2) {
                log.warn("Invalid cursor format: {}", cursor);
                return null;
            }

            OffsetDateTime timestamp = OffsetDateTime.parse(parts[0], CURSOR_DATE_FORMAT);
            Long id = Long.parseLong(parts[1]);

            return new CursorData(timestamp, id);

        } catch (Exception e) {
            log.warn("Failed to parse cursor: {}", cursor, e);
            return null;
        }
    }

    /**
     * Data class for parsed cursor information.
     */
    public static class CursorData {
        private final OffsetDateTime timestamp;
        private final Long id;

        public CursorData(OffsetDateTime timestamp, Long id) {
            this.timestamp = timestamp;
            this.id = id;
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public Long getId() {
            return id;
        }
    }
}