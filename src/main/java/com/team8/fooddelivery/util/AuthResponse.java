package com.team8.fooddelivery.util;

import com.team8.fooddelivery.model.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long clientId;
    private String authToken;
    private ClientStatus status;
}

