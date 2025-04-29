package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RemoteTVTimerView extends JFrame {
    // Getter methods for UI components

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
