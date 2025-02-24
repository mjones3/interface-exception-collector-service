package com.arcone.biopro.distribution.order.domain.model;


import com.arcone.biopro.distribution.order.domain.exception.DomainException;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType;
import com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.NO_ORDER_TO_BE_CANCELLED;
import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.ORDER_HAS_AN_OPEN_SHIPMENT;
import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.ORDER_IS_ALREADY_CANCELLED;
import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.ORDER_IS_ALREADY_COMPLETED;
import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.ORDER_IS_NOT_IN_PROGRESS_AND_CANNOT_BE_COMPLETED;
import static com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType.ORDER_IS_NOT_OPEN_AND_CANNOT_BE_CANCELLED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class Order implements Validatable {

    private Long id;
    private OrderNumber orderNumber;
    private OrderExternalId orderExternalId;
    private String locationCode;
    private ShipmentType shipmentType;
    private ShippingMethod shippingMethod;
    private OrderCustomer shippingCustomer;
    private OrderCustomer billingCustomer;
    private LocalDate desiredShippingDate;
    private Boolean willCallPickup;
    private String phoneNumber;
    private ProductCategory productCategory;
    private String comments;
    private OrderStatus orderStatus;
    private OrderPriority orderPriority;
    private String createEmployeeId;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private ZonedDateTime deleteDate;
    private List<OrderItem> orderItems;
    @Setter
    private String completeEmployeeId;
    @Setter
    private ZonedDateTime completeDate;
    @Setter
    private String completeComments;

    @Getter(AccessLevel.NONE)
    private Integer totalShipped;

    @Getter(AccessLevel.NONE)
    private Integer totalRemaining;

    @Getter(AccessLevel.NONE)
    private Integer totalProducts;

    @Setter
    private boolean backOrder;

    private static final String ORDER_IN_PROGRESS_STATUS = "IN_PROGRESS";
    private static final String ORDER_COMPLETED_STATUS = "COMPLETED";
    private static final String ORDER_SHIPMENT_OPEN_STATUS = "OPEN";
    private static final String ORDER_OPEN_STATUS = "OPEN";
    private static final String ORDER_CANCELLED_STATUS = "CANCELLED";

    @Setter
    private String cancelEmployeeId;
    @Setter
    private ZonedDateTime cancelDate;
    @Setter
    private String cancelReason;

    public Order(
        CustomerService customerService,
        LookupService lookupService,
        Long id,
        Long orderNumber,
        String externalId,
        String locationCode,
        String shipmentType,
        String shippingMethod,
        String shippingCustomerCode,
        String billingCustomerCode,
        String desiredShippingDate,
        Boolean willCallPickup,
        String phoneNumber,
        String productCategory,
        String comments,
        String orderStatus,
        String orderPriority,
        String createEmployeeId,
        ZonedDateTime createDate,
        ZonedDateTime modificationDate,
        ZonedDateTime deleteDate
    ) {
        this.id = id;
        this.orderNumber = new OrderNumber(orderNumber);
        this.orderExternalId = new OrderExternalId(externalId);
        this.locationCode = locationCode;
        this.shipmentType = new ShipmentType(shipmentType, lookupService);
        this.shippingMethod = new ShippingMethod(shippingMethod, lookupService);
        this.shippingCustomer = new OrderCustomer(shippingCustomerCode, customerService);
        this.billingCustomer = new OrderCustomer(billingCustomerCode, customerService);
        if(desiredShippingDate != null){
            try {
                this.desiredShippingDate = LocalDate.parse(desiredShippingDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("desiredShippingDate is invalid");
            }
        }

        this.willCallPickup = willCallPickup;
        this.phoneNumber = phoneNumber;
        this.productCategory = new ProductCategory(productCategory, lookupService);
        this.comments = comments;
        this.orderStatus = new OrderStatus(orderStatus, lookupService);
        this.orderPriority = new OrderPriority(orderPriority, lookupService);
        this.createEmployeeId = createEmployeeId;
        this.createDate = createDate;
        this.modificationDate = modificationDate;
        this.deleteDate = deleteDate;
        this.backOrder = false;

        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }
        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("locationCode cannot be null or blank");
        }
        if (this.shipmentType == null) {
            throw new IllegalArgumentException("shipmentType cannot be null");
        }
        if (this.shippingMethod == null) {
            throw new IllegalArgumentException("shippingMethod cannot be null");
        }
        if (this.shippingCustomer == null) {
            throw new IllegalArgumentException("shippingCustomer could not be found or it is null");
        }
        if (this.billingCustomer == null) {
            throw new IllegalArgumentException("billingCustomer could not be found or it is null");
        }
        if (this.desiredShippingDate != null && this.desiredShippingDate.isBefore(LocalDate.now()) && this.id == null) {
            throw new IllegalArgumentException("desiredShippingDate cannot be in the past");
        }
        if (this.productCategory == null) {
            throw new IllegalArgumentException("productCategory cannot be null");
        }
        if (this.orderStatus == null) {
            throw new IllegalArgumentException("orderStatus cannot be null");
        }
        if (this.orderPriority == null) {
            throw new IllegalArgumentException("orderPriority cannot be null");
        }
        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("createEmployeeId cannot be null or blank");
        }
    }

    public void addItem(Long id, String productFamily, String bloodType, Integer quantity, Integer quantityShipped, String comments
        , ZonedDateTime createDate, ZonedDateTime modificationDate, OrderConfigService orderConfigService) {

        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }

        this.orderItems.add(new OrderItem(id, this.id, productFamily, bloodType, quantity, quantityShipped, comments, createDate
            , modificationDate, this.getProductCategory().getProductCategory(), orderConfigService));
    }

    public Mono<Boolean> exists(final OrderRepository orderRepository) {
        return ofNullable(this.id)
            .map(id -> orderRepository.existsById(id, TRUE))
            .orElseGet(() -> Mono.just(FALSE));
    }

    public Integer getTotalShipped() {
        return ofNullable(orderItems)
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .reduce(0, (partialAgeResult, orderItem) -> partialAgeResult + orderItem.getQuantityShipped(), Integer::sum);
    }

    public Integer getTotalRemaining() {
        return ofNullable(orderItems)
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .reduce(0, (partialAgeResult, orderItem) -> partialAgeResult + orderItem.getQuantityRemaining(), Integer::sum);
    }

    public Integer getTotalProducts() {
        return ofNullable(orderItems)
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .reduce(0, (partialAgeResult, orderItem) -> partialAgeResult + orderItem.getQuantity(), Integer::sum);
    }

    public boolean isCompleted() {
        log.debug("Order {} totalShipped: {} totalRemaining: {} totalProducts: {}", this.orderNumber, this.totalShipped, this.totalRemaining, this.totalProducts);
        return this.getTotalRemaining().equals(0);
    }

    public boolean canBeCompleted(OrderShipmentService orderShipmentService) {
        return ORDER_IN_PROGRESS_STATUS.equals(orderStatus.getOrderStatus()) && (this.getTotalRemaining().compareTo(0) > 0) && !hasShipmentOpen(orderShipmentService);
    }

    public void completeOrder(CompleteOrderCommand completeOrderCommand, LookupService lookupService, OrderShipmentService orderShipmentService) {
        if (ORDER_COMPLETED_STATUS.equals(orderStatus.getOrderStatus())) {
            throw new DomainException(ORDER_IS_ALREADY_COMPLETED);
        }

        if (!ORDER_IN_PROGRESS_STATUS.equals(orderStatus.getOrderStatus())) {
            throw new DomainException(ORDER_IS_NOT_IN_PROGRESS_AND_CANNOT_BE_COMPLETED);
        }

        if (hasShipmentOpen(orderShipmentService)) {
            throw new DomainException(ORDER_HAS_AN_OPEN_SHIPMENT);
        }

        this.orderStatus = new OrderStatus(ORDER_COMPLETED_STATUS, lookupService);
        this.completeDate = ZonedDateTime.now();
        this.completeComments = completeOrderCommand.getComments();
        this.completeEmployeeId = completeOrderCommand.getEmployeeId();
    }

    public void completeOrderAutomatic(){
        this.orderStatus.setStatus(ORDER_COMPLETED_STATUS);
        this.completeDate = ZonedDateTime.now();
    }

    private boolean hasShipmentOpen(OrderShipmentService orderShipmentService) {
        var orderShipment = orderShipmentService.findOneByOrderId(this.getId()).blockOptional();
        return orderShipment.map(shipment -> shipment.getShipmentStatus().equals(ORDER_SHIPMENT_OPEN_STATUS)).orElse(false);
    }

    public boolean canCreateBackOrders(OrderConfigService orderConfigService){
        var backOrderActive = orderConfigService.findBackOrderConfiguration().blockOptional();
        return backOrderActive.orElse(false);
    }

    public Order cancel(CancelOrderCommand cancelOrderCommand , List<Order> orderList){

        if(orderList == null || orderList.isEmpty()){
            throw new DomainException(NO_ORDER_TO_BE_CANCELLED);
        }
        Order orderToBeCancelled;
        if(orderList.size() > 1){
            var backOrders = orderList.stream().filter(order -> order.backOrder && ORDER_OPEN_STATUS.equals(order.getOrderStatus().getOrderStatus())).toList();
            if(backOrders.isEmpty()){
                throw new DomainException(NO_ORDER_TO_BE_CANCELLED);
            }
            orderToBeCancelled = backOrders.getFirst();

        }else{
            orderToBeCancelled = orderList.getFirst();
        }
        cancel(cancelOrderCommand , orderToBeCancelled);

        return orderToBeCancelled;
    }

    private void cancel(CancelOrderCommand cancelOrderCommand , Order order){

        if (ORDER_CANCELLED_STATUS.equals(order.getOrderStatus().getOrderStatus())) {
            throw new DomainException(ORDER_IS_ALREADY_CANCELLED);
        }

        if (!ORDER_OPEN_STATUS.equals(order.getOrderStatus().getOrderStatus())) {
            throw new DomainException(ORDER_IS_NOT_OPEN_AND_CANNOT_BE_CANCELLED);
        }

        order.getOrderStatus().setStatus(ORDER_CANCELLED_STATUS);
        order.setCancelDate(ZonedDateTime.now());
        order.setCancelEmployeeId(cancelOrderCommand.getEmployeeId());
        order.setCancelReason(cancelOrderCommand.getReason());
    }

    public Order createBackOrder(String createEmployeeId,CustomerService customerService , LookupService lookupService , OrderConfigService orderConfigService){

        if(!canCreateBackOrders(orderConfigService)){
            throw new IllegalArgumentException("Back Order cannot be created, configuration is not active");
        }

        var desireShipDate = ofNullable(this.desiredShippingDate).filter(date -> !date.isBefore(LocalDate.now()))
            .map(validDate -> validDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).orElse(null);

        var backOrder =  new Order(
           customerService,
            lookupService,
            null,
            null,
            this.getOrderExternalId().getOrderExternalId(),
            this.getLocationCode(),
            this.getShipmentType().getShipmentType(),
            this.getShippingMethod().getShippingMethod(),
            this.getShippingCustomer().getCode(),
            this.getBillingCustomer().getCode(),
            desireShipDate,
            this.getWillCallPickup() == null ? FALSE : this.getWillCallPickup(),
            this.getPhoneNumber(),
            this.getProductCategory().getProductCategory(),
            this.getComments(),
            ORDER_OPEN_STATUS,
            this.getOrderPriority().getDeliveryType(),
           createEmployeeId,
            null,
            null,
            null);

        backOrder.setBackOrder(TRUE);

        var remainingItems = this.orderItems.stream().filter(orderItem -> orderItem.getQuantityRemaining().compareTo(0) > 0).toList();
        if(remainingItems.isEmpty()){
            throw new IllegalArgumentException("Back Order cannot be created, there is no remaining items");
        }
        remainingItems.forEach(remainingItem -> {
            backOrder.addItem(null,remainingItem.getProductFamily().getProductFamily()
                ,remainingItem.getBloodType().getBloodType(),remainingItem.getQuantityRemaining()
                ,0,remainingItem.getComments(),null,null,orderConfigService);
        });

        return backOrder;

    }

}
