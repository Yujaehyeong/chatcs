package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ChatServerRecieveThread extends Thread {

	private Socket socket;

	public ChatServerRecieveThread(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {

		InetSocketAddress inetRemoteSocketAddress =
				// Down Casting
				(InetSocketAddress) socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remotePort = inetRemoteSocketAddress.getPort();

		try {// Exception 처리 따로해줘야함
				// 4. IOStream 생성(받아오기)
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			// true를 넣어줌으로써 Auto flush 해줌
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			while (true) {
				// 5. 데이터 읽기
				String data = br.readLine(); // blocking
				if (data == null) {
					break;
				}

				// 6. 데이터 쓰기
				sendMessage(pr, data);

			}
		} catch (SocketException e) {
			System.out.println("[server] sudden closed by client");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void sendMessage(PrintWriter pw, String message) {
		ChatServer.broadcasting(pw, message);
	}
}
