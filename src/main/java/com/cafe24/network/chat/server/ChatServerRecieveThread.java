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

				// 6. 메세지검사
				messageInspect(pr, data);

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

	public void messageInspect(PrintWriter printWriter, String message) {

		String messageTokens[] = message.split("」「");
		String messageClassification = messageTokens[0];
		String sendedUserName = messageTokens[1];
		String sendData = "";
		
		switch (messageClassification) {
		case "nameOverlapCheck":
			nameOverlapCheck(sendedUserName, printWriter);
			break;
		case "login":
			addPrintWriter(printWriter);
			addUSer(sendedUserName, printWriter);
			sendData = sendedUserName + "님이 입장하셨습니다.";
			broadcasting(printWriter, sendData);
			break;
		case "message":
			String messageData = messageTokens[2];
			sendData = sendedUserName + " : " + messageData;
			broadcasting(printWriter, sendData);
			break;
		case "whisper":
			String whisperMessageData = messageTokens[2];
			String receiveWhisperUserName = messageTokens[3];
			sendData = sendedUserName + " : " + whisperMessageData;
			whisper(sendedUserName, receiveWhisperUserName, sendData);
			break;
		case "logout":
			removePrintWriter(printWriter);
			deleteUser(sendedUserName);
			sendData = sendedUserName + "님이 퇴장하셨습니다.";
			broadcasting(printWriter, sendData);
			break;

		default:
			break;
		}

	}
	
	public void nameOverlapCheck(String userName, PrintWriter printWriter) {
		synchronized (pwMap) {
			if(pwMap.containsKey(userName)) {
				printWriter.println("overlap");
			}else {
				printWriter.println("success");
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

	public void whisper(String sendedUserName, String receiveWhisperUserName, String message) {
		
		synchronized (pwMap) {
			if (!pwMap.containsKey(receiveWhisperUserName)) {
				pwMap.get(sendedUserName).println(receiveWhisperUserName+ "님이 없습니다.");
				return;
			}
			pwMap.get(receiveWhisperUserName).println("(귓속말)" + message);
		}
	}
	
	public void broadcasting(PrintWriter printWriter, String sendData) {
		synchronized (printWriterList) {
			for (PrintWriter pw : printWriterList) {
				if (pw != printWriter) {
					pw.println(sendData);
				}
			}
		}
	}

}
