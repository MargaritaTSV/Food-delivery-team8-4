package com.team8.fooddelivery.model.product;

import lombok.*;
import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  private Long productId;
  private String name;
  private String description;
  private Double weight;
  private Double price;
  private ProductCategory category;
  private Boolean available = true;
  private Duration cookingTimeMinutes;
}