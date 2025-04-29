package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RemoteTVTimerView extends JFrame {
    private JTable deviceTable;
    private DefaultTableModel deviceTableModel;
    private JTextField priceField;
    private JTextField startTimerField;
    private JTextField shutdownTimerField;
    private JTextArea statusArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton addDeviceButton;
    private JButton removeDeviceButton;
    private JButton scanButton;

    public RemoteTVTimerView() {
        setTitle("Remote TV Timer App");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());

        // Device table
        deviceTableModel = new DefaultTableModel(new Object[]{"IP Address", "Device Type"}, 0);
        deviceTable = new JTable(deviceTableModel);
        JScrollPane tableScrollPane = new JScrollPane(deviceTable);

        // Controls panel
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel priceLabel = new JLabel("Price per 30 minutes:");
        priceField = new JTextField("10.00", 10);

        JLabel startTimerLabel = new JLabel("Start Timer (seconds):");
        startTimerField = new JTextField(10);

        JLabel shutdownTimerLabel = new JLabel("Shutdown Timer (seconds):");
        shutdownTimerField = new JTextField(10);

        startButton = new JButton("Start Timers");
        stopButton = new JButton("Stop Timers");
        stopButton.setEnabled(false);

        addDeviceButton = new JButton("Add Device");
        removeDeviceButton = new JButton("Remove Device");
        scanButton = new JButton("Scan Network");

        statusArea = new JTextArea(8, 60);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        controlsPanel.add(priceLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        controlsPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        controlsPanel.add(startTimerLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        controlsPanel.add(startTimerField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        controlsPanel.add(shutdownTimerLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        controlsPanel.add(shutdownTimerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(addDeviceButton);
        buttonPanel.add(removeDeviceButton);
        buttonPanel.add(scanButton);
        controlsPanel.add(buttonPanel, gbc);

        panel.add(tableScrollPane, BorderLayout.NORTH);
        panel.add(controlsPanel, BorderLayout.CENTER);
        panel.add(statusScrollPane, BorderLayout.SOUTH);

        add(panel);
    }

    public JTable getDeviceTable() {
        return deviceTable;
    }

    public DefaultTableModel getDeviceTableModel() {
        return deviceTableModel;
    }

    public JTextField getPriceField() {
        return priceField;
    }

    public JTextField getStartTimerField() {
        return startTimerField;
    }

    public JTextField getShutdownTimerField() {
        return shutdownTimerField;
    }

    public JTextArea getStatusArea() {
        return statusArea;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JButton getAddDeviceButton() {
        return addDeviceButton;
    }

    public JButton getRemoveDeviceButton() {
        return removeDeviceButton;
    }

    public JButton getScanButton() {
        return scanButton;
    }
}
