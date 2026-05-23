package adminmanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class parking_slot extends JFrame implements ActionListener{
	private JLabel id;
	private JTextField tid;
	private JTextArea table;
	private JLabel cusprof;
	public JButton logout, menu1, search, reset, parking;
	private JLabel title;
	
	Font f1 = new Font("Times New Roman", Font.BOLD,17);
	Font f2 = new Font("Arial", Font.ITALIC,13);
	Font f3 = new Font("Times New Roman", Font.PLAIN,15);
	
	Color c1 = Color.GRAY;
	Color c2 = Color.WHITE;
	Color c3 = Color.BLACK;
	
	private ImageIcon icon;
	private JLabel label;
	private JLabel photo;
	private JLabel lblClock;
	
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
	
	public parking_slot () {
		
		icon = new ImageIcon("src/m.jpg");
		
		Container con = getContentPane();
		con.setLayout(null);
		
		photo = new JLabel() {
			  public void paintComponent(Graphics g) {
			    g.drawImage(icon.getImage(), 0, 0, null);
			    super.paintComponent(g);
			  }
			};
			
			photo.setOpaque(false);
			con.add(photo);
			 photo.setBounds(10,10,100,100);
			 
			 title = new JLabel ("Archim's TechCorner");
			 title.setBounds(100,-10,200,100);
			 con.add(title);
		
			con.add(lblClock = new JLabel(""));
			lblClock.setBounds(450,10,300,120);
			lblClock.setFont(f3);
		
			logout = new JButton("Logout");
			logout.addActionListener(this);
			logout.setBounds(580,10,100,40);
			con.add(logout);
			
			parking = new JButton("Reserve");
			parking.addActionListener(this);
			parking.setBounds(100,250,100,40);
			con.add(parking);
			
			search = new JButton("Show");
			search.addActionListener(this);
			search.setBounds(50,200,80,30);
			con.add(search);
			
			reset = new JButton("Refresh");
			reset.addActionListener(this);
			reset.setBounds(160,200,80,30);
			con.add(reset);
			
			menu1 = new JButton ("Back to Menu");
			menu1.addActionListener(this);
			menu1.setBounds(430,10,130,40);
			con.add(menu1);
			
			id = new JLabel ("Slot # :");
			id.setFont(f1);
			id.setBounds(40,170,60,20);
			con.add(id);
			
			tid = new JTextField ();
			tid.setFont(f3);
			tid.setBounds(100,170,150,20);
			con.add(tid);
			
			table = new JTextArea ();
			table.setFont(f3);
			table.setBounds(340,170,300,350);
			con.add(table);
			
			cusprof = new JLabel ("-------------------------------------------SLOT AVAILABILITY--------------------------------------");
			cusprof.setFont(f1);
			cusprof.setBounds(10,100,750,50);
			con.add(cusprof);

			Clock();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		 //Reset Button
	    if (e.getSource() == reset) {
	        tid.setText("");
	    }
		
	    //pag logout
	    if (e.getSource() == logout) {
			int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialog == JOptionPane.YES_OPTION) {
				 //pabalik ng login
				        login app = new login();
				        app.setTitle("Admin Login");
				        app.setVisible(true);
				        app.setSize(450,600); 
				        app.setVisible(true);
				    	app.setLocationRelativeTo(null);
				    	parking_slot.this.dispose();
				     
			}
			if (dialog == JOptionPane.NO_OPTION) {
				parking_slot.this.show();
			}
		}
		
	    
	    //pagbalik sa menu
	    if (e.getSource() == menu1) {
	    	//menu
	    	  menu app = new menu();
		        app.setTitle("Admin Menu");
		        app.setVisible(true);
		        app.setSize(1000,400); 
		        app.setVisible(true);
		    	app.setLocationRelativeTo(null);
		    	parking_slot.this.dispose();
	    }
	    
	    //reserve button
	    if (e.getSource() == parking) {
		park_inventory_management app = new park_inventory_management();
			app.setTitle("Parking Inventory Management");
			app.setVisible(true);
			app.setSize(1000, 700);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			parking_slot.this.dispose();
	    }
	}
	
	
	static parking_slot app = new parking_slot();
	public static void main(String[] args) {
		app.setTitle("Parking Slot");
		app.setSize(700, 600);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}

	
}
