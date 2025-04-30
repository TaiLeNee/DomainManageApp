package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class SearchDomainPanel extends JPanel {
    public SearchDomainPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Tìm kiếm tên miền", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}