package adminmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import DatabaseConnection.ConnectionDB;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class park_inventory_management extends JFrame implements ActionListener {
	
	public JLabel id, cus, tr, lrd;
	public JTextField t1, total, date;
	public JButton search, add1, logout, menu1;
	public JTable table;
	public Connection conn = null;
	public Statement st = null;
	public PreparedStatement ps = null;
	public ResultSet rs = null;
	public DefaultTableModel dm = null;
	public ResultSetMetaData rsmd = null;
	Font f1 = new Font("Times New Roman",Font.PLAIN, 15);
	Font f2 = new Font("Times New Roman",Font.BOLD, 20);
	Font f3 = new Font("Times New Roman", Font.PLAIN,15);
	Color c1 = Color.WHITE;
	Color c2 = Color.RED;
	Color c3 = Color.MAGENTA;
	
	private JLabel lblClock;
	private ImageIcon icon;
	private JLabel label;
	private JLabel photo;
	private JLabel title;
	
	public void Clock() {
		Thread clock = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Calendar cal = new GregorianCalendar();
						int day = cal.get(Calendar.DAY_OF_MONTH);
						int month = cal.get(Calendar.MONTH);
						int year = cal.get(Calendar.YEAR);

						int second = cal.get(Calendar.SECOND);
						int minute = cal.get(Calendar.MINUTE);
						int hour = cal.get(Calendar.HOUR);

						lblClock.setText("Time " + hour + " : " + minute + " : " + second + " Date " + year + " / "
								+ (month + 1) + " / " + day);
						sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		clock.start();
	}	
	
public park_inventory_management() {
	conn = ConnectionDB.getConnection();
	icon = new ImageIcon("src/m.jpg");
	
	Container con = getContentPane();
	con.setLayout(null);
	
	photo= new JLabel() {
		  public void paintComponent(Graphics g) {
		    g.drawImage(icon.getImage(), 0, 0, null);
		    super.paintComponent(g);
		  }
		};
		
		photo.setOpaque(false);
		con.add(photo);
		photo.setBounds(10,10,100,100);

	
	con.add(lblClock = new JLabel(""));
	lblClock.setBounds(730, 10, 230, 30);
	lblClock.setFont(f3);
	
	title = new JLabel ("Archim's TechCorner");
	title.setBounds(100,-10,200,100);
	con.add(title);
	
	id = new JLabel ("ID:");
	id.setFont(f2);
	cus = new JLabel ("--------------------CUSTOMER INFORMATION---------------------");
	cus.setFont(f2);
	tr = new JLabel ("Total Reservation");
	lrd = new JLabel ("Last Reservation Date");
	t1 = new JTextField ();
	total = new JTextField ();
	date = new JTextField ();
	
	tr.setFont(f2);
	lrd.setFont(f2);
	
	
	t1.setEditable(false);
	t1.setBackground(c1);
	t1.setFont(f1);
	total.setEditable(false);
	total.setBackground(c2);
	total.setFont(f2);
	total.setForeground(c1);
	date.setEditable(false);
	date.setBackground(c3);
	date.setFont(f2);
	date.setForeground(c1);
	
	search = new JButton ("Search");
	add1 = new JButton ("Add");
	logout = new JButton ("LOGOUT");
	menu1 = new JButton ("Back to Menu");
	search.addActionListener(this);
	add1.addActionListener(this);
	logout.addActionListener(this);
	menu1.addActionListener(this);
	
	table = new JTable ();
	
	id.setBounds(40,100,100,20);
	cus.setBounds(400,120,1000,20);
	tr.setBounds(120,250,300,20);
	lrd.setBounds(100,450,300,20);
	t1.setBounds(120,100,200,20);
	total.setBounds(40,300,300,100);
	date.setBounds(40,500,300,100);
	search.setBounds(60,180,100,30);
	add1.setBounds(200,180,100,30);
	table.setBounds(400,150,550,450);
	logout.setBounds(850,50,100,40);
	menu1.setBounds(690,50,130,40);
	
	
	con.add(id);
	con.add(cus);
	con.add(lrd);
	con.add(tr);
	con.add(t1);
	con.add(total);
	con.add(date);
	con.add(search);
	con.add(add1);
	con.add(table);
	con.add(logout);
	con.add(menu1);
	
	 Clock();
	
}

@Override
public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	//paglogout
			if (e.getSource() == logout) {
				int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
						JOptionPane.YES_NO_OPTION);
				if (dialog == JOptionPane.YES_OPTION) {
					 //pabalik ng login
					        login app = new login();
					        app.setTitle("Admin Login");
					        app.setVisible(true);
					        app.setSize(450,600); 
					        app.setVisible(true);
					    	app.setLocationRelativeTo(null);
					        park_inventory_management.this.dispose();
				}
				if (dialog == JOptionPane.NO_OPTION) {
					park_inventory_management.this.show();
				}
			}
			
			//pabalik ng menu

			//pagbalik sa menu
		    if (e.getSource() == menu1) {
		    	//menu
		    	  menu app = new menu();
			        app.setTitle("Admin Menu");
			        app.setVisible(true);
			        app.setSize(1000,400); 
			        app.setVisible(true);
			    	app.setLocationRelativeTo(null);
			    	park_inventory_management.this.dispose();
		    }
	
		  //add button
			if  (e.getSource() == add1) {
				parking_inventory_management_add_button app = new parking_inventory_management_add_button();
					app.setTitle("Parking Inventory Management");
					app.setVisible(true);
					app.setSize(1200, 750);
					app.setVisible(true);
					app.setLocationRelativeTo(null);
					park_inventory_management.this.dispose();
			}
		//search button
			if  (e.getSource() == search){
			    	
				 String m = JOptionPane.showInputDialog(null, "Type the ID #", 
		                "MANALAD", JOptionPane.INFORMATION_MESSAGE);
				 t1.setText(m);
			    }	
		
		
		    
}




static park_inventory_management app = new park_inventory_management();
public static void main(String[] args) {
	app.setTitle("Parking Inventory Management");
	app.setSize(1000, 700);
	app.setVisible(true);
	app.setLocationRelativeTo(null);
	app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
}
}
