package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.query.UpdateMapper;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.dialect.RenderContextFactory;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Configuration
@EnableR2dbcRepositories({"com.arcone.biopro.distribution.inventory.domain.repository"})
@EnableTransactionManagement
@Slf4j
public class DatabaseConfiguration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    // LocalDateTime seems to be the only type that is supported across all drivers atm
    // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(R2dbcDialect dialect) {
        List<Object> converters = new ArrayList<>();
        converters.add(InstantWriteConverter.INSTANCE);
        converters.add(InstantReadConverter.INSTANCE);
        converters.add(BitSetReadConverter.INSTANCE);
        converters.add(DurationWriteConverter.INSTANCE);
        converters.add(DurationReadConverter.INSTANCE);
        converters.add(ZonedDateTimeReadConverter.INSTANCE);
        converters.add(ZonedDateTimeWriteConverter.INSTANCE);
        converters.add(JsonToQuarantineListConverter.INSTANCE);
        converters.add(JsonToVolumeListConverter.INSTANCE);
        converters.add(QuarantineListToJsonConverter.INSTANCE);
        converters.add(VolumeListToJsonConverter.INSTANCE);
        converters.add(JsonToHistoryListConverter.INSTANCE);
        converters.add(HistoryListToJsonConverter.INSTANCE);
        return R2dbcCustomConversions.of(dialect, converters);
    }

    @Bean
    public R2dbcDialect dialect(ConnectionFactory connectionFactory) {
        return DialectResolver.getDialect(connectionFactory);
    }

    @Bean
    public UpdateMapper updateMapper(R2dbcDialect dialect, MappingR2dbcConverter mappingR2dbcConverter) {
        return new UpdateMapper(dialect, mappingR2dbcConverter);
    }

    @Bean
    public SqlRenderer sqlRenderer(R2dbcDialect dialect) {
        RenderContextFactory factory = new RenderContextFactory(dialect);
        return SqlRenderer.create(factory.createRenderContext());
    }

    @WritingConverter
    public enum InstantWriteConverter implements Converter<Instant, LocalDateTime> {
        INSTANCE;

        public LocalDateTime convert(Instant source) {
            return LocalDateTime.ofInstant(source, ZoneOffset.UTC);
        }
    }

    @ReadingConverter
    public enum InstantReadConverter implements Converter<LocalDateTime, Instant> {
        INSTANCE;

        @Override
        public Instant convert(LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
    }

    @ReadingConverter
    public enum BitSetReadConverter implements Converter<BitSet, Boolean> {
        INSTANCE;

        @Override
        public Boolean convert(BitSet bitSet) {
            return bitSet.get(0);
        }
    }

    @ReadingConverter
    public enum ZonedDateTimeReadConverter implements Converter<LocalDateTime, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(LocalDateTime localDateTime) {
            // Be aware - we are using the UTC timezone
            return ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        }
    }

    @WritingConverter
    public enum ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, LocalDateTime> {
        INSTANCE;

        @Override
        public LocalDateTime convert(ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDateTime();
        }
    }

    @WritingConverter
    public enum DurationWriteConverter implements Converter<Duration, Long> {
        INSTANCE;

        @Override
        public Long convert(Duration source) {
            return source != null ? source.toMillis() : null;
        }
    }

    @ReadingConverter
    public enum DurationReadConverter implements Converter<Long, Duration> {
        INSTANCE;

        @Override
        public Duration convert(Long source) {
            return source != null ? Duration.ofMillis(source) : null;
        }
    }

    @ReadingConverter
    public enum JsonToQuarantineListConverter implements Converter<Json, List<Quarantine>> {
        INSTANCE;

        @Override
        public List<Quarantine> convert(Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), new TypeReference<List<Quarantine>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert Json to List<Quarantine>", e);
            }
        }
    }

    @WritingConverter
    public enum QuarantineListToJsonConverter implements Converter<List<Quarantine>, Json> {
        INSTANCE;

        @Override
        public Json convert(List<Quarantine> source) {
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert List<Quarantine> to Json", e);
            }
        }
    }

    @ReadingConverter
    public enum JsonToVolumeListConverter implements Converter<Json, List<Volume>> {
        INSTANCE;

        @Override
        public List<Volume> convert(Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert Json to List<Volume>", e);
            }
        }
    }

    @WritingConverter
    public enum VolumeListToJsonConverter implements Converter<List<Volume>, Json> {
        INSTANCE;

        @Override
        public Json convert(List<Volume> source) {
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert List<Volume> to Json", e);
            }
        }
    }

    @ReadingConverter
    public enum JsonToHistoryListConverter implements Converter<Json, List<History>> {
        INSTANCE;

        @Override
        public List<History> convert(Json source) {
            try {
                return OBJECT_MAPPER.readValue(source.asString(), new TypeReference<List<History>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert Json to List<History>", e);
            }
        }
    }

    @WritingConverter
    public enum HistoryListToJsonConverter implements Converter<List<History>, Json> {
        INSTANCE;

        @Override
        public Json convert(List<History> source) {
            try {
                return Json.of(OBJECT_MAPPER.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert List<History> to Json", e);
            }
        }
    }
}
