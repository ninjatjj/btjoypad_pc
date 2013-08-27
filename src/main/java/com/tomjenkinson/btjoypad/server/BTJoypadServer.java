package com.tomjenkinson.btjoypad.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.JFrame;

public class BTJoypadServer extends Thread {
	public static final Integer ANALOG_LEFT_UP = 210;
	public static final Integer ANALOG_LEFT_DOWN = 211;
	public static final Integer ANALOG_LEFT_LEFT = 212;
	public static final Integer ANALOG_LEFT_RIGHT = 213;
	public static final Integer ANALOG_RIGHT_UP = 214;
	public static final Integer ANALOG_RIGHT_DOWN = 215;
	public static final Integer ANALOG_RIGHT_LEFT = 216;
	public static final Integer ANALOG_RIGHT_RIGHT = 217;
	public static final Integer ANALOG_MID = 218;

	static Map<Integer, Integer> androidKeyCodeMappings = new HashMap<Integer, Integer>();
	static {
		androidKeyCodeMappings.put(99, KeyEvent.VK_CONTROL);
		androidKeyCodeMappings.put(100, KeyEvent.VK_ALT);
		androidKeyCodeMappings.put(102, KeyEvent.VK_SPACE);
		androidKeyCodeMappings.put(23, KeyEvent.VK_SHIFT);
		androidKeyCodeMappings.put(4, KeyEvent.VK_Z);
		androidKeyCodeMappings.put(103, KeyEvent.VK_X);

		androidKeyCodeMappings.put(109, KeyEvent.VK_5);
		androidKeyCodeMappings.put(108, KeyEvent.VK_1);

		androidKeyCodeMappings.put(20, KeyEvent.VK_DOWN);
		androidKeyCodeMappings.put(21, KeyEvent.VK_LEFT);
		androidKeyCodeMappings.put(22, KeyEvent.VK_RIGHT);
		androidKeyCodeMappings.put(19, KeyEvent.VK_UP);

		androidKeyCodeMappings.put(82, KeyEvent.VK_M);
		androidKeyCodeMappings.put(84, KeyEvent.VK_Q);

		androidKeyCodeMappings.put(ANALOG_LEFT_UP, KeyEvent.VK_W);
		androidKeyCodeMappings.put(ANALOG_LEFT_DOWN, KeyEvent.VK_S);
		androidKeyCodeMappings.put(ANALOG_LEFT_LEFT, KeyEvent.VK_A);
		androidKeyCodeMappings.put(ANALOG_LEFT_RIGHT, KeyEvent.VK_D);
		androidKeyCodeMappings.put(ANALOG_RIGHT_UP, KeyEvent.VK_I);
		androidKeyCodeMappings.put(ANALOG_RIGHT_DOWN, KeyEvent.VK_K);
		androidKeyCodeMappings.put(ANALOG_RIGHT_LEFT, KeyEvent.VK_J);
		androidKeyCodeMappings.put(ANALOG_RIGHT_RIGHT, KeyEvent.VK_L);
		androidKeyCodeMappings.put(ANALOG_MID, KeyEvent.VK_ENTER);
	}

	private static final int EXIT_CMD = -1;
	private static final byte SHOW_CONFIG = 0;
	private static final byte KEYDOWN = 1;
	private static final byte KEYUP = 2;
	private StreamConnectionNotifier streamConnNotifier;
	private StreamConnection connection;
	private volatile boolean closed;

	private Robot robot = null;
	private DataInputStream bReader;

	public BTJoypadServer() throws IOException {

		try {
			robot = new Robot();
		} catch (AWTException e1) {
			System.out.println("Could not create robot");
			System.exit(-1);
		}

		File file = new File("server-mappings.txt");
		Properties mappings = new Properties();
		if (file.exists()) {
			InputStream in;
			try {
				in = new FileInputStream(file);
				mappings.load(in);
				in.close();
			} catch (Exception e) {
				System.out.println(new Date()
						+ "Could not load contact mappings");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		Iterator<Entry<Object, Object>> iterator = mappings.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<Object, Object> next = iterator.next();
			androidKeyCodeMappings.put(
					Integer.parseInt((String) next.getKey()),
					Integer.parseInt((String) next.getValue()));
		}

		LocalDevice localDevice = LocalDevice.getLocalDevice();
		localDevice.setDiscoverable(DiscoveryAgent.GIAC);

		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());
	}

	public void run() {
		UUID uuid = new UUID("86AF17F88A504D7F9F234FD16E4E072A", false);
		// UUID uuid = new UUID("ffffffffcdc49e7dffffffffcdc49e7d", false);
		String connectionString = "btspp://localhost:" + uuid
				+ ";name=Sample SPP Server";

		try {
			streamConnNotifier = (StreamConnectionNotifier) Connector
					.open(connectionString);

			while (!closed) {
				try {
					System.out.println("waiting for connection...");
					connection = streamConnNotifier.acceptAndOpen();

					RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
					System.out.println("Remote device address: "
							+ dev.getBluetoothAddress());
					System.out.println("Remote device name: "
							+ dev.getFriendlyName(true));

					// Perform handshake
					InputStream inStream = connection.openInputStream();
					bReader = new DataInputStream(inStream);

					bReader.readLine();

					OutputStream outStream = connection.openOutputStream();
					PrintWriter pWriter = new PrintWriter(
							new OutputStreamWriter(outStream));
					pWriter.write("btjoypadserver\r\n");
					pWriter.flush();
					pWriter.close();

					while (!closed) {
						byte command = bReader.readByte();
						System.out.println("read command: " + command);
						boolean down = true;
						if (command == EXIT_CMD) {
							System.out.println("client EXIT_CMD");
							break;
						} else if (command == SHOW_CONFIG) {
							new JFrame("Not supported").setVisible(true);
							continue;
						} else if (command == KEYDOWN) {
							down = true;
						} else if (command == KEYUP) {
							down = false;
						} else {
							throw new Exception("Unexpected command: "
									+ command);
						}
						int keyCode = bReader.read();
						if (down) {
							robot.keyPress(androidKeyCodeMappings.get(keyCode));
						} else {
							robot.keyRelease(androidKeyCodeMappings
									.get(keyCode));
						}
					}
				} catch (Exception e) {
					if (connection != null) {
						try {
							connection.close();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						connection = null;
					}
					e.printStackTrace();
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	public void close() {
		closed = true;
		if (bReader != null) {
			try {
				bReader.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bReader = null;
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection = null;
		}
		if (streamConnNotifier != null) {
			try {
				streamConnNotifier.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			streamConnNotifier = null;
		}
	}

	public static void main(String[] args) throws IOException {
		BTJoypadServer btJoypadServer = new BTJoypadServer();
		btJoypadServer.run();
	}
}