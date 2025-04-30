package view.UserView.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.User;
import repository.DatabaseConnection;
import view.UserView.UserDashboardView;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfilePanel extends JPanel {
	private static final Color BG_COLOR = new Color(245, 245, 245);
	private static final Color PRIMARY_COLOR = new Color(0, 102, 102);
	private static final Color ACCENT_COLOR = new Color(255, 153, 0);
	 
	private User loggedInUser;
	private JTable profileTable;
	private DefaultTableModel model; 
	private JFrame parentFrame;
	 
    public ProfilePanel(User loggedInUser,UserDashboardView parentFrame) {
    	this.loggedInUser = loggedInUser;
    	this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }
    

	public void initComponents() {
    	 // Panel công cụ
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBackground(Color.WHITE);
        toolPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)));
        // bảng thông tin cá nhân
        String[] ColumnNames = {"ID","Họ và tên","Email","Thao tác"};
        model = new DefaultTableModel(ColumnNames,0) {
        	 @Override
             public boolean isCellEditable(int row, int column) {
                 return column == 3; // Chỉ cho phép chỉnh sửa cột thao tác
             }
        };
        profileTable = new JTable(model);
        profileTable.setRowHeight(40);
        profileTable.getTableHeader().setReorderingAllowed(false);
        profileTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        // Scroll pane cho table
        JScrollPane scrollPane = new JScrollPane(profileTable);
        
        add(toolPanel,BorderLayout.NORTH);
        add(scrollPane,BorderLayout.CENTER);
        
        //load dữ liệu
        loadDomainData();
        
        profileTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
       	 @Override
       	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
       	                                                   boolean hasFocus, int row, int column) {
       	        JButton button = new JButton("Chỉnh sửa");
       	        button.setForeground(Color.BLUE);
       	        button.setBackground(Color.WHITE);
       	        button.setFocusPainted(false);
       	        button.setBorder(BorderFactory.createEmptyBorder());
       	        return button;
       	    }
      });
        
        profileTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton button = new JButton("Chỉnh sửa");
            private boolean isPushed = false;

            {
                button.setForeground(Color.BLUE);
                button.setBackground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder());

                button.addActionListener(e -> {
                    int selectedRow = profileTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String hoTen = (String) model.getValueAt(selectedRow, 1);
                        String email = (String) model.getValueAt(selectedRow, 2);

                        // Mở JDialog chỉnh sửa
                        JDialog editDialog = new JDialog(parentFrame, "Chỉnh sửa thông tin", true);
                        editDialog.setSize(400, 250);
                        editDialog.setLocationRelativeTo(parentFrame);
                        editDialog.setLayout(new BorderLayout(10, 10));

                        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
                        inputPanel.setBorder(new EmptyBorder(20, 20, 0, 20));
                        inputPanel.add(new JLabel("Họ và tên:"));
                        JTextField nameField = new JTextField(hoTen);
                        inputPanel.add(nameField);
                        inputPanel.add(new JLabel("Email:"));
                        JTextField emailField = new JTextField(email);
                        inputPanel.add(emailField);

                        JPanel buttonPanel = new JPanel();
                        JButton saveBtn = new JButton("Lưu");
                        JButton cancelBtn = new JButton("Hủy");
                        buttonPanel.add(saveBtn);
                        buttonPanel.add(cancelBtn);

                        editDialog.add(inputPanel, BorderLayout.CENTER);
                        editDialog.add(buttonPanel, BorderLayout.SOUTH);

                        saveBtn.addActionListener(ev -> {
                        	String newName = nameField.getText().trim();
                            String newEmail = emailField.getText().trim();

                            if (newName.isEmpty() || newEmail.isEmpty()) {
                                JOptionPane.showMessageDialog(editDialog, "Không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Cập nhật đối tượng user hiện tại
                            loggedInUser.setFullName(newName);
                            loggedInUser.setEmail(newEmail);

                            // Gọi DAO để cập nhật CSDL
                            try {
                                String sql = "UPDATE Users SET fullname = ?, email = ? WHERE id = ?";
                                Connection conn = DatabaseConnection.getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ps.setString(1, newName);
                                ps.setString(2, newEmail);
                                ps.setInt(3, loggedInUser.getId());

                                int rowsUpdated = ps.executeUpdate();
                                if (rowsUpdated > 0) {
                                    model.setValueAt(newName, selectedRow, 1);
                                    model.setValueAt(newEmail, selectedRow, 2);
                                    JOptionPane.showMessageDialog(editDialog, "Cập nhật thành công!");
                                    editDialog.dispose();
                                } else {
                                    JOptionPane.showMessageDialog(editDialog, "Không thể cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }

                                ps.close();
                                conn.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(editDialog, "Lỗi khi cập nhật dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        });

                        cancelBtn.addActionListener(ev -> editDialog.dispose());

                        editDialog.setVisible(true);
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                isPushed = true;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                isPushed = false;
                return "";
            }

            @Override
            public boolean stopCellEditing() {
                isPushed = false;
                return super.stopCellEditing();
            }
        });

    }
    
    public void loadDomainData() {
    	model.setRowCount(0);
    	try {
    		int IDUser = loggedInUser.getId();
    		String fullNameUser = loggedInUser.getFullName();
    		String emailUser = loggedInUser.getEmail();
    		model.addRow(new Object[]{
    				IDUser,
    				fullNameUser,
    				emailUser,
    				""
    		});
    		
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}