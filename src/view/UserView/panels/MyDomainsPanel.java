package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class MyDomainsPanel extends JPanel {
    public MyDomainsPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Tên miền của tôi", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}