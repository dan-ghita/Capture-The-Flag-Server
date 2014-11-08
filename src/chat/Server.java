package chat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Server extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;

	private String UID;
	private String flag;
	private boolean flagPresent;
	private String flagToken;
	private String localHost = "127.0.0.1";

	private ArrayList<String> IPList = new ArrayList<String>();
	InetAddress myIP;

	// constructor
	public Server(String fName, String lName, ArrayList <String> _IPList, boolean set_flag) {
		super("Server side");

		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand());
				userText.setText("");
			}
		});
		add(userText, BorderLayout.NORTH);

		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));

		setSize(300, 150);
		setVisible(true);

		UID = generateUID(fName, lName);
		// System.out.println(UID);

		flag = new String();
		flagPresent = false;
		flagToken = new String();

		try {
			myIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		IPList = _IPList;
		IPList.remove(myIP.getHostAddress());
		
		System.out.println(myIP.getHostAddress());
		
		if(set_flag)
			hideFlag("abcdef");
	}

	public void startRunning() {

		try {
			server = new ServerSocket(8888, 100);
			while (true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();

				} catch (EOFException eofException) {
					showMessage("\n");
				} finally {
					closeCrap();
				}
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private String generateUID(String fName, String lName) {
		int randomLength;
		String randomSet = new String("abcdefghijklmnopqrstuvwxyz0123456789");
		String result = new String();
		Random rand = new Random();
		int randomIndex;

		result += fName.charAt(0) + lName + ".";
		randomLength = Math.abs(rand.nextInt() % 16);

		// System.out.println(randomLength);
		// System.out.println(randomSet.length());

		for (int i = 0; i < randomLength; i++) {
			randomIndex = Math.abs(rand.nextInt() % randomSet.length());

			// System.out.println(randomIndex);
			result += randomSet.charAt(randomIndex);
		}

		// System.out.println(result);

		return result;
	}

	private String generateToken() {
		String randomSet = new String("abcdefghijklmnopqrstuvwxyz0123456789");
		String result = new String();
		Random rand = new Random();
		int randomIndex;

		for (int i = 0; i < 16; i++) {
			randomIndex = Math.abs(rand.nextInt() % randomSet.length());

			// System.out.println(randomIndex);
			result += randomSet.charAt(randomIndex);
		}
		return result;

	}

	private void whoAreYou() {
		sendMessage(UID);
	}

	private void haveFlag() {
		if (flagPresent == false)
			sendMessage("NO");
		else {
			sendMessage("YES " + flagToken);
		}
	}
	
	private void nextServer() {
		Random rand = new Random();
		int nextIPIndex;
		do{
			nextIPIndex = Math.abs(rand.nextInt() % IPList.size());
		}while(IPList.get(nextIPIndex).equals(myIP.getHostAddress()) || IPList.get(nextIPIndex).equals(localHost));		
		sendMessage(IPList.get(nextIPIndex));
		
	}

	private void captureFlag(String clientToken) {
		//System.out.println(clientToken);
		if (flagToken.equals(clientToken)) {
			sendMessage("FLAG:" + flag);
			flagPresent = false;
		} else {
			sendMessage("ERR: You're trying to trick me!");
		}
	}

	private void hideFlag(String clientFlag) {
		flagPresent = true;
		flag = clientFlag;
		flagToken = generateToken();
	}

	private void waitForConnection() throws IOException {
		connection = server.accept();
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
	}

	private void whileChatting() throws IOException {
		String clientFlag = new String();
		String clientToken = new String();
		String message = " You are now connected! ";

		ableToType(true);
		do {
			try {

				message = (String) input.readObject();

				if (message.startsWith("hide_flag")) {
					clientFlag = message.substring(10, message.length());
					message = "hide_flag";
				}

				if (message.startsWith("capture_flag")) {
					clientToken = message.substring(13, message.length());
					message = "capture_flag";
				}

				switch (message) {
				case "who_are_you?":
					whoAreYou();
					break;
				case "have_flag?":
					haveFlag();
					break;
				case "capture_flag":
					captureFlag(clientToken);
					break;
				case "hide_flag":
					hideFlag(clientFlag);
					break;
				case "next_server":
					nextServer();
					break;
				default:
					break;
				}

			} catch (ClassNotFoundException classNotFoundException) {
				showMessage("\n Unrecognised  \n");
			}
		} while (!message.equals("CLIENT - END"));
	}

	private void closeCrap() {
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			output.writeObject(message);
			output.flush();
		} catch (IOException ioException) {
			chatWindow.append("\n ERROR \n");
		}
	}

	private void showMessage(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append(text);
			}
		});
	}

	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				userText.setEditable(tof);
			}
		});
	}

}
