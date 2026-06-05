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
 * Displays the signed-in customer's personal account fields.
 */
public class user_profile extends JFrame implements ActionListener {
	private JTable table;
	private JButton backButton;
	private JButton logoutButton;
	private JLabel statusLabel;
	private String username;
	private Connection conn;

	public user_profile() {
		this(null);
	}

	public user_profile(String username) {
		AppTheme.install();
		this.username = username;
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Customer Profile", "Review your account information.", buildContent()));
		loadProfile();
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
		statusLabel = AppTheme.label("Loading profile...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadProfile() {
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

		String sql = "select FirstName,MiddleName,LastName,Email,Gender,Birthdate,Occupation,Address,MobileNumber,Username "
				+ "from useraccount";
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
					model.addRow(new Object[] { resultSet.getString("FirstName"), resultSet.getString("MiddleName"),
							resultSet.getString("LastName"), resultSet.getString("Email"), resultSet.getString("Gender"),
							resultSet.getString("Birthdate"), resultSet.getString("Occupation"),
							resultSet.getString("Address"), resultSet.getString("MobileNumber"),
							resultSet.getString("Username") });
					rows++;
				}
				table.setModel(model);
				statusLabel.setText(rows + " profile record(s) loaded.");
			}
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load profile.", error);
			statusLabel.setText("Unable to load profile.");
		}
	}

	private String[] columnNames() {
		return new String[] { "First Name", "Middle Name", "Last Name", "Email", "Gender", "Birthdate",
				"Occupation", "Address", "Mobile #", "Username" };
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
			user_profile app = new user_profile();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Customer Profile", 980, 620);
		});
	}
}
