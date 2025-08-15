package com.arcone.biopro.exception.collector.api.mapper;

import com.arcone.biopro.exception.collector.api.dto.ExceptionDetailResponse;
import com.arcone.biopro.exception.collector.api.dto.ExceptionListResponse;
import com.arcone.biopro.exception.collector.api.dto.PagedResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryAttemptResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * MapStruct mapper for converting between domain entities and API DTOs.
 * Provides mapping methods for exception and retry attempt entities.
 */
@Mapper(componentModel = "spring")
public interface ExceptionMapper {

    /**
     * Maps InterfaceException entity to ExceptionListResponse DTO.
     *
     * @param exception the entity to map
     * @return the mapped DTO
     */
    ExceptionListResponse toListResponse(InterfaceException exception);

    /**
     * Maps a list of InterfaceException entities to ExceptionListResponse DTOs.
     *
     * @param exceptions the entities to map
     * @return the mapped DTOs
     */
    List<ExceptionListResponse> toListResponse(List<InterfaceException> exceptions);

    /**
     * Maps InterfaceException entity to ExceptionDetailResponse DTO.
     * Includes retry history and related exceptions if present.
     *
     * @param exception the entity to map
     * @return the mapped DTO
     */
    @Mapping(target = "originalPayload", ignore = true)
    @Mapping(target = "retryHistory", source = "retryAttempts")
    @Mapping(target = "relatedExceptions", ignore = true)
    ExceptionDetailResponse toDetailResponse(InterfaceException exception);

    /**
     * Maps RetryAttempt entity to RetryAttemptResponse DTO.
     *
     * @param retryAttempt the entity to map
     * @return the mapped DTO
     */
    RetryAttemptResponse toRetryAttemptResponse(RetryAttempt retryAttempt);

    /**
     * Maps a list of RetryAttempt entities to RetryAttemptResponse DTOs.
     *
     * @param retryAttempts the entities to map
     * @return the mapped DTOs
     */
    List<RetryAttemptResponse> toRetryAttemptResponse(List<RetryAttempt> retryAttempts);

    /**
     * Maps a Spring Data Page to a PagedResponse DTO.
     *
     * @param page the Spring Data Page
     * @param <T>  the type of content in the page
     * @return the mapped PagedResponse
     */
    default <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Maps a Page of InterfaceException entities to a PagedResponse of
     * ExceptionListResponse DTOs.
     *
     * @param page the page of entities
     * @return the mapped paged response
     */
    default PagedResponse<ExceptionListResponse> toPagedListResponse(Page<InterfaceException> page) {
        List<ExceptionListResponse> content = toListResponse(page.getContent());
        return PagedResponse.<ExceptionListResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
}