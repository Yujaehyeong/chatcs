package com.cafe24.network.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ChatClientApp {

	private static final String SERVER_IP = "192.168.1.20";
	private static final int SERVER_PORT = 7000;

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);

		// 체크하는 소켓을 만들었다가 중복된아이디만 체크하는게 옳은것인가 --> 서버부하 ?
		// 하나의 소켓만 생성해서 아이디체크하고 로그인된 후 부터 끊길때 까지 사용하는게 맞는것인가
		Socket isOverlapCheckSocket = new Socket();
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			isOverlapCheckSocket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			br = new BufferedReader(new InputStreamReader(
					isOverlapCheckSocket.getInputStream(), "utf-8"));
			pw = new PrintWriter(new OutputStreamWriter(
					isOverlapCheckSocket.getOutputStream(), "utf-8"), true);
			
			while (true) {

				System.out.println("대화명을 입력하세요.");
				System.out.print(">>> ");
				name = scanner.nextLine();
				
				pw.println("nameOverlapCheck」「" + name);

				String nameOverlapResult = br.readLine();
				
				if ("overlap".equals(nameOverlapResult)) {
					System.out.println("이미 존재하는 대화명입니다. 다시입력해주세요....");
					continue;
				}

				if (name.isEmpty() == false) {
					break;
				}

				System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
				
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isOverlapCheckSocket != null && !isOverlapCheckSocket.isClosed()) {
				try {
					isOverlapCheckSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
