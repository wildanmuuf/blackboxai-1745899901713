import model.DeviceModel;
import view.RemoteTVTimerView;
import controller.RemoteTVTimerController;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DeviceModel model = new DeviceModel("jdbc:mysql://localhost:3306/remote_tv", "root", "password");
                RemoteTVTimerView view = new RemoteTVTimerView();
                new RemoteTVTimerController(model, view);
                view.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start application: " + e.getMessage());
            }
        });
    }
}
