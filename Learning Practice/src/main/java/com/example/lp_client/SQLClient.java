package com.example.lp_client;

import java.sql.*;

public class SQLClient {

    private Connection connection;

    public void connect(String url, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(url, user, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public boolean isConnected() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public void insertData(int id, String applicant, String manager,
                           String address, String matter, String contents,
                           String resolution, String additionalInfo) throws SQLException {
        String sql = "INSERT INTO requests (id, applicant, manager, address, matter, contents, " +
                "resolution, status, additional_info) VALUES (?, ?, ?, ?, ?, ?, ?, null, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Office1",
                "postgres", "ChaosNova2020");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setString(2, applicant);
            pstmt.setString(3, manager);
            pstmt.setString(4, address);
            pstmt.setString(5, matter);
            pstmt.setString(6, contents);
            pstmt.setString(7, resolution);
            pstmt.setString(8, additionalInfo);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public String getEachID(){
        StringBuilder data = new StringBuilder();
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Office1",
                "postgres", "ChaosNova2020");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM requests WHERE status IS NULL")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                data.append(id);
                data.append("; ");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении данных из БД: " + e.getMessage());
        }
        return data.toString();
    }
    public String getCurrentID(int id) throws SQLException {
        String sql = "SELECT * FROM requests WHERE id = ?";
        StringBuilder recordString = new StringBuilder();

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Office1",
                "postgres", "ChaosNova2020");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = rs.getString(i);
                    recordString.append(columnName).append(": ").append(columnValue).append("; ");
                }
            } else {
                throw new SQLException("Запись с ID " + id + " не найдена.");
            }
        }

        return recordString.toString();
    }
}
