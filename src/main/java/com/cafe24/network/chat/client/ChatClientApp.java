package com.cafe24.network.chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {

	private static final String SERVER_IP = "192.168.1.20";
	private static final int SERVER_PORT = 7000;

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);

		while (true) {

			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();

			if (name.isEmpty() == false) {
				break;
			}
			
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		// 2. 소켓 생성
		Socket socket = new Socket();
		
		try {
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		scanner.close();
		
		new ChatWindow(name, socket).show();
	}

}
