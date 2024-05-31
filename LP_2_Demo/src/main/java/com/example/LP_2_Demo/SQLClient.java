package com.example.LP_2_Demo;

import java.sql.*;

public class SQLClient {

    //Имена баз данных
    public final String FIRST_HOST = "jdbc:postgresql://localhost:5432/Office1";
    public final String SECOND_HOST = "jdbc:postgresql://localhost:5432/Office2";

    //Имена таблиц
    public final String REQUESTS = "requests";
    public final String PRINTED_REQUESTS = "printed_requests";
    public final String SEEN_REQUESTS = "seen_requests";

    //Данные пользователя
    private final String PASSWORD = "ChaosNova2020";
    private final String USER = "postgres";

    //Запрос на отправку данных
    public void insertData(String dataBase, String tableName, int id, String applicant, String manager,
                           String address, String matter, String contents,
                           String resolution, Boolean status, String additionalInfo) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (id, applicant, manager, address, matter, contents, " +
                "resolution, status, additional_info) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //Устанавливаем типы вводимых данных
            pstmt.setInt(1, id);
            pstmt.setString(2, applicant);
            pstmt.setString(3, manager);
            pstmt.setString(4, address);
            pstmt.setString(5, matter);
            pstmt.setString(6, contents);
            pstmt.setString(7, resolution);
            //Устанавливаем статус отдельно
            if (status != null)
                pstmt.setBoolean(8, status);
            else
                pstmt.setNull(8, Types.BOOLEAN);
            //Устанавливаем дополнительную информацию
            pstmt.setString(9, additionalInfo);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    //Обновление данных в БД (после рассмотрения руководителем)
    void updateData(String dataBase, String tableName, String resolution,
                    boolean status, String info, int id) {
        String sql = "UPDATE " + tableName + " SET resolution = (?), status = (?), additional_info = (?) WHERE id = (?)";
        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //Подготовка данных к отправке
            pstmt.setString(1, resolution);
            pstmt.setBoolean(2, status);
            pstmt.setString(3, info);
            pstmt.setInt(4, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    //Получаем все id из БД
    public String getEachID(String dataBase, String tableName, boolean isDataSeen) {
        String sql = "SELECT id FROM " + tableName + " WHERE status";
        if (!isDataSeen) sql += " IS NULL";
        else sql += " IS NOT NULL";
        StringBuilder data = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            //Пока запрос не пустой, формируем строку
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

    //Получаем информацию по id в виде "имя столбца1: значение;..."
    public String getCurrentID(String dataBase, String tableName, int id) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        StringBuilder recordString = new StringBuilder();

        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            //Пока запрос не пустой, формируем строку
            if (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(i);

                    if (columnValue == null) {
                        recordString.append(columnName).append(": ").append("null").append("; ");
                    } else if (columnValue instanceof Boolean) {
                        recordString.append(columnName).append(": ").append(columnValue).append("; ");
                    } else if (columnValue instanceof Number) {
                        recordString.append(columnName).append(": ").append(columnValue).append("; ");
                    } else {
                        recordString.append(columnName).append(": ").append(columnValue).append("; ");
                    }
                }
            } else {
                throw new SQLException("Запись с ID " + id + " не найдена.");
            }
        }

        return recordString.toString();
    }

    //Проверяем наличие id в БД
    public boolean isIdExists(String dataBase, String tableName, int id) {
        String sql = "SELECT id FROM " + tableName + " WHERE id = (?)";
        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Проверка статуса заявки
    public String getUserInformation(String dataBase, String tableName, String applicant) throws SQLException {
        String data;
        String sql = "SELECT id, resolution, status, additional_info FROM " + tableName + " WHERE applicant = ?";
        try (Connection conn = DriverManager.getConnection(dataBase, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, applicant);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String resolution = rs.getString("resolution");
                Boolean statusValue; // Инициализируем значение по умолчанию
                if (rs.getObject("status") != null) {
                    statusValue = rs.getBoolean("status");
                }
                else statusValue = null;
                String additionalInfo = rs.getString("additional_info");

                String statusString = "";

                if(statusValue == null){
                    statusString = "Создано";
                }
                else if(statusValue){
                    statusString = "Рассмотрено";
                }
                else statusString = "Отклонено";

                data = "ID: " + id + "; Резолюция: " + resolution + "; Статус: " + statusString +
                        "; Дополнительная информация: " + additionalInfo;
                return data;
            }
            else{
                return "Данное обращение отсутствует в БД. Возможно, оно еще не было обработано" +
                        " или данные введены некорректно";
            }
        }
    }
}