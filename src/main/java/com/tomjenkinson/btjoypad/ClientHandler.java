package com.tomjenkinson.btjoypad;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.io.StreamConnection;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ClientHandler extends JFrame implements WindowListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel keyLabel = new JLabel("Send keys");
	private boolean dead;
	private StreamConnection streamConnection;
	private PrintWriter printWriter;
	private BTJoypad btJoypad;

	private static final byte EXIT_CMD = -1;
	private static final byte KEYDOWN = 1;
	private static final byte KEYUP = 2;

	private static Map<Integer, Integer> androidToPCKeyCodes = new HashMap<Integer, Integer>();
	static {
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_A,
				android.view.KeyEvent.KEYCODE_A);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_B,
				android.view.KeyEvent.KEYCODE_B);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_C,
				android.view.KeyEvent.KEYCODE_C);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_D,
				android.view.KeyEvent.KEYCODE_D);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_E,
				android.view.KeyEvent.KEYCODE_E);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_F,
				android.view.KeyEvent.KEYCODE_F);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_G,
				android.view.KeyEvent.KEYCODE_G);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_H,
				android.view.KeyEvent.KEYCODE_H);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_I,
				android.view.KeyEvent.KEYCODE_I);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_J,
				android.view.KeyEvent.KEYCODE_J);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_K,
				android.view.KeyEvent.KEYCODE_K);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_L,
				android.view.KeyEvent.KEYCODE_L);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_M,
				android.view.KeyEvent.KEYCODE_M);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_N,
				android.view.KeyEvent.KEYCODE_N);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_O,
				android.view.KeyEvent.KEYCODE_O);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_P,
				android.view.KeyEvent.KEYCODE_P);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_Q,
				android.view.KeyEvent.KEYCODE_Q);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_R,
				android.view.KeyEvent.KEYCODE_R);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_S,
				android.view.KeyEvent.KEYCODE_S);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_T,
				android.view.KeyEvent.KEYCODE_T);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_U,
				android.view.KeyEvent.KEYCODE_U);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_V,
				android.view.KeyEvent.KEYCODE_V);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_W,
				android.view.KeyEvent.KEYCODE_W);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_X,
				android.view.KeyEvent.KEYCODE_X);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_Y,
				android.view.KeyEvent.KEYCODE_Y);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_Z,
				android.view.KeyEvent.KEYCODE_Z);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_1,
				android.view.KeyEvent.KEYCODE_1);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_2,
				android.view.KeyEvent.KEYCODE_2);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_3,
				android.view.KeyEvent.KEYCODE_3);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_4,
				android.view.KeyEvent.KEYCODE_4);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_5,
				android.view.KeyEvent.KEYCODE_5);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_6,
				android.view.KeyEvent.KEYCODE_6);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_7,
				android.view.KeyEvent.KEYCODE_7);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_8,
				android.view.KeyEvent.KEYCODE_8);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_9,
				android.view.KeyEvent.KEYCODE_9);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_0,
				android.view.KeyEvent.KEYCODE_0);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_SHIFT,
				android.view.KeyEvent.KEYCODE_SHIFT_LEFT);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_ENTER,
				android.view.KeyEvent.KEYCODE_ENTER);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_COMMA,
				android.view.KeyEvent.KEYCODE_COMMA);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_PERIOD,
				android.view.KeyEvent.KEYCODE_PERIOD);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_TAB,
				android.view.KeyEvent.KEYCODE_TAB);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_SLASH,
				android.view.KeyEvent.KEYCODE_SLASH);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_QUOTE,
				android.view.KeyEvent.KEYCODE_APOSTROPHE);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_CONTROL,
				android.view.KeyEvent.KEYCODE_DPAD_CENTER);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_ESCAPE,
				android.view.KeyEvent.KEYCODE_BACK);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_ALT,
				android.view.KeyEvent.KEYCODE_MENU);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_LEFT,
				android.view.KeyEvent.KEYCODE_DPAD_LEFT);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_RIGHT,
				android.view.KeyEvent.KEYCODE_DPAD_RIGHT);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_UP,
				android.view.KeyEvent.KEYCODE_DPAD_UP);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_DOWN,
				android.view.KeyEvent.KEYCODE_DPAD_DOWN);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_BACK_SPACE,
				android.view.KeyEvent.KEYCODE_DEL);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_SPACE,
				android.view.KeyEvent.KEYCODE_SPACE);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_ALT_GRAPH,
				android.view.KeyEvent.KEYCODE_HOME);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_SEMICOLON,
				android.view.KeyEvent.KEYCODE_SEMICOLON);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_MINUS,
				android.view.KeyEvent.KEYCODE_MINUS);
		androidToPCKeyCodes.put(java.awt.event.KeyEvent.VK_EQUALS,
				android.view.KeyEvent.KEYCODE_EQUALS);
	}

	public ClientHandler(StreamConnection streamConnection,
			PrintWriter printWriter, BufferedReader bReader2, BTJoypad btJoypad) {
		super("Send keys");
		setSize(300, 200);
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setFocusable(true);
		this.streamConnection = streamConnection;
		this.printWriter = printWriter;
		this.btJoypad = btJoypad;
		MyKeyAdapter monitor = new MyKeyAdapter(this);
		addKeyListener(monitor);
		addWindowListener(this);
		add(keyLabel);
		new DisconnectionHandler(this, bReader2).start();
		setVisible(true);
	}

	public void windowClosing(WindowEvent arg0) {
		printWriter.write(EXIT_CMD);
		printWriter.flush();

		try {
			streamConnection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (btJoypad != null) {
			btJoypad.onDisconnected();
		}
		dispose();
	}

	public void dead() {

		keyLabel.setText("dead");
		repaint();
		dead = true;
	}

	public boolean isDead() {
		return dead;
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowActivated(WindowEvent arg0) {

	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

	class MyKeyAdapter extends KeyAdapter {
		ClientHandler display;

		MyKeyAdapter(ClientHandler display) {
			this.display = display;
		}

		public void keyPressed(KeyEvent event) {
			if (!display.isDead()) {
				if (ClientHandler.androidToPCKeyCodes.containsKey(event
						.getKeyCode())) {
					Integer integer = ClientHandler.androidToPCKeyCodes
							.get(event.getKeyCode());
					printWriter.write(KEYDOWN);
					printWriter.write(integer);
					printWriter.flush();
					display.keyLabel.setText("down " + integer);
				} else {
					display.keyLabel.setText("down (UNKNOWN) "
							+ event.getKeyChar());
				}
				display.repaint();
			}
		}

		public void keyReleased(KeyEvent event) {
			if (!display.isDead()) {
				if (ClientHandler.androidToPCKeyCodes.containsKey(event
						.getKeyCode())) {
					int keyCode = event.getKeyCode();
					Integer integer = ClientHandler.androidToPCKeyCodes
							.get(keyCode);
					printWriter.write(KEYUP);
					printWriter.write(integer);
					printWriter.flush();
					display.keyLabel.setText("up   " + event.getKeyChar());
					display.keyLabel.setText("up   " + integer);
				} else {
					display.keyLabel.setText("up   (UNKNOWN) "
							+ event.getKeyChar());
				}
				display.repaint();
			}
		}
	}

	private class DisconnectionHandler extends Thread {
		ClientHandler display;

		private BufferedReader breader;

		public DisconnectionHandler(ClientHandler clientHandler,
				BufferedReader bReader2) {
			this.display = clientHandler;
			this.breader = bReader2;
		}

		@Override
		public void run() {
			try {
				breader.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			display.dead();
		}
	}
}
