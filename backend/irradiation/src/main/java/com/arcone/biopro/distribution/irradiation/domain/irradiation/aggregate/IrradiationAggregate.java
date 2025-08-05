package com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate;

import com.arcone.biopro.distribution.irradiation.domain.exception.BatchSubmissionException;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchItem;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.service.ProductDeterminationService;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

@Getter
public class IrradiationAggregate {
    private final Device device;
    private final List<Inventory> inventories;
    private final Batch batch;
    private final ProductDeterminationService productDeterminationService;
    private final List<BatchItemCompletion> itemCompletions;
    private final Map<String, ProductDetermination> productDeterminations;
    private final LocalDateTime completionTime;

    public IrradiationAggregate(Device device, List<Inventory> inventories, Batch batch, ProductDeterminationService productDeterminationService) {
        this.device = device;
        this.inventories = inventories;
        this.batch = batch;
        this.productDeterminationService = productDeterminationService;
        this.itemCompletions = null;
        this.productDeterminations = null;
        this.completionTime = null;
    }

    public IrradiationAggregate(Device device, List<Inventory> inventories, Batch batch) {
        this.device = device;
        this.inventories = inventories;
        this.batch = batch;
        this.productDeterminationService = null;
        this.itemCompletions = null;
        this.productDeterminations = null;
        this.completionTime = null;
    }

    // Constructor for batch completion with service
    public IrradiationAggregate(Device device, Batch batch, List<BatchItemCompletion> itemCompletions,
                               ProductDeterminationService productDeterminationService, LocalDateTime completionTime) {
        this.device = device;
        this.batch = batch;
        this.itemCompletions = itemCompletions;
        this.productDeterminationService = productDeterminationService;
        this.completionTime = completionTime;
        this.inventories = null;
        this.productDeterminations = null;
    }

    // Constructor for batch completion with pre-loaded determinations
    public IrradiationAggregate(Device device, Batch batch, List<BatchItemCompletion> itemCompletions,
                               Map<String, ProductDetermination> productDeterminations, LocalDateTime completionTime) {
        this.device = device;
        this.batch = batch;
        this.itemCompletions = itemCompletions;
        this.productDeterminations = productDeterminations;
        this.completionTime = completionTime;
        this.inventories = null;
        this.productDeterminationService = null;
    }

    public boolean validateDevice(Location targetLocation) {
        return device != null && device.isAtLocation(targetLocation);
    }

    public boolean validateDeviceIsInUse(Batch batch) {
        return batch != null && batch.isActive();
    }

    public boolean validateDeviceIsInUse() {
        return batch != null && batch.isActive();
    }

    public List<Inventory> getValidInventoriesForIrradiation(Location targetLocation) {
        return inventories != null ? inventories.stream()
            .filter(inventory -> isValidInventoryForIrradiation(inventory, targetLocation))
            .toList() : List.of();
    }

    private boolean isValidInventoryForIrradiation(Inventory inventory, Location targetLocation) {
        return ("AVAILABLE".equals(inventory.getStatus()) || "DISCARDED".equals(inventory.getStatus())) &&
            inventory.getLocation().equals(targetLocation);
    }

    public boolean canSubmitBatch(List<BatchItem> batchItems, Location targetLocation) {
        if (batchItems == null || batchItems.isEmpty() || inventories == null) {
            return false;
        }

        List<Inventory> validInventories = getValidInventoriesForIrradiation(targetLocation);
        List<UnitNumber> availableUnitNumbers = validInventories.stream()
                .map(Inventory::getUnitNumber)
                .toList();

        List<UnitNumber> requestedUnitNumbers = batchItems.stream()
                .map(BatchItem::unitNumber)
                .toList();

        return availableUnitNumbers.containsAll(requestedUnitNumbers);
    }

    public Mono<ProductCode> determineTargetProduct(ProductCode sourceProductCode) {
        if (productDeterminationService == null) {
            throw new UnsupportedOperationException("Missing productDeterminationService");
        }
        return productDeterminationService.determineTargetProduct(sourceProductCode);
    }

    public Mono<Map<String, ProductDetermination>> loadProductDeterminations() {
        if (productDeterminationService == null) {
            return Mono.just(productDeterminations != null ? productDeterminations : Map.of());
        }

        return reactor.core.publisher.Flux.fromIterable(getIrradiatedItems())
                .map(item -> new ProductCode(item.productCode()))
                .distinct()
                .flatMap(productDeterminationService::findProductDetermination)
                .collectMap(determination -> determination.getSourceProductCode().value())
                .defaultIfEmpty(Map.of());
    }

    // Batch completion methods - safe for all usage scenarios
    public List<BatchItemCompletion> getIrradiatedItems() {
        return itemCompletions != null ? itemCompletions.stream()
                .filter(BatchItemCompletion::isIrradiated)
                .toList() : List.of();
    }

    public List<BatchItemCompletion> getNonIrradiatedItems() {
        return itemCompletions != null ? itemCompletions.stream()
                .filter(item -> !item.isIrradiated())
                .toList() : List.of();
    }

    public boolean isConfiguredForCompletion() {
        return itemCompletions != null && productDeterminations != null && completionTime != null;
    }

    public BatchItem processIrradiatedItem(BatchItemCompletion completion, BatchItem originalItem) {
        if (productDeterminations == null) {
            throw new UnsupportedOperationException("Product determinations not available");
        }

        ProductDetermination determination = productDeterminations.get(completion.productCode());
        if (determination == null) {
            throw new UnsupportedOperationException("No product determination found for: " + completion.productCode());
        }

        return BatchItem.builder()
                .id(originalItem.id())
                .unitNumber(originalItem.unitNumber())
                .productCode(originalItem.productCode())
                .lotNumber(originalItem.lotNumber())
                .newProductCode(determination.getTargetProductCode().value())
                .expirationDate(originalItem.expirationDate())
                .productFamily(originalItem.productFamily())
                .productDescription(determination.getTargetProductDescription())
                .irradiated(originalItem.irradiated())
                .build();
    }

    public void validateBatchCompletion() {
        if (batch != null && !batch.isActive()) {
            throw new BatchSubmissionException("Batch is already completed");
        }

        if (productDeterminations != null && itemCompletions != null) {
            List<String> missingDeterminations = getIrradiatedItems().stream()
                    .map(BatchItemCompletion::productCode)
                    .filter(productCode -> !productDeterminations.containsKey(productCode))
                    .distinct()
                    .toList();

            if (!missingDeterminations.isEmpty()) {
                throw new UnsupportedOperationException("Missing product determinations for: " +
                    String.join(", ", missingDeterminations));
            }
        }
    }

    public record BatchItemCompletion(String unitNumber, String productCode, boolean isIrradiated) {}
}
