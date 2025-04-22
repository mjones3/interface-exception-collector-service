package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseNotificationDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.List;

class UseCaseNotificationDtoMapperTest {

    private UseCaseNotificationDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UseCaseNotificationDtoMapper.class);
    }

    @Test
    void shouldMapSingleUseCaseNotificationOutputToDto() {
        // Given
        UseCaseMessage useCaseMessage = UseCaseMessage.builder()
            .type(UseCaseNotificationType.WARN)
            .code(1)
            .message("Test error message")
            .action("RETRY")
            .reason("Invalid input")
            .details(List.of("Detailed error information"))
            .build();

        UseCaseNotificationOutput input = UseCaseNotificationOutput.builder()
            .useCaseMessage(useCaseMessage)
            .build();

        // When
        UseCaseNotificationDTO result = mapper.toDto(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(useCaseMessage.type().name());
        assertThat(result.code()).isEqualTo(useCaseMessage.code());
        assertThat(result.message()).isEqualTo(useCaseMessage.message());
        assertThat(result.action()).isEqualTo(useCaseMessage.action());
        assertThat(result.reason()).isEqualTo(useCaseMessage.reason());
        assertThat(result.details()).isEqualTo(useCaseMessage.details());
    }

    @Test
    void shouldMapListOfUseCaseNotificationOutputToDto() {
        // Given
        UseCaseMessage message1 = UseCaseMessage.builder()
            .type(UseCaseNotificationType.CAUTION)
            .code(1)
            .message("First error")
            .build();

        UseCaseMessage message2 = UseCaseMessage.builder()
            .type(UseCaseNotificationType.WARN)
            .code(2)
            .message("First warning")
            .build();

        UseCaseNotificationOutput input1 = UseCaseNotificationOutput.builder()
            .useCaseMessage(message1)
            .build();

        UseCaseNotificationOutput input2 = UseCaseNotificationOutput.builder()
            .useCaseMessage(message2)
            .build();

        List<UseCaseNotificationOutput> inputList = Arrays.asList(input1, input2);

        // When
        List<UseCaseNotificationDTO> result = mapper.toDto(inputList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify first item
        assertThat(result.get(0).type()).isEqualTo(message1.type().name());
        assertThat(result.get(0).code()).isEqualTo(message1.code());
        assertThat(result.get(0).message()).isEqualTo(message1.message());

        // Verify second item
        assertThat(result.get(1).type()).isEqualTo(message2.type().name());
        assertThat(result.get(1).code()).isEqualTo(message2.code());
        assertThat(result.get(1).message()).isEqualTo(message2.message());
    }


    @Test
    void shouldHandleNullList() {
        // When
        List<UseCaseNotificationDTO> result = mapper.toDto((List<UseCaseNotificationOutput>) null);

        // Then
        assertThat(result).isNull();
    }
}
