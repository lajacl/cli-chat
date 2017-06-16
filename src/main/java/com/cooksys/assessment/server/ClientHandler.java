package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	public Socket socket;
	public PrintWriter writer;
	private Server server;
	private List<ClientHandler> clientList;
	private String user = "";
	private String receiver;
	
	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				message.setTimestamp();
				
				// check if a direct message was sent and set the command
				if (message.getCommand().charAt(0) == '@') {
					receiver = message.getCommand().substring(1);
					message.setCommand("direct");
				}
				
				/*
				 * Handles message commands and returns a response to the client
				 */
				switch (message.getCommand()) {
					
					case "connect":
						log.info("{}: <{}> has connected", message.getTimestamp(), message.getUsername());
						this.user = message.getUsername();
						clientList = server.getClientList();
						message.setContents(message.getTimestamp() + ": <" + message.getUsername() + "> has connected");
						String alert = mapper.writeValueAsString(message);
						for(ClientHandler client: clientList) {
							client.writer.write(alert);
							client.writer.flush();
						}
						break;
					case "disconnect":
						log.info("{}: <{}> has disconnected", message.getTimestamp(), message.getUsername());
						clientList = server.getClientList();
						message.setContents(message.getTimestamp() + ": <" + message.getUsername() + "> has disconnected");
						alert = mapper.writeValueAsString(message);
						for(ClientHandler client: clientList) {
							client.writer.write(alert);
							client.writer.flush();
						}
						clientList.remove(this);
						this.socket.close();
						break;
					case "echo":
						log.info("{} <{}> (echo): {}", message.getTimestamp(), message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("{} <{}> (all): {}", message.getTimestamp(), message.getUsername(), message.getContents());
						clientList = server.getClientList();
						response = mapper.writeValueAsString(message);
						for(ClientHandler client: clientList) {
							client.writer.write(response);
							client.writer.flush();
						}
						break;
					// loops through 
					case "direct":
						log.info("{} <{}> (whisper): {}", message.getTimestamp(), message.getUsername(), message.getContents());
						response = mapper.writeValueAsString(message);
						clientList = server.getClientList();						
						for(ClientHandler client: clientList) {
							if (client.getUser().equals(receiver)) {
								client.writer.write(response);
								client.writer.flush();
								break;
							}
						}						
						break;
					// loop through clients and pull usernames
					case "users":
						clientList = server.getClientList();
						String userStr = "";
						for(ClientHandler client: clientList) {
							userStr += "<" + client.getUser() + ">\n";			
						}
							message.setContents(userStr);
							response = mapper.writeValueAsString(message);
							writer.write(response);
							writer.flush();
							log.info("{}: currently connected users:\n{}", message.getTimestamp(), userStr);
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	public String getUser() {
		return user;		
	}

}
