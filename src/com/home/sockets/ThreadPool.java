package com.home.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
	public static Map<String, Socket> dataBase;
	private static ExecutorService executor;
	public static String stringBase = "";
	
	public static void main(String[] argc) throws IOException{
		dataBase = new HashMap<String, Socket>();
		executor = Executors.newCachedThreadPool();
		ServerSocket serverSocket = new ServerSocket(4444);
		
		while(true){
			System.out.println("Going to accept new connection");
			Socket socket = serverSocket.accept();
			System.out.println("Socket connected to: " + socket.getLocalAddress().toString());
			Runnable work = new ClientSide(socket);
			executor.execute(work);
		}
		
	}
	
}
