package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServerRecieveThread extends Thread {

	private Socket socket;
	private List<PrintWriter> printWriterList;
	Map<String, PrintWriter> pwMap;

	public ChatServerRecieveThread(Socket socket, List<PrintWriter> printWriterList, Map<String, PrintWriter> pwMap) {
		this.socket = socket;
		this.printWriterList = printWriterList;
		this.pwMap = pwMap;
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

	public void addUSer(String userName, PrintWriter printWriter) {
		synchronized (pwMap) {
			pwMap.put(userName, printWriter);
		}
	}

	public void deleteUser(String userName) {
		synchronized (pwMap) {
			pwMap.remove(userName);
		}
	}

	public void addPrintWriter(PrintWriter printWriter) {
		synchronized (printWriterList) {
			printWriterList.add(printWriter);
		}
	}

	public void removePrintWriter(PrintWriter printWriter) {
		synchronized (printWriterList) {
			printWriterList.remove(printWriter);
		}
	}

	private void sendMessage(PrintWriter printWriter, String message) {
		broadcasting(printWriter, message);
	}

	private void whisper(String receiveWhisperUserName, String message) {
		synchronized (pwMap) {
			pwMap.get(receiveWhisperUserName).println("(귓속말)"+message);
		}
	}
	
	public void broadcasting(PrintWriter printWriter, String message) {

		String messageTokens[] = message.split("」「");
		String messageClassification = messageTokens[0];
		String sendedUserName = messageTokens[1];

		String sendData = "";

		switch (messageClassification) {
		case "login":
			addPrintWriter(printWriter);
			addUSer(sendedUserName, printWriter);
			sendData = sendedUserName + "님이 입장하셨습니다.";

			break;
		case "message":
			String messageData = messageTokens[2];
			sendData = sendedUserName + " : " + messageData;
			break;
		case "whisper":
			String whisperMessageData = messageTokens[2];
			String receiveWhisperUserName = messageTokens[3];
			System.out.println("receiveWhisperUserName: "+ receiveWhisperUserName);
			System.out.println("sendedUserName: "+ sendedUserName);
			sendData = sendedUserName + " : " + whisperMessageData;
			whisper(receiveWhisperUserName, sendData);
			return;
		case "logout":
			removePrintWriter(printWriter);
			deleteUser(sendedUserName);
			sendData = sendedUserName + "님이 퇴장하셨습니다.";
			break;
			
		default:
			break;
		}
		
		synchronized (printWriterList) {
			for (PrintWriter pw : printWriterList) {
				if (pw != printWriter) {
					pw.println(sendData);
				}
			}
		}
	}

	
}
