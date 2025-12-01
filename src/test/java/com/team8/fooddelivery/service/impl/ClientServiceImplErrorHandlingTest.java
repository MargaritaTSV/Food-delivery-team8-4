package com.team8.fooddelivery.service.impl;

import com.team8.fooddelivery.model.client.Client;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ClientServiceImplErrorHandlingTest {

    @Mock
    private CartServiceImpl cartService;
    @Mock
    private ClientRepository clientRepository;

    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientServiceImpl(cartService);
        // Use reflection to inject mocked repository
        java.lang.reflect.Field repoField = ClientServiceImpl.class.getDeclaredField("clientRepository");
        repoField.setAccessible(true);
        repoField.set(clientService, clientRepository);
    }

    @Test
    @DisplayName("getByPhone: Should return null on SQLException")
    void testGetByPhone_SQLException() throws SQLException {
        when(clientRepository.findByPhone(anyString())).thenThrow(new SQLException("DB Error"));
        
        Client result = clientService.getByPhone("+79001234567");
        
        assertNull(result);
        verify(clientRepository, times(1)).findByPhone(anyString());
    }

    @Test
    @DisplayName("getByEmail: Should return null on SQLException")
    void testGetByEmail_SQLException() throws SQLException {
        when(clientRepository.findByEmail(anyString())).thenThrow(new SQLException("DB Error"));
        
        Client result = clientService.getByEmail("test@example.com");
        
        assertNull(result);
        verify(clientRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("listAll: Should return empty list on SQLException")
    void testListAll_SQLException() throws SQLException {
        when(clientRepository.findAll()).thenThrow(new SQLException("DB Error"));
        
        var result = clientService.listAll();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getOrderHistory: Should return empty list on SQLException")
    void testGetOrderHistory_SQLException() throws SQLException {
        when(clientRepository.findById(anyLong())).thenThrow(new SQLException("DB Error"));
        
        var result = clientService.getOrderHistory(1L);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("getById: Should return null on SQLException")
    void testGetById_SQLException() throws SQLException {
        when(clientRepository.findById(anyLong())).thenThrow(new SQLException("DB Error"));
        
        Client result = clientService.getById(1L);
        
        assertNull(result);
        verify(clientRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("getByPhone: Should return null for non-existent phone")
    void testGetByPhone_NotFound() throws SQLException {
        when(clientRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        Client result = clientService.getByPhone("+79999999999");
        assertNull(result);
    }

    @Test
    @DisplayName("getByEmail: Should return null for non-existent email")
    void testGetByEmail_NotFound() throws SQLException {
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Client result = clientService.getByEmail("nonexistent@example.com");
        assertNull(result);
    }
}

