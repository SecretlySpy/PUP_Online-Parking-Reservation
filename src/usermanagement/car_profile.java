package usermanagement;

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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Displays vehicle data linked to the signed-in customer account.
 */
public class car_profile extends JFrame implements ActionListener {
	private JTable table;
	private JButton backButton;
	private JButton logoutButton;
	private JLabel statusLabel;
	private String username;
	private Connection conn;

	public car_profile() {
		this(null);
	}

	public car_profile(String username) {
		AppTheme.install();
		this.username = username;
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Car Profile", "Review the vehicle information connected to your account.",
				buildContent()));
		loadCarProfile();
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

		backButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");

		backButton.addActionListener(this);
		logoutButton.addActionListener(this);

		toolbar.add(backButton);
		toolbar.add(logoutButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Loading car profile...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadCarProfile() {
		DefaultTableModel model = new DefaultTableModel(new String[] { "Username", "Plate #", "Brand", "Color", "Type" },
				0) {
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

		String sql = "select Username,PlateNumber,Brand,Color,Type from useraccount";
		boolean hasUserFilter = username != null && !username.trim().isEmpty();
		if (hasUserFilter) {
			sql += " where Username=?";
		}

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			if (hasUserFilter) {
				statement.setString(1, username);
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				int rows = 0;
				while (resultSet.next()) {
					model.addRow(new Object[] { resultSet.getString("Username"), resultSet.getString("PlateNumber"),
							resultSet.getString("Brand"), resultSet.getString("Color"), resultSet.getString("Type") });
					rows++;
				}
				table.setModel(model);
				statusLabel.setText(rows + " vehicle record(s) loaded.");
			}
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load car profile.", error);
			statusLabel.setText("Unable to load car profile.");
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == backButton) {
			menu app = new menu(username);
			AppTheme.showFrame(app, "User Menu", 980, 540);
			dispose();
		} else if (source == logoutButton) {
			login app = new login();
			AppTheme.showFrame(app, "User Login", 520, 620);
			dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			car_profile app = new car_profile();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Car Profile", 860, 520);
		});
	}
}
