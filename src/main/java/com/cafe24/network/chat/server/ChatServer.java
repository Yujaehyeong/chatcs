package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {

	private static final int PORT = 7000;

	public static void main(String[] args) {

		List<PrintWriter> printWriterList = new ArrayList<>();
		Map<String, PrintWriter> pwMap = new HashMap<>();
		ServerSocket serverSocket = null;
		try {
			// 1. 서버소켓 생성
			serverSocket = new ServerSocket();
			
			// 2. binding
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			log("server start... [port: " + PORT + "]");

			while (true) {
				// 3. accept
				Socket socket = serverSocket.accept(); // blocking - connect할 동안 대기
				Thread thread = new ChatServerRecieveThread(socket, printWriterList, pwMap);
				thread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
					System.out.println("[server] Server Closed");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void log(String log) {
		System.out.println("[server#" + Thread.currentThread().getId() + log + "]");
	}
}
