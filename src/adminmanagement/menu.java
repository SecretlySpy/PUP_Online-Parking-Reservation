package adminmanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class menu extends JFrame implements ActionListener{
	
	public JButton user, bill, invent, pslot, logout, reserve;
	
	Font f1 = new Font("Times New Roman", Font.BOLD,17);
	Font f2 = new Font("Arial", Font.ITALIC,13);
	Color c1 = Color.GRAY;
	Color c2 = Color.BLACK;
	Color c3 = Color.WHITE;
	Color c4 = Color.DARK_GRAY;
	
public menu() {
	Container con = getContentPane();
	con.setLayout(null);
	
	user = new JButton ("CUSTOMER PROFILE");
	bill = new JButton ("BILLING");
	invent = new JButton ("PARK INVENTORY");
	pslot = new JButton ("PARKING SLOT");
	logout = new JButton ("LOGOUT");
	reserve = new JButton ("CUSTOMER RESERVATION");
	
	user.addActionListener(this);
	bill.addActionListener(this);
	invent.addActionListener(this);
	pslot.addActionListener(this);
	logout.addActionListener(this);
	reserve.addActionListener(this);

	user.setBounds(10,90,250,30);
	bill.setBounds(10,130,250,30);
	invent.setBounds(10,170,250,30);
	pslot.setBounds(10,210,250,30);
	logout.setBounds(800,10,170,30);
	reserve.setBounds(10,250,250,30);

	user.setFont(f1);
	bill.setFont(f1);
	invent.setFont(f1);
	pslot.setFont(f1);
	logout.setFont(f1);
	reserve.setFont(f1);
	
	user.setForeground(c2);
	bill.setForeground(c2);
	invent.setForeground(c2);
	pslot.setForeground(c2);
	logout.setForeground(c2);
	reserve.setForeground(c2);
	
	con.setBackground(c4);

	con.add(user);
	con.add(bill);
	con.add(invent);
	con.add(pslot);
	con.add(logout);
	con.add(reserve);
	
	}

@Override
public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	//paglogout
	if (e.getSource() == logout) {
		int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (dialog == JOptionPane.YES_OPTION) {
			//pabalik ng login.java
	        login app = new login();
	        app.setVisible(true);
	        app.setSize(450,600); 
	        app.setVisible(true);
	    	app.setLocationRelativeTo(null);
	    	menu.this.dispose();
		}
		if (dialog == JOptionPane.NO_OPTION) {
			menu.this.show();
		}
	}
	
	//papuntang billing
		if (e.getSource() == bill)   {
	        billingmanagement app = new billingmanagement();
	        app.setTitle("Billing Management");
	        app.setVisible(true);
	        app.setSize(1000,700);
	    	app.setLocationRelativeTo(null);
	    	menu.this.dispose();
	    } 
	
	//papuntang inventory
		if (e.getSource() == invent)  {
		    	park_inventory_management app = new park_inventory_management();
		    	app.setTitle("Parking Inventory Management");
		    	app.setVisible(true);
		        app.setSize(1000,700);
		    	app.setLocationRelativeTo(null);
		    	menu.this.dispose();
		    } 
	
		
	//papuntang customer profile
		if (e.getSource() == user) {
			customer_profile app = new customer_profile();
			app.setTitle("Customer Profile");
			app.setSize(1000, 600);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			menu.this.dispose();
		}
		
	//papuntang slot 	
		if (e.getSource() == pslot) {
			parking_slot app = new parking_slot();
			app.setTitle("Parking Slot");
			app.setSize(700, 600);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			menu.this.dispose();
		}
		
	//papuntang reservation	
		if (e.getSource() == reserve) {
			customer_reservation app = new customer_reservation();
			app.setTitle("Customer Reservation");
			app.setSize(1000, 400);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			menu.this.dispose();
		}
	}	


static menu app = new menu();
public static void main(String[] args) {
app.setTitle("Admin Menu");
app.setSize(1000,400);
app.setVisible(true);
app.setLocationRelativeTo(null);
}
}
