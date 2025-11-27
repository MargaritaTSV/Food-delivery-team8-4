package com.team8.fooddelivery.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
  private Long paymentMethodId;
  private Long clientId;
  private String methodType;
  private String cardNumber;
  private String cardHolderName;
  private String expiryDate;
  private String cvv;
  private Boolean isDefault = false;
  private Instant createdAt = Instant.now();
  private Boolean isActive = true;

  public PaymentMethod(Long clientId, String methodType, String cardNumber,
      String cardHolderName, String expiryDate, String cvv) {
    this.clientId = clientId;
    this.methodType = methodType;
    this.cardNumber = cardNumber;
    this.cardHolderName = cardHolderName;
    this.expiryDate = expiryDate;
    this.cvv = cvv;
    this.createdAt = Instant.now();
    this.isActive = true;
    this.isDefault = false;
  }
}
