package com.home.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientSide implements Runnable {

	private Socket socket;
	private String socketUsername;

	public ClientSide(Socket socket) {
		this.socket = socket;
		socketUsername = null;
	}

	@Override
	public void run() {
		try {
			System.out.println("New connection is accepted");
			InputStream input = socket.getInputStream();
			OutputStream output = socket.getOutputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			PrintWriter writer = new PrintWriter(output, true);
			String line = null;

			while ((line = reader.readLine()) != null) {
				System.out.println("input line: " + line);
				String[] splittedInput = inputSplitter(line);

				if (splittedInput[0].equals("LOGIN")) {
					System.out.println("LOGGED IN");
					int returnCheckLoginValue = checkLogin(splittedInput[1], socket);
					if (returnCheckLoginValue == 1) {
						writer.println("ERROR_NOT_UNIQIE");
					} else if (returnCheckLoginValue == 2) {
						writer.println("ERROR_ALREADY_REGISTERED");
					} else {
						writer.println("OK");
						socketUsername = splittedInput[1];
					}
				} else if (splittedInput[0].equals("BRDCAST")) {
					if (!ThreadPool.dataBase.containsValue(socket)) {
						writer.println("ERROR_NOT_REGISTERED");
					} else {
						sendBroadcast(splittedInput[1]);
						ThreadPool.stringBase += socketUsername + ": " + splittedInput[1] + "\n";  
						writer.println("OK BRDCAST");
					}
				} else if(splittedInput[0].equals("LIST")){
					listOfUsers();
				} else if(splittedInput[0].equals("MSG")){
					sendMessage(splittedInput[1], splittedInput[2]);
				} else if(splittedInput[0].equals("GET")){
					writer.println(ThreadPool.stringBase);
				}
			}
			System.out.println("client finishes");
			writer.close();
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void sendMessage(String user, String message) throws IOException{
		OutputStream output = ThreadPool.dataBase.get(user).getOutputStream();
		PrintWriter writer = new PrintWriter(output, true);
		
		writer.println("MSG " + socketUsername + " " + message);
	}
	
	private void listOfUsers() throws IOException{
		OutputStream output = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(output, true);
		String users = "";
		for(Map.Entry<String, Socket> entry: ThreadPool.dataBase.entrySet()){
			users += entry.getKey() + ", ";
		}
		writer.println("LIST " + users);
	}

	private void sendBroadcast(String text) throws IOException {
		for (Map.Entry<String, Socket> entry : ThreadPool.dataBase.entrySet()) {
			if (!entry.getValue().equals(this.socket) && !entry.getValue().isClosed()) {
				OutputStream output = entry.getValue().getOutputStream();
				PrintWriter writer = new PrintWriter(output, true);
				writer.println("BRDCAST " + socketUsername + " " + text);
				writer.close();
			}
		}
	}

	private String[] inputSplitter(String input) {
		return input.trim().split(" ");
	}

	private int checkLogin(String username, Socket userSocker) {
		if (ThreadPool.dataBase.containsKey(username)) {
			return 1;
		} else if (ThreadPool.dataBase.containsValue(socket)) {
			return 2;
		} else {
			ThreadPool.dataBase.put(username, socket);
			return 3;
		}
	}

}
