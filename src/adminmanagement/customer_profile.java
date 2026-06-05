package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Admin customer directory with keyword search across names, email, and username.
 */
public class customer_profile extends JFrame implements ActionListener {
	private JTextField searchText;
	private JTable table;
	private JButton logoutButton;
	private JButton menuButton;
	private JButton searchButton;
	private JButton resetButton;
	private JLabel statusLabel;
	private Connection conn;

	public customer_profile() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Customer Profiles", "Search and review registered customer accounts.",
				buildContent()));
		loadCustomers(null);
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(buildTable(), BorderLayout.CENTER);
		content.add(buildStatus(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		toolbar.setOpaque(false);

		menuButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");
		searchText = AppTheme.textField("Search customers");
		searchText.setColumns(24);
		searchButton = AppTheme.primaryButton("Search");
		resetButton = AppTheme.secondaryButton("Reset");

		menuButton.addActionListener(this);
		logoutButton.addActionListener(this);
		searchButton.addActionListener(this);
		resetButton.addActionListener(this);
		searchText.addActionListener(this);

		toolbar.add(menuButton);
		toolbar.add(logoutButton);
		toolbar.add(AppTheme.label("Search"));
		toolbar.add(searchText);
		toolbar.add(searchButton);
		toolbar.add(resetButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Loading customers...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadCustomers(String filter) {
		DefaultTableModel model = new DefaultTableModel(columnNames(), 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		if (conn == null) {
			table.setModel(model);
			statusLabel.setText("Database connection is not available.");
			return;
		}

		String sql = "select FirstName,MiddleName,LastName,Email,Gender,Birthdate,Occupation,Address,"
				+ "MobileNumber,Username,PlateNumber,Brand,Color,Type from useraccount";
		boolean hasFilter = filter != null && !filter.trim().isEmpty();
		if (hasFilter) {
			sql += " where Username like ? or FirstName like ? or MiddleName like ? or LastName like ? or Email like ?";
		}

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			if (hasFilter) {
				String searchValue = "%" + filter.trim() + "%";
				for (int index = 1; index <= 5; index++) {
					statement.setString(index, searchValue);
				}
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				int rows = 0;
				while (resultSet.next()) {
					model.addRow(new Object[] { resultSet.getString("FirstName"), resultSet.getString("MiddleName"),
							resultSet.getString("LastName"), resultSet.getString("Email"), resultSet.getString("Gender"),
							resultSet.getString("Birthdate"), resultSet.getString("Occupation"),
							resultSet.getString("Address"), resultSet.getString("MobileNumber"),
							resultSet.getString("Username"), resultSet.getString("PlateNumber"),
							resultSet.getString("Brand"), resultSet.getString("Color"), resultSet.getString("Type") });
					rows++;
				}
				table.setModel(model);
				statusLabel.setText(rows + " customer record(s) loaded.");
			}
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load customer profiles.", error);
			statusLabel.setText("Unable to load customer profiles.");
		}
	}

	private String[] columnNames() {
		return new String[] { "First Name", "Middle Name", "Last Name", "Email", "Gender", "Birthdate",
				"Occupation", "Address", "Mobile #", "Username", "Plate #", "Brand", "Color", "Type" };
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == searchButton || source == searchText) {
			loadCustomers(searchText.getText());
		} else if (source == resetButton) {
			searchText.setText("");
			loadCustomers(null);
		} else if (source == logoutButton) {
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		} else if (source == menuButton) {
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			customer_profile app = new customer_profile();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Customer Profile", 1100, 680);
		});
	}
}
