package com.team8.fooddelivery.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDataSource {
  private final String url;
  private final String username;
  private final String password;
  private int activeConnections = 0;

  public SimpleDataSource(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public Connection getConnection() throws SQLException {
    activeConnections++;
    return DriverManager.getConnection(url, username, password);
  }

  public int getActiveConnections() {
    return activeConnections;
  }
}