package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AuthenticationService;
import app.AppTheme;
import app.PasswordSecurity;
import app.SessionContext;
import app.UserRole;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Administrator login screen for parking operations.
 */
public class login extends JFrame implements ActionListener {
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JButton resetButton;
	private JButton registerButton;
	private JCheckBox showPasswordBox;
	private JLabel statusLabel;
	private Connection conn;

	public login() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Admin Console", "Sign in to manage customers, inventory, slots, and billing.",
				buildContent()));
	}

	private JPanel buildContent() {
		JPanel card = AppTheme.card();
		card.setLayout(new BorderLayout(0, 18));
		card.add(buildHeaderRow(), BorderLayout.NORTH);
		card.add(buildForm(), BorderLayout.CENTER);
		card.add(buildActions(), BorderLayout.SOUTH);
		return card;
	}

	private JPanel buildHeaderRow() {
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		header.add(AppTheme.sectionLabel("Administrator Login"), BorderLayout.WEST);
		header.add(AppTheme.clockLabel(), BorderLayout.EAST);
		return header;
	}

	private JPanel buildForm() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		JLabel usernameLabel = AppTheme.label("Username");
		usernameField = AppTheme.textField("Admin username");
		usernameLabel.setLabelFor(usernameField);

		JLabel passwordLabel = AppTheme.label("Password");
		passwordField = AppTheme.passwordField("Admin password");
		passwordLabel.setLabelFor(passwordField);

		showPasswordBox = new JCheckBox("Show password");
		showPasswordBox.setOpaque(false);
		showPasswordBox.addActionListener(this);

		statusLabel = AppTheme.label("Use an administrator account.");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);

		GridBagConstraints usernameLabelConstraints = AppTheme.constraints(0, 0);
		usernameLabelConstraints.insets.set(8, 0, 4, 0);
		form.add(usernameLabel, usernameLabelConstraints);

		GridBagConstraints usernameConstraints = AppTheme.constraints(0, 1);
		usernameConstraints.insets.set(0, 0, 12, 0);
		form.add(usernameField, usernameConstraints);

		GridBagConstraints passwordLabelConstraints = AppTheme.constraints(0, 2);
		passwordLabelConstraints.insets.set(8, 0, 4, 0);
		form.add(passwordLabel, passwordLabelConstraints);

		GridBagConstraints passwordConstraints = AppTheme.constraints(0, 3);
		passwordConstraints.insets.set(0, 0, 8, 0);
		form.add(passwordField, passwordConstraints);

		GridBagConstraints showConstraints = AppTheme.constraints(0, 4);
		showConstraints.insets.set(0, 0, 8, 0);
		form.add(showPasswordBox, showConstraints);

		GridBagConstraints statusConstraints = AppTheme.constraints(0, 5);
		statusConstraints.insets.set(6, 0, 0, 0);
		form.add(statusLabel, statusConstraints);

		return form;
	}

	private JPanel buildActions() {
		JPanel actions = new JPanel(new GridBagLayout());
		actions.setOpaque(false);
		actions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

		loginButton = AppTheme.primaryButton("Login");
		resetButton = AppTheme.secondaryButton("Clear");
		registerButton = AppTheme.secondaryButton("Register Admin");

		loginButton.addActionListener(this);
		resetButton.addActionListener(this);
		registerButton.addActionListener(this);

		actions.add(loginButton, AppTheme.constraints(0, 0));
		actions.add(resetButton, AppTheme.constraints(1, 0));

		GridBagConstraints registerConstraints = AppTheme.constraints(0, 1);
		registerConstraints.gridwidth = 2;
		actions.add(registerButton, registerConstraints);

		return actions;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == registerButton) {
			reg app = new reg();
			AppTheme.showFrame(app, "Admin Registration Form", 1100, 560);
			dispose();
		} else if (source == loginButton) {
			loginAdmin();
		} else if (source == resetButton) {
			resetForm();
		} else if (source == showPasswordBox) {
			passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '*');
		}
	}

	private void resetForm() {
		usernameField.setText("");
		passwordField.setText("");
		statusLabel.setText("Use an administrator account.");
		usernameField.requestFocusInWindow();
	}

	private void loginAdmin() {
		if (conn == null) {
			AppTheme.showError(this, "Database connection is not available. Check the MySQL container or local service.",
					null);
			return;
		}

		String username = usernameField.getText().trim();
		char[] password = passwordField.getPassword();
		if (username.isEmpty() || password.length == 0) {
			statusLabel.setText("Enter both username and password.");
			PasswordSecurity.clear(password);
			return;
		}

		try {
			if (AuthenticationService.authenticateAdmin(conn, username, password)) {
				openMenu();
			} else {
				statusLabel.setText("Invalid admin username or password.");
				passwordField.setText("");
			}
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to log in.", error);
		} finally {
			PasswordSecurity.clear(password);
		}
	}

	private void openMenu() {
		SessionContext.signIn(usernameField.getText().trim(), UserRole.ADMIN);
		JOptionPane.showMessageDialog(this, "Login successful.");
		menu app = new menu();
		AppTheme.showFrame(app, "Admin Menu", 1060, 600);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			login app = new login();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Admin Login", 540, 640);
		});
	}
}
