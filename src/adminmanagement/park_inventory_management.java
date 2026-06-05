package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
 * Admin inventory screen for viewing and searching parking reservation records.
 */
public class park_inventory_management extends JFrame implements ActionListener {
	private JTextField searchField;
	private JButton searchButton;
	private JButton resetButton;
	private JButton addButton;
	private JButton logoutButton;
	private JButton menuButton;
	private JTable table;
	private JLabel totalLabel;
	private JLabel latestLabel;
	private JLabel statusLabel;
	private Connection conn;

	public park_inventory_management() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Parking Inventory", "Track customer reservations, vehicles, and slot usage.",
				buildContent()));
		loadInventory(null);
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(buildTable(), BorderLayout.CENTER);
		content.add(buildFooter(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		toolbar.setOpaque(false);

		menuButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");
		addButton = AppTheme.primaryButton("Add Reservation");
		searchField = AppTheme.textField("Search inventory");
		searchField.setColumns(22);
		searchButton = AppTheme.primaryButton("Search");
		resetButton = AppTheme.secondaryButton("Reset");

		menuButton.addActionListener(this);
		logoutButton.addActionListener(this);
		addButton.addActionListener(this);
		searchButton.addActionListener(this);
		resetButton.addActionListener(this);
		searchField.addActionListener(this);

		toolbar.add(menuButton);
		toolbar.add(logoutButton);
		toolbar.add(addButton);
		toolbar.add(AppTheme.label("Search"));
		toolbar.add(searchField);
		toolbar.add(searchButton);
		toolbar.add(resetButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JPanel buildFooter() {
		JPanel footer = new JPanel(new BorderLayout(14, 0));
		footer.setOpaque(false);

		JPanel metrics = new JPanel(new GridLayout(1, 2, 12, 0));
		metrics.setOpaque(false);
		totalLabel = AppTheme.sectionLabel("Total Reservations: 0");
		latestLabel = AppTheme.sectionLabel("Latest Reservation: N/A");
		metrics.add(totalLabel);
		metrics.add(latestLabel);

		statusLabel = AppTheme.label("Loading inventory...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);

		footer.add(metrics, BorderLayout.CENTER);
		footer.add(statusLabel, BorderLayout.SOUTH);
		return footer;
	}

	private void loadInventory(String filter) {
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

		String sql = "select ID,FullName,Email,Gender,MobileNumber,Birthdate,PlateNumber,Brand,Color,Type,DOR,DOP,"
				+ "SlotNumber,THP,TP,TD from inventory";
		boolean hasFilter = filter != null && !filter.trim().isEmpty();
		if (hasFilter) {
			sql += " where ID like ? or FullName like ? or Email like ? or PlateNumber like ? or SlotNumber like ?";
		}
		sql += " order by ID";

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			if (hasFilter) {
				String searchValue = "%" + filter.trim() + "%";
				for (int index = 1; index <= 5; index++) {
					statement.setString(index, searchValue);
				}
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				int rows = 0;
				String latestReservation = "N/A";
				while (resultSet.next()) {
					latestReservation = resultSet.getString("DOR");
					model.addRow(new Object[] { resultSet.getString("ID"), resultSet.getString("FullName"),
							resultSet.getString("Email"), resultSet.getString("Gender"),
							resultSet.getString("MobileNumber"), resultSet.getString("Birthdate"),
							resultSet.getString("PlateNumber"), resultSet.getString("Brand"),
							resultSet.getString("Color"), resultSet.getString("Type"), resultSet.getString("DOR"),
							resultSet.getString("DOP"), resultSet.getString("SlotNumber"), resultSet.getString("THP"),
							resultSet.getString("TP"), resultSet.getString("TD") });
					rows++;
				}
				table.setModel(model);
				totalLabel.setText("Total Reservations: " + rows);
				latestLabel.setText("Latest Reservation: " + latestReservation);
				statusLabel.setText(rows + " inventory record(s) loaded.");
			}
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load parking inventory.", error);
			statusLabel.setText("Unable to load parking inventory.");
		}
	}

	private String[] columnNames() {
		return new String[] { "ID", "Full Name", "Email", "Gender", "Mobile #", "Birthdate", "Plate #", "Brand",
				"Color", "Type", "Reservation Date", "Park Date", "Slot #", "Hours", "Time Park", "Departure" };
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == menuButton) {
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		} else if (source == logoutButton) {
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		} else if (source == addButton) {
			parking_inventory_management_add_button app = new parking_inventory_management_add_button();
			AppTheme.showFrame(app, "Add Parking Reservation", 1180, 760);
			dispose();
		} else if (source == searchButton || source == searchField) {
			loadInventory(searchField.getText());
		} else if (source == resetButton) {
			searchField.setText("");
			loadInventory(null);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			park_inventory_management app = new park_inventory_management();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Parking Inventory Management", 1120, 720);
		});
	}
}
