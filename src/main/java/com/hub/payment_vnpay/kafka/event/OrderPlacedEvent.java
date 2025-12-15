package com.hub.payment_vnpay.kafka.event;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class OrderPlacedEvent {

    private Long orderId;
    private String studentId;
    private List<Long> courseIds;
    private BigDecimal totalPrice;
    private String orderStatus;
    private OffsetDateTime createdOn;
    private String paymentMethod;

    public OrderPlacedEvent() {
    }
    public OrderPlacedEvent(Long orderId, String studentId, List<Long> courseIds, BigDecimal totalPrice, String orderStatus, OffsetDateTime createdOn, String paymentMethod) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.courseIds = courseIds;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.createdOn = createdOn;
        this.paymentMethod = paymentMethod;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


}
