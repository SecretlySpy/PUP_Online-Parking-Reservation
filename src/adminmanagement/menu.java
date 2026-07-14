package adminmanagement;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Main navigation hub for administrator workflows.
 */
public class menu extends JFrame implements ActionListener {
	private JButton customerButton;
	private JButton billingButton;
	private JButton inventoryButton;
	private JButton slotButton;
	private JButton reservationButton;
	private JButton reportsButton;
	private JButton logoutButton;
	private JButton themeButton;
	private JButton notificationButton;
	private JPanel moduleGrid;
	private Timer pollTimer;
	private ReservationRepository repository;

	public menu() {
		AppTheme.install();
		this.repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Admin Dashboard", "Manage parking operations from one place.", buildContent()));
		
		pollTimer = new Timer(5000, e -> updateNotificationCount());
		pollTimer.start();
		updateNotificationCount();
	}

	private void updateNotificationCount() {
		try {
			int count = repository.getAdminNotificationCount();
			if (count > 0) {
				notificationButton.setText("🔔 Pending (" + count + ")");
			} else {
				notificationButton.setText("🔔 Pending");
			}
		} catch (Exception ex) {
			// Ignore silently for background polling
		}
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
		topBar.add(AppTheme.sectionLabel("Operations"), BorderLayout.WEST);

		JPanel rightActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
		rightActions.setOpaque(false);

		themeButton = AppTheme.secondaryButton(AppTheme.isDarkMode ? "Light Mode" : "Dark Mode");
		themeButton.addActionListener(this);
		
		notificationButton = AppTheme.secondaryButton("🔔 Pending");
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

		customerButton = AppTheme.moduleButton("Customer Profiles");
		inventoryButton = AppTheme.moduleButton("Park Inventory");
		slotButton = AppTheme.moduleButton("Parking Slots");
		billingButton = AppTheme.moduleButton("Billing");
		reservationButton = AppTheme.moduleButton("Customer Reservations");
		reportsButton = AppTheme.moduleButton("Reports");

		customerButton.addActionListener(this);
		inventoryButton.addActionListener(this);
		slotButton.addActionListener(this);
		billingButton.addActionListener(this);
		reservationButton.addActionListener(this);
		reportsButton.addActionListener(this);

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
		JButton[] buttons = new JButton[] { customerButton, inventoryButton, slotButton, billingButton,
				reservationButton, reportsButton };
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
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Dashboard", getWidth(), getHeight());
			dispose();
		} else if (source == notificationButton) {
			if (pollTimer != null) pollTimer.stop();
			openReservations();
		} else if (source == logoutButton) {
			logout();
		} else if (source == customerButton) {
			openCustomerProfile();
		} else if (source == inventoryButton) {
			openInventory();
		} else if (source == slotButton) {
			openSlots();
		} else if (source == billingButton) {
			openBilling();
		} else if (source == reservationButton) {
			openReservations();
		} else if (source == reportsButton) {
			openReports();
		}
	}

	private void logout() {
		int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm logout",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			if (pollTimer != null) pollTimer.stop();
			SessionContext.signOut();
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		}
	}

	private void openCustomerProfile() {
		customer_profile app = new customer_profile();
		AppTheme.showFrame(app, "Customer Profile", 1100, 680);
		dispose();
	}

	private void openInventory() {
		park_inventory_management app = new park_inventory_management();
		AppTheme.showFrame(app, "Parking Inventory Management", 1120, 720);
		dispose();
	}

	private void openSlots() {
		parking_slot app = new parking_slot();
		AppTheme.showFrame(app, "Parking Slot", 1180, 720);
		dispose();
	}

	private void openBilling() {
		billingmanagement app = new billingmanagement();
		AppTheme.showFrame(app, "Billing Management", 1000, 700);
		dispose();
	}

	private void openReservations() {
		customer_reservation app = new customer_reservation();
		AppTheme.showFrame(app, "Customer Reservation", 1180, 700);
		dispose();
	}

	private void openReports() {
		reports app = new reports();
		AppTheme.showFrame(app, "Reports", 1080, 660);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			menu app = new menu();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
		});
	}
}
