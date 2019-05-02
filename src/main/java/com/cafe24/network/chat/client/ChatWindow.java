package com.cafe24.network.chat.client;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private String name;
	private Socket socket;
	private BufferedReader br;
	private PrintWriter pr;
	private Thread receiveMessageThread;

	public ChatWindow(String name, Socket socket) {
		this.name = name;
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.socket = socket;

	}

	private void finish() {
		// 로그아웃 메세지 전송 - 메세지전송 연결이 되어있는상태에서 메세지 보내고 종료
		logout();
		// socket 정리

		if (socket != null && !socket.isClosed()) {
			try {

				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	private void createStream() {

		// IOStream 생성(받아오기)
		try {
			br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "utf-8"));
			// true를 넣어줌으로써 Auto flush 해줌
			pr = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), "utf-8"), true);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				sendMessage();
			}
		});

		// TextField
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) { // 엔터 눌렀을때 감지
					sendMessage();
				}
			}

		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();

		// Stream생성
		createStream();

		// 로그인 메세지전송- 연결이 되고나서 메세지를 전송해야함
		login();

		// thread 생성
		class ReceiveMessageThread extends Thread {

			private BufferedReader br;
			private ChatWindow chatWindow;

			public ReceiveMessageThread(BufferedReader br, ChatWindow chatWindow) {
				this.br = br;
				this.chatWindow = chatWindow;
			}

			@Override
			public void run() {
				String data = null;
				try {
					while (true) {

						data = br.readLine();

						if (data == null) {
							break;
						}
						chatWindow.updateTextArea(data);
					}

				} catch (SocketException e) {
					System.out.println("sudden close");
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

		receiveMessageThread = new ReceiveMessageThread(br, this);
		receiveMessageThread.start();

	}

	private void login() {
		pr.println("login」「" + name);
	}

	private void logout() {
		pr.println("logout」「" + name);
	}

	private void updateTextArea(String receivedMessage) {

		textArea.append(receivedMessage + "\n");

	}

	private void sendMessage() { // 여기서 printWriter 해주면됨

		String sendMessage = textField.getText();
		if ("".equals(sendMessage)) {
			return;
		}
		if(sendMessage.startsWith("/w ")) {
			whisper(sendMessage);
			return;
		}
		if ("quit".equals(sendMessage)) {
			finish();
			return;
		}
		
		pr.println("message」「" + name + "」「" + sendMessage); // 여기서 보내면서버가 받고 서버가 모든 채팅인원들에게 브로드캐스팅해주면됨.
		textField.setText("");
		textField.requestFocus();

		updateTextArea(name + ":" + sendMessage);
	}
	
	
	// >>>>>> /w 아이디 메세지 -> 이런식으로 입력하면됨
	private void whisper(String sendMessage) {
		
		String whisperMessageTokens [] = sendMessage.split(" ");
		String receiveUser = whisperMessageTokens[1];
		// ex) /w 홍길동 안녕하세요 라면 "/w" = 2, " " = 1, "홍길동" = 3, " "= 1 을 더한값 부터 시작하여 마지막까지 메세지이다.
		String completeSendMessage = sendMessage.substring(2+1+receiveUser.length()+1);
		System.out.println("completeSendMessage: "+ completeSendMessage);
		pr.println("whisper」「" + name+"」「"+completeSendMessage+"」「"+receiveUser);
		textField.setText("");
		textField.requestFocus();
		updateTextArea(name + ":" + completeSendMessage+"("+receiveUser+"님에게)");
	}
}
