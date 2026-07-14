package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.ReservationRecord;
import app.ReservationRepository;
import app.SessionContext;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.toedter.calendar.JCalendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Admin reservation manager with filters and status updates.
 */
public class customer_reservation extends JFrame implements ActionListener {
	private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

	private ReservationRepository repository;
	private JTable table;
	private JButton menuButton;
	private JButton logoutButton;
	private JButton refreshButton;
	private JButton updateStatusButton;
	private JComboBox<String> statusFilterBox;
	private JComboBox<String> floorFilterBox;
	private JComboBox<String> statusUpdateBox;
	private JLabel statusLabel;
	private JCalendar calendar;

	public customer_reservation() {
		AppTheme.install();
		repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Customer Reservations", "Filter reservations and update operational status.",
				buildContent()));
		loadReservations();
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		
		calendar = new JCalendar();
		calendar.addPropertyChangeListener("calendar", evt -> loadReservations());
		
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(calendar, BorderLayout.WEST);
		content.add(buildTable(), BorderLayout.CENTER);
		content.add(buildStatus(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		toolbar.setOpaque(false);

		menuButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");
		refreshButton = AppTheme.primaryButton("Refresh");
		updateStatusButton = AppTheme.secondaryButton("Update Status");
		statusFilterBox = new JComboBox<String>(new String[] { "All", "reserved", "occupied", "completed",
				"cancelled" });
		floorFilterBox = new JComboBox<String>(new String[] { "All", "Ground", "Second", "Third" });
		statusUpdateBox = new JComboBox<String>(new String[] { "reserved", "occupied", "completed", "cancelled" });

		menuButton.addActionListener(this);
		logoutButton.addActionListener(this);
		refreshButton.addActionListener(this);
		updateStatusButton.addActionListener(this);
		statusFilterBox.addActionListener(this);
		floorFilterBox.addActionListener(this);

		toolbar.add(menuButton);
		toolbar.add(logoutButton);
		toolbar.add(AppTheme.label("Status"));
		toolbar.add(statusFilterBox);
		toolbar.add(AppTheme.label("Floor"));
		toolbar.add(floorFilterBox);
		toolbar.add(refreshButton);
		toolbar.add(AppTheme.label("Set"));
		toolbar.add(statusUpdateBox);
		toolbar.add(updateStatusButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Loading reservations...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadReservations() {
		DefaultTableModel model = new DefaultTableModel(new String[] { "Code", "User", "Customer", "Slot", "Floor",
				"Type", "Start", "End", "Status", "Plate", "QR / Verification Payload" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		try {
			java.time.LocalDate selectedDate = null;
			if (calendar != null && calendar.getDate() != null) {
				selectedDate = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
			List<ReservationRecord> reservations = repository.findReservations(null,
					(String) statusFilterBox.getSelectedItem(), (String) floorFilterBox.getSelectedItem(), selectedDate);
			for (ReservationRecord reservation : reservations) {
				model.addRow(new Object[] { reservation.getReservationCode(), reservation.getUsername(),
						reservation.getCustomerName(), reservation.getSlotId(), reservation.getFloor(),
						reservation.getSlotType(), DISPLAY_TIME.format(reservation.getStartTime()),
						DISPLAY_TIME.format(reservation.getEndTime()), reservation.getStatus(),
						reservation.getVehiclePlate(), reservation.getQrPayload() });
			}
			table.setModel(model);
			statusLabel.setText(reservations.size() + " reservation record(s) loaded.");
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load reservations.", error);
		}
	}

	private void updateSelectedReservation() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			statusLabel.setText("Select a reservation before updating status.");
			return;
		}

		String reservationCode = String.valueOf(table.getValueAt(selectedRow, 0));
		try {
			repository.updateReservationStatus(reservationCode, (String) statusUpdateBox.getSelectedItem(),
					SessionContext.username());
			loadReservations();
			statusLabel.setText("Reservation " + reservationCode + " updated.");
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to update reservation.", error);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == refreshButton || source == statusFilterBox || source == floorFilterBox) {
			loadReservations();
		} else if (source == updateStatusButton) {
			updateSelectedReservation();
		} else if (source == menuButton) {
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		} else if (source == logoutButton) {
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			customer_reservation app = new customer_reservation();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Customer Reservation", 1180, 700);
		});
	}
}
