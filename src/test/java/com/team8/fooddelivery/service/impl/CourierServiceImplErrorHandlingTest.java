package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.courier.Courier;
import com.team8.fooddelivery.model.order.Order;
import com.team8.fooddelivery.repository.CourierRepository;
import com.team8.fooddelivery.repository.OrderRepository;
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

class CourierServiceImplErrorHandlingTest {

    @Mock
    private CourierRepository courierRepository;
    @Mock
    private OrderRepository orderRepository;

    private CourierServiceImpl courierService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        courierService = new CourierServiceImpl();
        // Use reflection to inject mocked repositories
        java.lang.reflect.Field courierRepoField = CourierServiceImpl.class.getDeclaredField("courierRepository");
        courierRepoField.setAccessible(true);
        courierRepoField.set(courierService, courierRepository);

        java.lang.reflect.Field orderRepoField = CourierServiceImpl.class.getDeclaredField("orderRepository");
        orderRepoField.setAccessible(true);
        orderRepoField.set(courierService, orderRepository);
    }

    @Test
    @DisplayName("getCourierById: Should return null on SQLException")
    void testGetCourierById_SQLException() throws SQLException {
        when(courierRepository.findById(anyLong())).thenThrow(new SQLException("DB Error"));
        
        Courier result = courierService.getCourierById(1L);
        
        assertNull(result);
        verify(courierRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("getOrderHistory: Should return empty list on SQLException")
    void testGetOrderHistory_SQLException() throws SQLException {
        when(orderRepository.findByCourierId(anyLong())).thenThrow(new SQLException("DB Error"));
        
        var result = courierService.getOrderHistory(1L);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByCourierId(anyLong());
    }

    @Test
    @DisplayName("getCourierById: Should return null for non-existent courier")
    void testGetCourierById_NotFound() throws SQLException {
        when(courierRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        Courier result = courierService.getCourierById(999L);
        
        assertNull(result);
        verify(courierRepository, times(1)).findById(anyLong());
    }
}

