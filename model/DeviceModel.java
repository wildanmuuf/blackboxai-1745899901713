package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceModel {
    private Connection connection;

    public DeviceModel(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        initDB();
    }

    private void initDB() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS devices (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "ip VARCHAR(50) NOT NULL," +
                "type VARCHAR(20) NOT NULL" +
                ")");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usage_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "device_ip VARCHAR(50) NOT NULL," +
                "start_time TIMESTAMP," +
                "stop_time TIMESTAMP," +
                "duration_minutes INT," +
                "cost DECIMAL(10,2)" +
                ")");
        stmt.close();
    }

    public List<Device> getAllDevices() throws SQLException {
        List<Device> devices = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ip, type FROM devices");
        while (rs.next()) {
            devices.add(new Device(rs.getString("ip"), rs.getString("type")));
        }
        rs.close();
        stmt.close();
        return devices;
    }

    public void addDevice(String ip, String type) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO devices (ip, type) VALUES (?, ?)");
        ps.setString(1, ip);
        ps.setString(2, type);
        ps.executeUpdate();
        ps.close();
    }

    public void removeDevice(String ip) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM devices WHERE ip = ?");
        ps.setString(1, ip);
        ps.executeUpdate();
        ps.close();
    }

    public void logUsageStart(String ip) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO usage_logs (device_ip, start_time) VALUES (?, NOW())");
        ps.setString(1, ip);
        ps.executeUpdate();
        ps.close();
    }

    public void logUsageStop(String ip, double pricePer30Min) throws SQLException {
        PreparedStatement psSelect = connection.prepareStatement(
                "SELECT id, start_time FROM usage_logs WHERE device_ip = ? AND stop_time IS NULL ORDER BY start_time DESC LIMIT 1");
        psSelect.setString(1, ip);
        ResultSet rs = psSelect.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("id");
            Timestamp startTime = rs.getTimestamp("start_time");
            Timestamp stopTime = new Timestamp(System.currentTimeMillis());
            long durationMillis = stopTime.getTime() - startTime.getTime();
            int durationMinutes = (int) Math.ceil(durationMillis / 60000.0);
            double cost = (durationMinutes / 30.0) * pricePer30Min;

            PreparedStatement psUpdate = connection.prepareStatement(
                    "UPDATE usage_logs SET stop_time = ?, duration_minutes = ?, cost = ? WHERE id = ?");
            psUpdate.setTimestamp(1, stopTime);
            psUpdate.setInt(2, durationMinutes);
            psUpdate.setDouble(3, cost);
            psUpdate.setInt(4, id);
            psUpdate.executeUpdate();
            psUpdate.close();
        }
        rs.close();
        psSelect.close();
    }

    public static class Device {
        private String ip;
        private String type;

        public Device(String ip, String type) {
            this.ip = ip;
            this.type = type;
        }

        public String getIp() {
            return ip;
        }

        public String getType() {
            return type;
        }
    }
}
