import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.io.*;
import java.lang.ProcessHandle;

public class RemoteTVTimerApp extends JFrame {
    private JTextField tvIpField;
    private JTextField ps4IpField;
    private JTextField startTimerField;
    private JTextField shutdownTimerField;
    private JTextArea statusArea;
    private JButton startButton;
    private JButton stopButton;

    private Timer timer;
    private TimerTask startTask;
    private TimerTask shutdownTask;

    public RemoteTVTimerApp() {
        setTitle("Remote TV Timer App");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel tvIpLabel = new JLabel("Android TV IP:");
        tvIpField = new JTextField(15);

        JLabel ps4IpLabel = new JLabel("PlayStation 4 IP:");
        ps4IpField = new JTextField(15);

        JLabel startTimerLabel = new JLabel("Start Timer (seconds):");
        startTimerField = new JTextField(10);

        JLabel shutdownTimerLabel = new JLabel("Shutdown Timer (seconds):");
        shutdownTimerField = new JTextField(10);

        startButton = new JButton("Start Timers");
        stopButton = new JButton("Stop Timers");
        stopButton.setEnabled(false);

        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusArea);

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(tvIpLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(tvIpField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(ps4IpLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(ps4IpField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(startTimerLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(startTimerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(shutdownTimerLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(shutdownTimerField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        add(panel);

        startButton.addActionListener(e -> startTimers());
        stopButton.addActionListener(e -> stopTimers());
    }

    private void startTimers() {
        String tvIp = tvIpField.getText().trim();
        String ps4Ip = ps4IpField.getText().trim();
        int startSeconds;
        int shutdownSeconds;

        try {
            startSeconds = Integer.parseInt(startTimerField.getText().trim());
            shutdownSeconds = Integer.parseInt(shutdownTimerField.getText().trim());
        } catch (NumberFormatException e) {
            appendStatus("Please enter valid numbers for timers.");
            return;
        }

        if (tvIp.isEmpty() && ps4Ip.isEmpty()) {
            appendStatus("Please enter at least one device IP.");
            return;
        }

        timer = new Timer();

        if (startSeconds > 0) {
            startTask = new TimerTask() {
                public void run() {
                    appendStatus("Start timer triggered.");
                    if (!tvIp.isEmpty()) {
                        sendWakeOnLan(tvIp);
                        appendStatus("Sent Wake-on-LAN to Android TV: " + tvIp);
                        sendUnlockCommandToAPK(tvIp);
                    }
                    if (!ps4Ip.isEmpty()) {
                        sendWakeOnLan(ps4Ip);
                        appendStatus("Sent Wake-on-LAN to PlayStation 4: " + ps4Ip);
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
                    if (!tvIp.isEmpty()) {
                        sendShutdownCommandToAndroidTV(tvIp);
                        appendStatus("Sent shutdown command to Android TV: " + tvIp);
                        sendLockCommandToAPK(tvIp);
                    }
                    if (!ps4Ip.isEmpty()) {
                        // Placeholder for PS4 shutdown
                        appendStatus("Shutdown command for PlayStation 4 is not implemented.");
                    }
                }
            };
            timer.schedule(shutdownTask, shutdownSeconds * 1000L);
            appendStatus("Shutdown timer set for " + shutdownSeconds + " seconds.");
        }

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopTimers() {
        if (timer != null) {
            timer.cancel();
            appendStatus("Timers stopped.");
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    // Placeholder method to send unlock command to the Android TV APK
    private void sendUnlockCommandToAPK(String ip) {
        try {
            // Example ADB command to send broadcast intent to APK
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
            // Example ADB command to send broadcast intent to APK
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
            // Requires adb installed and in PATH, and Android TV with debugging enabled
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RemoteTVTimerApp app = new RemoteTVTimerApp();
            app.setVisible(true);
        });
    }
}
