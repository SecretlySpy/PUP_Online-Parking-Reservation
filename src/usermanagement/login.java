package usermanagement;

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
import java.util.concurrent.ExecutionException;

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
import javax.swing.SwingWorker;

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
		resetButton = AppTheme.secondaryButton("Reset Password");
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
			openPasswordResetWorkflow();
		} else if (source == showPasswordBox) {
			passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '*');
		}
	}

	private void openRegistration() {
		reg app = new reg();
		AppTheme.showFrame(app, "Registration Form", 1000, 750);
		dispose();
	}

	private void openPasswordResetWorkflow() {
		String[] options = { "Send Reset Link", "Use Reset Link", "Cancel" };
		int selection = JOptionPane.showOptionDialog(this, "Choose how you want to reset your password.",
				"Reset Password", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (selection == 0) {
			sendPasswordResetLink();
		} else if (selection == 1) {
			completePasswordReset();
		}
	}

	private void sendPasswordResetLink() {
		String accountInput = JOptionPane.showInputDialog(this, "Enter your username or registered email address:",
				usernameField.getText().trim());
		if (accountInput == null) {
			return;
		}
		accountInput = accountInput.trim();

		if (accountInput.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter your username or registered email address.");
			return;
		}

		setLoginActionsEnabled(false);
		statusLabel.setText("Sending password reset link...");

		final String identifier = accountInput;
		new SwingWorker<PasswordResetService.PasswordResetRequestResult, Void>() {
			@Override
			protected PasswordResetService.PasswordResetRequestResult doInBackground() throws Exception {
				try (Connection resetConnection = ConnectionDB.getConnection()) {
					if (resetConnection == null) {
						throw new IllegalStateException("Database connection is not available.");
					}
					return new PasswordResetService().requestCustomerPasswordReset(resetConnection, identifier);
				}
			}

			@Override
			protected void done() {
				setLoginActionsEnabled(true);
				try {
					get();
					statusLabel.setText("Password reset request submitted.");
					JOptionPane.showMessageDialog(login.this,
							"If an account matches that username or email, a secure reset link will be sent.");
				} catch (Exception error) {
					statusLabel.setText("Unable to send password reset link.");
					AppTheme.showError(login.this, "Unable to send password reset link.", unwrap(error));
				}
			}
		}.execute();
	}

	private void completePasswordReset() {
		String tokenOrLink = JOptionPane.showInputDialog(this, "Paste the reset link or token from your email:");
		if (tokenOrLink == null) {
			return;
		}
		tokenOrLink = tokenOrLink.trim();

		if (tokenOrLink.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Paste the reset link or token from your email.");
			return;
		}

		JPasswordField newPasswordField = AppTheme.passwordField("New password");
		JPasswordField repeatPasswordField = AppTheme.passwordField("Confirm new password");
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(AppTheme.label("New Password"), AppTheme.constraints(0, 0));
		panel.add(newPasswordField, AppTheme.constraints(1, 0));
		panel.add(AppTheme.label("Confirm Password"), AppTheme.constraints(0, 1));
		panel.add(repeatPasswordField, AppTheme.constraints(1, 1));

		int confirm = JOptionPane.showConfirmDialog(this, panel, "Set New Password", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			return;
		}

		char[] newPassword = newPasswordField.getPassword();
		char[] repeatPassword = repeatPasswordField.getPassword();
		try {
			if (!PasswordSecurity.matches(newPassword, repeatPassword)) {
				JOptionPane.showMessageDialog(this, "Passwords do not match.");
				return;
			}

			String strengthError = PasswordSecurity.strengthError(newPassword);
			if (strengthError != null) {
				JOptionPane.showMessageDialog(this, strengthError);
				return;
			}
		} finally {
			PasswordSecurity.clear(repeatPassword);
		}

		setLoginActionsEnabled(false);
		statusLabel.setText("Updating password...");

		final String resetTokenOrLink = tokenOrLink;
		new SwingWorker<PasswordResetService.PasswordResetCompletionResult, Void>() {
			@Override
			protected PasswordResetService.PasswordResetCompletionResult doInBackground() throws Exception {
				try (Connection resetConnection = ConnectionDB.getConnection()) {
					if (resetConnection == null) {
						throw new IllegalStateException("Database connection is not available.");
					}
					return new PasswordResetService().completeCustomerPasswordReset(resetConnection, resetTokenOrLink,
							newPassword);
				}
			}

			@Override
			protected void done() {
				PasswordSecurity.clear(newPassword);
				setLoginActionsEnabled(true);
				try {
					PasswordResetService.PasswordResetCompletionResult result = get();
					usernameField.setText(result.getUsername());
					passwordField.setText("");
					statusLabel.setText("Password updated. Sign in with your new password.");
					JOptionPane.showMessageDialog(login.this, "Your password has been updated.");
				} catch (Exception error) {
					statusLabel.setText("Unable to update password.");
					AppTheme.showError(login.this, "Unable to update password.", unwrap(error));
				}
			}
		}.execute();
	}

	private Exception unwrap(Exception error) {
		if (error instanceof ExecutionException && error.getCause() instanceof Exception) {
			return (Exception) error.getCause();
		}
		return error;
	}

	private void setLoginActionsEnabled(boolean enabled) {
		loginButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		registerButton.setEnabled(enabled);
	}

	private void loginUser() {
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
			if (AuthenticationService.authenticateCustomer(conn, username, password)) {
				openMenu(username);
			} else {
				statusLabel.setText("Invalid username or password.");
				passwordField.setText("");
			}
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to log in.", error);
		} finally {
			PasswordSecurity.clear(password);
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
