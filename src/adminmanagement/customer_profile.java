package adminmanagement;

import DatabaseConnection.ConnectionDB;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class customer_profile extends JFrame implements ActionListener {
	private JTextField searchText;
	private JTable table;
	private JButton logout, menu1, search, reset;
	private Connection conn;

	private final Font f1 = new Font("Times New Roman", Font.BOLD, 17);
	private final Font f3 = new Font("Times New Roman", Font.PLAIN, 15);

	public customer_profile() {
		conn = ConnectionDB.getConnection();

		Container con = getContentPane();
		con.setLayout(null);

		JLabel title = new JLabel("Archim's TechCorner");
		title.setBounds(20, 10, 200, 40);
		con.add(title);

		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setBounds(850, 10, 100, 35);
		con.add(logout);

		menu1 = new JButton("Back to Menu");
		menu1.addActionListener(this);
		menu1.setBounds(700, 10, 130, 35);
		con.add(menu1);

		JLabel cusprof = new JLabel("-------------------------------------------CUSTOMER PROFILE--------------------------------------");
		cusprof.setFont(f1);
		cusprof.setBounds(10, 60, 950, 40);
		con.add(cusprof);

		JLabel searchLabel = new JLabel("Search:");
		searchLabel.setFont(f3);
		searchLabel.setBounds(40, 115, 70, 25);
		con.add(searchLabel);

		searchText = new JTextField();
		searchText.setFont(f3);
		searchText.setBounds(110, 115, 260, 25);
		con.add(searchText);

		search = new JButton("Search");
		search.addActionListener(this);
		search.setBounds(390, 112, 90, 30);
		con.add(search);

		reset = new JButton("Reset");
		reset.addActionListener(this);
		reset.setBounds(500, 112, 90, 30);
		con.add(reset);

		table = new JTable();
		table.setRowHeight(26);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(20, 160, 930, 380);
		con.add(scrollPane);

		loadCustomers(null);
	}

	private void loadCustomers(String filter) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("First Name");
		model.addColumn("Middle Name");
		model.addColumn("Last Name");
		model.addColumn("Email");
		model.addColumn("Gender");
		model.addColumn("Birthdate");
		model.addColumn("Occupation");
		model.addColumn("Address");
		model.addColumn("Mobile #");
		model.addColumn("Username");
		model.addColumn("Plate #");
		model.addColumn("Brand");
		model.addColumn("Color");
		model.addColumn("Type");

		if (conn == null) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Database connection is not available.");
			return;
		}

		try {
			String sql = "select FirstName,MiddleName,LastName,Email,Gender,Birthdate,Occupation,Address,"
					+ "MobileNumber,Username,PlateNumber,Brand,Color,Type from useraccount";
			boolean hasFilter = filter != null && !filter.trim().isEmpty();
			if (hasFilter) {
				sql += " where Username like ? or FirstName like ? or MiddleName like ? or LastName like ? or Email like ?";
			}

			PreparedStatement ps = conn.prepareStatement(sql);
			if (hasFilter) {
				String searchValue = "%" + filter.trim() + "%";
				for (int i = 1; i <= 5; i++) {
					ps.setString(i, searchValue);
				}
			}

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("FirstName"), rs.getString("MiddleName"),
						rs.getString("LastName"), rs.getString("Email"), rs.getString("Gender"),
						rs.getString("Birthdate"), rs.getString("Occupation"), rs.getString("Address"),
						rs.getString("MobileNumber"), rs.getString("Username"), rs.getString("PlateNumber"),
						rs.getString("Brand"), rs.getString("Color"), rs.getString("Type") });
			}
			rs.close();
			ps.close();
			table.setModel(model);
		} catch (Exception ex) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Unable to load customer profiles: " + ex.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == search) {
			loadCustomers(searchText.getText());
			return;
		}

		if (e.getSource() == reset) {
			searchText.setText("");
			loadCustomers(null);
			return;
		}

		if (e.getSource() == logout) {
			int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialog == JOptionPane.YES_OPTION) {
				login app = new login();
				app.setTitle("Admin Login");
				app.setVisible(true);
				app.setSize(450, 600);
				app.setLocationRelativeTo(null);
				dispose();
			}
			return;
		}

		if (e.getSource() == menu1) {
			menu app = new menu();
			app.setTitle("Admin Menu");
			app.setVisible(true);
			app.setSize(1000, 400);
			app.setLocationRelativeTo(null);
			dispose();
		}
	}

	public static void main(String[] args) {
		customer_profile app = new customer_profile();
		app.setTitle("Customer Profile");
		app.setSize(1000, 600);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
