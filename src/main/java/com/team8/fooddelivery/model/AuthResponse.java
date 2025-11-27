<<<<<<<< HEAD:src/main/java/com/team8/fooddelivery/util/AuthResponse.java
package com.team8.fooddelivery.util;
========
package com.team8.fooddelivery.model;
>>>>>>>> 03f38c28dfcdba7f46bf2708ff4cd0e64408a386:src/main/java/com/team8/fooddelivery/model/AuthResponse.java

import com.team8.fooddelivery.model.client.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long clientId;
    private String authToken;
    private ClientStatus status;
}

