package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

	private static final int PORT = 7000;
	private static List<PrintWriter> printWriterList;

	public static void main(String[] args) {

		printWriterList = new ArrayList<>();
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
				Thread thread = new ChatServerRecieveThread(socket);
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

	public static void broadcasting(PrintWriter printWriter, String message) {

		String messageTokens[] = message.split("」「");
		String messageClassification = messageTokens[0];
		String sendedUserName = messageTokens[1];
		

		String sendData = "";

		switch (messageClassification) {
		case "login":
			ChatServer.addPrintWriter(printWriter);
			sendData = sendedUserName+ "님이 입장하셨습니다.";
			
			break;
		case "message":
			String messageData = messageTokens[2];
			sendData = sendedUserName+ ":" + messageData;
			break;
		case "logout":
			
			removePrintWriter(printWriter);
			sendData = sendedUserName+ "님이 퇴장하셨습니다.";
			break;
		default:
			break;
		}
		
		for (PrintWriter pw : printWriterList) {
			System.out.println("printWriterList에서 메세지 전송");
			if (pw != printWriter) {
				System.out.println("printWriterList에서 메세지 해당 pw 제외하고");
				pw.println(sendData);
			}
		}
	}

	public static void addPrintWriter(PrintWriter pr) {
		printWriterList.add(pr);
	}
	public static void removePrintWriter(PrintWriter pr) {
		printWriterList.remove(pr);
	}
	
	public static void log(String log) {
		System.out.println("[server#" + Thread.currentThread().getId() + log + "]");
	}
}
