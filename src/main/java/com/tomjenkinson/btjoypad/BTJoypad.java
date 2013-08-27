package com.tomjenkinson.btjoypad;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.tomjenkinson.btjoypad.server.BTJoypadServer;

public class BTJoypad {

	private BTJoypadServer server;

	private TrayIcon trayIcon;

	private MenuItem reconnect;

	public BTJoypad() throws AWTException, IOException {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
		} else {
			final PopupMenu popup = new PopupMenu();
			BufferedImage connectedImg = ImageIO.read(this.getClass()
					.getClassLoader().getResourceAsStream("drawable/icon.png"));
			final ImageIcon ci = new ImageIcon(connectedImg);

			trayIcon = new TrayIcon(ci.getImage().getScaledInstance(24, 24, 0),
					"btjoypad");
			final SystemTray tray = SystemTray.getSystemTray();

			// Create a pop-up menu components
			MenuItem exitMenuItem = new MenuItem("Exit");
			exitMenuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

			final MenuItem startServer = new MenuItem("Start server");
			final MenuItem stopServer = new MenuItem("Stop server");
			startServer.setEnabled(false);
			startServer.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					try {
						startServer.setEnabled(false);
						stopServer.setEnabled(true);
						server = new BTJoypadServer();
						server.start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			stopServer.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					server.close();
					startServer.setEnabled(true);
					stopServer.setEnabled(false);
				}
			});

			reconnect = new MenuItem("Start client");
			reconnect.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					try {
						new BTJoypadClient(BTJoypad.this);
						reconnect.setEnabled(false);
					} catch (Exception e1) {
						e1.printStackTrace();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e1.printStackTrace(pw);
						new JDialog((JDialog) null, e1.getClass().getName())
								.add(new JPanel().add(new JTextArea(sw
										.toString()))).setVisible(true);
					}
				}
			});

			Menu fileMenu = new Menu("File");
			fileMenu.add(exitMenuItem);
			fileMenu.add(startServer);
			fileMenu.add(stopServer);
			fileMenu.add(reconnect);

			// Add components to pop-up menu
			popup.add(fileMenu);

			trayIcon.setPopupMenu(popup);

			tray.add(trayIcon);

			server = new BTJoypadServer();
			server.start();
		}
	}

	public void onDisconnected() {
		reconnect.setEnabled(true);
	}

	public static void main(String[] args) throws AWTException, IOException {
		new BTJoypad();
	}
}
