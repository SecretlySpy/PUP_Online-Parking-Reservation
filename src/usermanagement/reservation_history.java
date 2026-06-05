package usermanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.ReservationRecord;
import app.ReservationRepository;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Customer view for past and upcoming reservations with verification payloads.
 */
public class reservation_history extends JFrame implements ActionListener {
	private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

	private String username;
	private ReservationRepository repository;
	private JTable table;
	private JLabel statusLabel;
	private JButton refreshButton;
	private JButton menuButton;
	private JButton bookButton;
	private JButton deleteButton;

	public reservation_history(String username) {
		AppTheme.install();
		this.username = username;
		this.repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Reservation History", "Review booking codes, status, and verification payloads.",
				buildContent()));
		loadHistory();
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

		refreshButton = AppTheme.primaryButton("Refresh");
		bookButton = AppTheme.secondaryButton("New Reservation");
		deleteButton = AppTheme.dangerButton("Delete");
		menuButton = AppTheme.secondaryButton("Back to Menu");

		refreshButton.addActionListener(this);
		bookButton.addActionListener(this);
		deleteButton.addActionListener(this);
		menuButton.addActionListener(this);

		toolbar.add(refreshButton);
		toolbar.add(bookButton);
		toolbar.add(deleteButton);
		toolbar.add(menuButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Loading reservation history...");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadHistory() {
		DefaultTableModel model = new DefaultTableModel(new String[] { "Code", "Slot", "Floor", "Type", "Start",
				"End", "Status", "QR / Verification Payload" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		try {
			List<ReservationRecord> reservations = repository.findReservations(username, "All", "All");
			for (ReservationRecord reservation : reservations) {
				model.addRow(new Object[] { reservation.getReservationCode(), reservation.getSlotId(),
						reservation.getFloor(), reservation.getSlotType(), DISPLAY_TIME.format(reservation.getStartTime()),
						DISPLAY_TIME.format(reservation.getEndTime()), reservation.getStatus(),
						reservation.getQrPayload() });
			}
			table.setModel(model);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			statusLabel.setText(reservations.size() + " reservation record(s) loaded.");
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load reservation history.", error);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == refreshButton) {
			loadHistory();
		} else if (source == bookButton) {
			reservation app = new reservation(username);
			AppTheme.showFrame(app, "Book Parking Slot", 1180, 720);
			dispose();
		} else if (source == deleteButton) {
			deleteSelectedReservation();
		} else if (source == menuButton) {
			menu app = new menu(username);
			AppTheme.showFrame(app, "User Menu", 980, 540);
			dispose();
		}
	}

	private void deleteSelectedReservation() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			statusLabel.setText("Select a reservation before deleting.");
			return;
		}

		String reservationCode = String.valueOf(table.getValueAt(selectedRow, 0));
		int answer = JOptionPane.showConfirmDialog(this, "Delete reservation " + reservationCode + "?",
				"Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			repository.deleteReservation(reservationCode, username);
			loadHistory();
			statusLabel.setText("Reservation " + reservationCode + " deleted.");
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to delete reservation.", error);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			reservation_history app = new reservation_history("Harayuri");
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Reservation History", 1080, 640);
		});
	}
}
