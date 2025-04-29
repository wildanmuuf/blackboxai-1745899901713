package controller;

import model.DeviceModel;
import model.DeviceModel.Device;
import view.RemoteTVTimerView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteTVTimerController {
    private DeviceModel model;
    private RemoteTVTimerView view;

    private Timer timer;
    private TimerTask startTask;
    private TimerTask shutdownTask;

    public RemoteTVTimerController(DeviceModel model, RemoteTVTimerView view) {
        this.model = model;
        this.view = view;

        initView();
        initController();
    }

    private void initView() {
        loadDevicesToView();
    }

    private void initController() {
        view.getAddDeviceButton().addActionListener(e -> addDevice());
        view.getRemoveDeviceButton().addActionListener(e -> removeDevice());
        view.getScanButton().addActionListener(e -> scanNetwork());
        view.getStartButton().addActionListener(e -> startTimers());
        view.getStopButton().addActionListener(e -> stopTimers());
    }

    private void loadDevicesToView() {
        try {
            DefaultTableModel tableModel = view.getDeviceTableModel();
            tableModel.setRowCount(0);
            List<Device> devices = model.getAllDevices();
            for (Device d : devices) {
                tableModel.addRow(new Object[]{d.getIp(), d.getType()});
            }
            appendStatus("Loaded devices from database.");
        } catch (SQLException e) {
            appendStatus("Error loading devices: " + e.getMessage());
        }
    }

    private void addDevice() {
        JTextField ipField = new JTextField();
        String[] deviceTypes = {"Android TV", "PlayStation 4"};
        JComboBox<String> typeCombo = new JComboBox<>(deviceTypes);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("IP Address:"));
        panel.add(ipField);
        panel.add(new JLabel("Device Type:"));
        panel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(view, panel, "Add Device",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if (!ip.isEmpty() && type != null) {
                try {
                    model.addDevice(ip, type);
                    view.getDeviceTableModel().addRow(new Object[]{ip, type});
                    appendStatus("Added device: " + ip + " (" + type + ")");
                } catch (SQLException e) {
                    appendStatus("Error adding device: " + e.getMessage());
                }
            } else {
                appendStatus("IP and device type must be provided.");
            }
        }
    }

    private void removeDevice() {
        int selectedRow = view.getDeviceTable().getSelectedRow();
        if (selectedRow >= 0) {
            String ip = (String) view.getDeviceTableModel().getValueAt(selectedRow, 0);
            try {
                model.removeDevice(ip);
                view.getDeviceTableModel().removeRow(selectedRow);
                appendStatus("Removed device: " + ip);
            } catch (SQLException e) {
                appendStatus("Error removing device: " + e.getMessage());
            }
        } else {
            appendStatus("Please select a device to remove.");
        }
    }

    private void scanNetwork() {
        appendStatus("Starting network scan...");
        new Thread(() -> {
            for (int i = 1; i <= 254; i++) {
                String ip = "192.168.1." + i;
                try {
                    InetAddress address = InetAddress.getByName(ip);
                    if (address.isReachable(100)) {
                        appendStatus("Found device: " + ip);
                        SwingUtilities.invokeLater(() -> {
                            DefaultTableModel modelTable = view.getDeviceTableModel();
                            boolean exists = false;
                            for (int row = 0; row < modelTable.getRowCount(); row++) {
                                if (modelTable.getValueAt(row, 0).equals(ip)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                try {
                                    model.addDevice(ip, "Android TV");
                                    modelTable.addRow(new Object[]{ip, "Android TV"});
                                    appendStatus("Added device from scan: " + ip);
                                } catch (SQLException e) {
                                    appendStatus("Error adding scanned device: " + e.getMessage());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignore unreachable hosts
                }
            }
            appendStatus("Network scan completed.");
        }).start();
    }

    private void startTimers() {
        int startSeconds;
        int shutdownSeconds;
        double pricePer30Min;

        try {
            startSeconds = Integer.parseInt(view.getStartTimerField().getText().trim());
            shutdownSeconds = Integer.parseInt(view.getShutdownTimerField().getText().trim());
            pricePer30Min = Double.parseDouble(view.getPriceField().getText().trim());
        } catch (NumberFormatException e) {
            appendStatus("Please enter valid numbers for timers and price.");
            return;
        }

        if (view.getDeviceTableModel().getRowCount() == 0) {
            appendStatus("No devices configured.");
            return;
        }

        timer = new Timer();

        if (startSeconds > 0) {
            startTask = new TimerTask() {
                public void run() {
                    appendStatus("Start timer triggered.");
                    for (int i = 0; i < view.getDeviceTableModel().getRowCount(); i++) {
                        String ip = (String) view.getDeviceTableModel().getValueAt(i, 0);
                        String type = (String) view.getDeviceTableModel().getValueAt(i, 1);
                        sendWakeOnLan(ip);
                        appendStatus("Sent Wake-on-LAN to " + type + ": " + ip);
                        if ("Android TV".equals(type)) {
                            sendUnlockCommandToAPK(ip);
                        }
                        logUsageStart(ip);
                    }
                }
            };
            timer.schedule(startTask, startSeconds * 1000L);
            appendStatus("Start timer set for " + startSeconds + " seconds.");
        }

        if (shutdownSeconds > 0) {
            shutdownTask = new TimerTask() {
                public void run() {
                    appendStatus("Shutdown timer triggered.");
                    for (int i = 0; i < view.getDeviceTableModel().getRowCount(); i++) {
                        String ip = (String) view.getDeviceTableModel().getValueAt(i, 0);
                        String type = (String) view.getDeviceTableModel().getValueAt(i, 1);
                        if ("Android TV".equals(type)) {
                            sendShutdownCommandToAndroidTV(ip);
                            sendLockCommandToAPK(ip);
                        } else {
                            appendStatus("Shutdown command for PlayStation 4 is not implemented.");
                        }
                        logUsageStop(ip, pricePer30Min);
                    }
                }
            };
            timer.schedule(shutdownTask, shutdownSeconds * 1000L);
            appendStatus("Shutdown timer set for " + shutdownSeconds + " seconds.");
        }

        view.getStartButton().setEnabled(false);
        view.getStopButton().setEnabled(true);
    }

    private void stopTimers() {
        if (timer != null) {
            timer.cancel();
            appendStatus("Timers stopped.");
        }
        view.getStartButton().setEnabled(true);
        view.getStopButton().setEnabled(false);
    }

    private void logUsageStart(String ip) {
        try {
            model.logUsageStart(ip);
            appendStatus("Logged start time for device: " + ip);
        } catch (SQLException e) {
            appendStatus("Error logging start time: " + e.getMessage());
        }
    }

    private void logUsageStop(String ip, double pricePer30Min) {
        try {
            model.logUsageStop(ip, pricePer30Min);
            appendStatus("Logged stop time for device: " + ip);
        } catch (SQLException e) {
            appendStatus("Error logging stop time: " + e.getMessage());
        }
    }

    // Placeholder method to send unlock command to the Android TV APK
    private void sendUnlockCommandToAPK(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", ip + ":5555", "shell",
                    "am", "broadcast", "-a", "com.example.smartlock.UNLOCK");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendStatus("APK Unlock: " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            appendStatus("Failed to send unlock command to APK: " + e.getMessage());
        }
    }

    // Placeholder method to send lock command to the Android TV APK
    private void sendLockCommandToAPK(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", ip + ":5555", "shell",
                    "am", "broadcast", "-a", "com.example.smartlock.LOCK");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendStatus("APK Lock: " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            appendStatus("Failed to send lock command to APK: " + e.getMessage());
        }
    }

    private void sendWakeOnLan(String ipAddress) {
        try {
            byte[] macBytes = getMacAddress(ipAddress);
            if (macBytes == null) {
                appendStatus("Could not get MAC address for IP: " + ipAddress);
                return;
            }
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            appendStatus("Failed to send Wake-on-LAN packet: " + e.getMessage());
        }
    }

    private byte[] getMacAddress(String ip) {
        try {
            Process p = Runtime.getRuntime().exec("arp -n " + ip);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains(ip)) {
                    String[] tokens = line.split(" ");
                    for (String token : tokens) {
                        if (token.matches("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")) {
                            return hexStringToByteArray(token);
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            appendStatus("Error getting MAC address: " + e.getMessage());
        }
        return null;
    }

    private byte[] hexStringToByteArray(String s) {
        String[] hex = s.split(":");
        byte[] bytes = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return bytes;
    }

    private void sendShutdownCommandToAndroidTV(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", ip + ":5555", "shell", "reboot", "-p");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendStatus(line);
            }
            process.waitFor();
        } catch (Exception e) {
            appendStatus("Failed to send shutdown command via ADB: " + e.getMessage());
        }
    }

    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            view.getStatusArea().append(message + "\n");
            view.getStatusArea().setCaretPosition(view.getStatusArea().getDocument().getLength());
        });
    }
}
