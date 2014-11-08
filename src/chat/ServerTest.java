package chat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

public class ServerTest {

	public static void main(String[] args) {
		String line = new String();
		ArrayList<String> IPList = new ArrayList<String>();

		try (BufferedReader buf = new BufferedReader(new FileReader(
				"IPList.txt"));) {
			while ((line = buf.readLine()) != null && line.length() != 0)
				IPList.add(line);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Server srv = new Server("Dan", "Ghita", IPList, true);
		srv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		srv.startRunning();
	}
}
