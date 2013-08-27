package com.tomjenkinson.btjoypad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class BTJoypadClient implements DiscoveryListener {

	private static Object lock = new Object();
	private static Vector<RemoteDevice> devices = new Vector<RemoteDevice>();
	private static String connectionURL = null;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		new BTJoypadClient();
	}

	public BTJoypadClient() throws IOException, InterruptedException {
		this(null);
	}

	public BTJoypadClient(BTJoypad btJoypad) throws IOException,
			InterruptedException {
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());
		DiscoveryAgent agent = localDevice.getDiscoveryAgent();

		boolean connect = canConnect(this, localDevice, agent);

		if (connect) {

			System.out.println("Connecting to: " + connectionURL);

			StreamConnection streamConnection = (StreamConnection) Connector
					.open(connectionURL);

			// handshake
			OutputStream outStream = streamConnection.openOutputStream();
			PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(
					outStream));
			pWriter.write("btjoypadpc\r\n");
			pWriter.flush();
			InputStream inStream = streamConnection.openInputStream();
			BufferedReader bReader2 = new BufferedReader(new InputStreamReader(
					inStream));
			String lineRead = bReader2.readLine();
			System.out.println(lineRead);
			new ClientHandler(streamConnection, pWriter, bReader2, btJoypad);
		}
	}

	public void deviceDiscovered(RemoteDevice remoteDevice,
			DeviceClass deviceClass) {
		if (!devices.contains(remoteDevice)) {
			devices.addElement(remoteDevice);
		}
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		if (servRecord != null && servRecord.length > 0) {
			connectionURL = servRecord[0].getConnectionURL(
					ServiceRecord.AUTHENTICATE_ENCRYPT, false);
		}
		// synchronized (lock) {
		// lock.notify();
		// }
	}

	private static boolean canConnect(BTJoypadClient client,
			LocalDevice localDevice, DiscoveryAgent agent) throws IOException {
		RemoteDevice remoteDevice = null;
		File oldConnection = new File("connection.txt");
		if (oldConnection.exists()) {
			BufferedReader fileReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(oldConnection)));
			String remoteDeviceName = fileReader.readLine();
			fileReader.close();
			remoteDevice = client.new MyRemoteDevice(remoteDeviceName);
		} else {
			System.out.println("Starting device inquiry...");
			agent.startInquiry(DiscoveryAgent.GIAC, client);

			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Device Inquiry Completed. ");

			int deviceCount = devices.size();

			if (deviceCount <= 0) {
				System.out.println("No Devices Found .");
				System.exit(0);
			} else {
				System.out.println("Bluetooth Devices: ");
				for (int i = 0; i < deviceCount; i++) {
					RemoteDevice found = (RemoteDevice) devices.elementAt(i);
					String addr = found.getBluetoothAddress();
					String friendly = "protected";
					try {
						friendly = found.getFriendlyName(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println((i + 1) + ". " + addr + " (" + friendly
							+ ")");
				}
			}

			System.out.print("Choose Device index: ");
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					System.in));

			String chosenIndex = bReader.readLine();
			int index = Integer.parseInt(chosenIndex.trim());

			remoteDevice = (RemoteDevice) devices.elementAt(index - 1);

			File newConnection = new File("connection.txt");
			newConnection.createNewFile();
			FileWriter fw = new FileWriter(newConnection);
			fw.append(remoteDevice.getBluetoothAddress());
			fw.close();
		}

		UUID[] uuidSet = new UUID[1];
		uuidSet[0] = new UUID("86AF17F88A504D7F9F234FD16E4E072A", false);

		System.out.println("\nSearching for service...");
		agent.searchServices(null, uuidSet, remoteDevice, client);

		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (connectionURL == null) {
			System.out.println("Device does not support BTJoypad");
			System.out.println("Try again (y/n)?");
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					System.in));

			String chosenIndex = bReader.readLine();
			if (chosenIndex.equalsIgnoreCase("y")) {
				oldConnection.delete();
				return canConnect(client, localDevice, agent);
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void inquiryCompleted(int discType) {
		synchronized (lock) {
			lock.notify();
		}

	}

	private class MyRemoteDevice extends RemoteDevice {

		protected MyRemoteDevice(String remoteDeviceName) {
			super(remoteDeviceName);
		}
	}
}