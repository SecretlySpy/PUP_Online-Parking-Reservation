package usermanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.SessionContext;
import app.UserRole;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
 * User login screen for customer-facing workflows.
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
		setContentPane(AppTheme.shell("Customer Portal", "Sign in to view your profile and parking details.", buildContent()));
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
		header.add(AppTheme.sectionLabel("User Login"), BorderLayout.WEST);
		header.add(AppTheme.clockLabel(), BorderLayout.EAST);
		return header;
	}

	private JPanel buildForm() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		JLabel usernameLabel = AppTheme.label("Username");
		usernameField = AppTheme.textField("Username");
		usernameLabel.setLabelFor(usernameField);

		JLabel passwordLabel = AppTheme.label("Password");
		passwordField = AppTheme.passwordField("Password");
		passwordLabel.setLabelFor(passwordField);

		showPasswordBox = new JCheckBox("Show password");
		showPasswordBox.setOpaque(false);
		showPasswordBox.addActionListener(this);

		statusLabel = AppTheme.label("Use your registered customer account.");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);

		GridBagConstraints labelConstraints = AppTheme.constraints(0, 0);
		labelConstraints.weightx = 0;
		labelConstraints.insets.set(8, 0, 4, 0);
		form.add(usernameLabel, labelConstraints);

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
		resetButton = AppTheme.secondaryButton("Reset");
		registerButton = AppTheme.secondaryButton("Create Account");

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
			openRegistration();
		} else if (source == loginButton) {
			loginUser();
		} else if (source == resetButton) {
			resetForm();
		} else if (source == showPasswordBox) {
			passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '*');
		}
	}

	private void openRegistration() {
			reg app = new reg();
		AppTheme.showFrame(app, "Registration Form", 1000, 750);
		dispose();
	}

	private void resetForm() {
		usernameField.setText("");
		passwordField.setText("");
		statusLabel.setText("Use your registered customer account.");
		usernameField.requestFocusInWindow();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			wirePasswordResetButton();
		}
		super.setVisible(visible);
	}

	private void wirePasswordResetButton() {
		java.awt.Component[] components = getContentPane().getComponents();
		for (java.awt.Component component : components) {
			if (component instanceof javax.swing.JButton) {
				javax.swing.JButton button = (javax.swing.JButton) component;
				String text = button.getText() == null ? "" : button.getText().trim().toLowerCase();
				if (text.contains("reset") || text.contains("forgot")) {
					button.setText("Forgot Password");
					for (java.awt.event.ActionListener listener : button.getActionListeners()) {
						button.removeActionListener(listener);
					}
					button.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent event) {
							resetPassword();
						}
					});
					return;
				}
			}
		}
	}

	private void resetPassword() {
		String accountInput = javax.swing.JOptionPane.showInputDialog(this,
				"Enter your username or registered email address:");
		if (accountInput == null) {
			return;
		}
		accountInput = accountInput.trim();

		if (accountInput.isEmpty()) {
			javax.swing.JOptionPane.showMessageDialog(this, "Please enter your username or registered email address.");
			return;
		}

		int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
				"A temporary password will be sent to the email address on your account.", "Reset Password",
				javax.swing.JOptionPane.OK_CANCEL_OPTION);
		if (confirm != javax.swing.JOptionPane.OK_OPTION) {
			return;
		}

		try (java.sql.Connection resetConnection = DatabaseConnection.ConnectionDB.getConnection()) {
			if (resetConnection == null) {
				javax.swing.JOptionPane.showMessageDialog(this, "Database connection is not available.");
				return;
			}
			PasswordResetService.PasswordResetResult result = new PasswordResetService().resetCustomerPassword(
					resetConnection, accountInput);
			javax.swing.JOptionPane.showMessageDialog(this,
					"A temporary password was sent to " + result.getMaskedEmail() + " for " + result.getUsername()
							+ ".");
		} catch (Exception ex) {
			javax.swing.JOptionPane.showMessageDialog(this, "Unable to reset password: " + ex.getMessage());
		}
	}

	private void loginUser() {
		if (conn == null) {
			AppTheme.showError(this, "Database connection is not available. Check the MySQL container or local service.",
					null);
			return;
		}

		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());
		if (username.isEmpty() || password.isEmpty()) {
			statusLabel.setText("Enter both username and password.");
			return;
		}

		String sql = "select Username from useraccount where Username=? and Password=?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, username);
			statement.setString(2, password);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					openMenu(resultSet.getString("Username"));
				} else {
					statusLabel.setText("Invalid username or password.");
					passwordField.setText("");
				}
			}
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to log in.", error);
		}
	}

	private void openMenu(String loggedInUsername) {
		SessionContext.signIn(loggedInUsername, UserRole.USER);
		JOptionPane.showMessageDialog(this, "Login successful.");
		menu app = new menu(loggedInUsername);
		AppTheme.showFrame(app, "User Menu", 980, 540);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			login app = new login();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "User Login", 520, 620);
		});
	}
}
