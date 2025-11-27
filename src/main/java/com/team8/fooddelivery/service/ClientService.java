package com.team8.fooddelivery.service;

import com.team8.fooddelivery.model.Address;
import com.team8.fooddelivery.model.client.Client;
import java.util.List;

public interface ClientService {
    Client register(String phone, String password, String name, String email, Address address);
    Client update(Long clientId, String name, String email, Address address);
    boolean deactivate(Long clientId);
    boolean activate(Long clientId);
    Client getById(Long clientId);
    List<String> getOrderHistory(Long clientId);
    List<Client> listAll();
    void addOrderHistoryEntry(Long clientId, String entry);
    boolean authenticate(String phone, String password);
}


