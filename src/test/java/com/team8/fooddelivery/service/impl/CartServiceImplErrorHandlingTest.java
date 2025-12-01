package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.product.Cart;
import com.team8.fooddelivery.repository.CartRepository;
import com.team8.fooddelivery.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CartServiceImplErrorHandlingTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ClientRepository clientRepository;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        cartService = new CartServiceImpl();
        // Use reflection to inject mocked repositories
        java.lang.reflect.Field cartRepoField = CartServiceImpl.class.getDeclaredField("cartRepository");
        cartRepoField.setAccessible(true);
        cartRepoField.set(cartService, cartRepository);

        java.lang.reflect.Field clientRepoField = CartServiceImpl.class.getDeclaredField("clientRepository");
        clientRepoField.setAccessible(true);
        clientRepoField.set(cartService, clientRepository);
    }

    @Test
    @DisplayName("getCartForClient: Should return null on SQLException")
    void testGetCartForClient_SQLException() throws SQLException {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new com.team8.fooddelivery.model.client.Client()));
        when(cartRepository.findByClientId(anyLong())).thenThrow(new SQLException("DB Error"));
        
        Cart result = cartService.getCartForClient(1L);
        
        assertNull(result);
        verify(cartRepository, times(1)).findByClientId(anyLong());
    }
}

