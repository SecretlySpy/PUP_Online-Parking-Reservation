package usermanagement;

import app.AppTheme;
import app.ReservationRepository;
import app.SessionContext;
import DatabaseConnection.ConnectionDB;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Customer dashboard that routes the signed-in user to profile and car details.
 */
public class menu extends JFrame implements ActionListener {
	private JButton profileButton;
	private JButton carButton;
	private JButton reservationButton;
	private JButton historyButton;
	private JButton helpButton;
	private JButton logoutButton;
	private JButton themeButton;
	private JButton notificationButton;
	private JPanel moduleGrid;
	private String username;
	private Timer pollTimer;
	private ReservationRepository repository;

	public menu() {
		this(null);
	}

	public menu(String username) {
		AppTheme.install();
		this.username = username;
		this.repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Customer Dashboard", subtitle(), buildContent()));
		
		pollTimer = new Timer(5000, e -> updateNotificationCount());
		pollTimer.start();
		updateNotificationCount();
	}

	private void updateNotificationCount() {
		if (username == null) return;
		try {
			int count = repository.getUnreadNotificationCount(username);
			if (count > 0) {
				notificationButton.setText("🔔 Notifications (" + count + ")");
			} else {
				notificationButton.setText("🔔 Notifications");
			}
		} catch (Exception ex) {
			// Ignore silently for background polling
		}
	}

	private String subtitle() {
		if (username == null || username.trim().isEmpty()) {
			return "Choose a module to continue.";
		}
		return "Signed in as " + username + ". Choose a module to continue.";
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 18));
		content.add(buildTopBar(), BorderLayout.NORTH);
		content.add(buildModuleGrid(), BorderLayout.CENTER);
		return content;
	}

	private JPanel buildTopBar() {
		JPanel topBar = new JPanel(new BorderLayout());
		topBar.setOpaque(false);

		JLabel sectionTitle = AppTheme.sectionLabel("Available Modules");
		topBar.add(sectionTitle, BorderLayout.WEST);

		JPanel rightActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
		rightActions.setOpaque(false);

		themeButton = AppTheme.secondaryButton(AppTheme.isDarkMode ? "Light Mode" : "Dark Mode");
		themeButton.addActionListener(this);
		
		notificationButton = AppTheme.secondaryButton("🔔 Notifications");
		notificationButton.addActionListener(this);
		
		rightActions.add(themeButton);
		rightActions.add(notificationButton);

		logoutButton = AppTheme.dangerButton("Logout");
		logoutButton.addActionListener(this);
		rightActions.add(logoutButton);

		topBar.add(rightActions, BorderLayout.EAST);

		return topBar;
	}

	private JPanel buildModuleGrid() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		moduleGrid = new JPanel(new GridBagLayout());
		moduleGrid.setOpaque(false);

		profileButton = AppTheme.moduleButton("Customer Profile");
		carButton = AppTheme.moduleButton("Car Profile");
		reservationButton = AppTheme.moduleButton("Book Parking Slot");
		historyButton = AppTheme.moduleButton("Reservation History");
		helpButton = AppTheme.moduleButton("Help / Support");
		helpButton.setEnabled(false);

		profileButton.addActionListener(this);
		carButton.addActionListener(this);
		reservationButton.addActionListener(this);
		historyButton.addActionListener(this);

		moduleGrid.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				layoutModuleButtons();
			}
		});
		layoutModuleButtons();
		wrapper.add(moduleGrid, BorderLayout.NORTH);
		return wrapper;
	}

	private void layoutModuleButtons() {
		if (moduleGrid == null) {
			return;
		}

		int columns = moduleGrid.getWidth() > 0 && moduleGrid.getWidth() < 760 ? 1 : 2;
		JButton[] buttons = new JButton[] { profileButton, carButton, reservationButton, historyButton, helpButton };
		moduleGrid.removeAll();
		for (int index = 0; index < buttons.length; index++) {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = index % columns;
			constraints.gridy = index / columns;
			constraints.insets = new Insets(8, 0, 8, 16);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			moduleGrid.add(buttons[index], constraints);
		}
		moduleGrid.revalidate();
		moduleGrid.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == themeButton) {
			if (pollTimer != null) pollTimer.stop();
			AppTheme.toggleTheme();
			themeButton.setText(AppTheme.isDarkMode ? "Light Mode" : "Dark Mode");
			menu app = new menu(username);
			AppTheme.showFrame(app, "Customer Dashboard", getWidth(), getHeight());
			dispose();
		} else if (source == notificationButton) {
			JOptionPane.showMessageDialog(this, "Notification feature is a placeholder. Check back later.");
		} else if (source == logoutButton) {
			logout();
		} else if (source == profileButton) {
			openProfile();
		} else if (source == carButton) {
			openCarProfile();
		} else if (source == reservationButton) {
			openReservation();
		} else if (source == historyButton) {
			openHistory();
		}
	}

	private void logout() {
		int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm logout",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			if (pollTimer != null) pollTimer.stop();
			SessionContext.signOut();
			login app = new login();
			AppTheme.showFrame(app, "User Login", 520, 620);
			dispose();
		}
	}

	private void openProfile() {
		user_profile app = new user_profile(username);
		AppTheme.showFrame(app, "Customer Profile", 980, 620);
		dispose();
	}

	private void openCarProfile() {
		car_profile app = new car_profile(username);
		AppTheme.showFrame(app, "Car Profile", 860, 520);
		dispose();
	}

	private void openReservation() {
		reservation app = new reservation(username);
		AppTheme.showFrame(app, "Book Parking Slot", 1180, 720);
		dispose();
	}

	private void openHistory() {
		reservation_history app = new reservation_history(username);
		AppTheme.showFrame(app, "Reservation History", 1080, 640);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			menu app = new menu();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "User Menu", 980, 540);
		});
	}
}
