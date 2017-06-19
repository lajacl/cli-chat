package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;
	private Server server;
	private String user;
	private ObjectMapper mapper;
	private PrintWriter writer;
	private Message message;
	private Set<ClientHandler> clientList; // list of clients connected to server
	private String receiver; // @user cmd other user(name) to message
	private String response; // msg to return to client

	
	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {

			mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				message = mapper.readValue(raw, Message.class);
				//server.addMessge(message);
				
				message.setTimestamp();
				
				// check if a direct message was sent and set the command
				if (message.getCommand().charAt(0) == '@') {
					receiver = message.getCommand().substring(1);
					message.setCommand("direct");
				}
				
				/*
				 * handles message commands and returns a response to the client
				 */
				switch (message.getCommand()) {
					
					case "connect":
					cmdConnect();
						break;
					case "disconnect":
					cmdDisconnect();
						break;
					case "echo":
					cmdEcho();
						break;
					case "broadcast":
					cmdBroadcast();
						break;
					case "direct":
					cmdDirect();						
						break;
					case "users":
					cmdUsers();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	/**
	 * Command: 'connect'
	 * gets the username from the message to store in client instance
	 * sends an alert to all users that the new user is online
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 */
	private void cmdConnect() throws JsonProcessingException {
		this.user = message.getUsername();
		clientList = server.getClientList();
		message.setContents(message.getTimestamp() + ": <" + message.getUsername() + "> has connected");
		response = mapper.writeValueAsString(message);
		for(ClientHandler client: clientList) {
			client.sendResponse(response);
		log.info("{}: <{}> has connected", message.getTimestamp(), message.getUsername());
		}
		
	}
	
	/**
	 * Command: 'disconnect'
	 * 
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private void cmdDisconnect() throws JsonProcessingException, IOException {
		clientList = server.getClientList();
		message.setContents(message.getTimestamp() + ": <" + message.getUsername() + "> has disconnected");
		response = mapper.writeValueAsString(message);
		for(ClientHandler client: clientList) {
			client.sendResponse(response);
		}
		clientList.remove(this);
		this.socket.close();
		log.info("{}: <{}> has disconnected", message.getTimestamp(), message.getUsername());
	}
	
	/**
	 * Command: 'echo'
	 * returns the message received from the client back to it
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 */
	private void cmdEcho() throws JsonProcessingException {
		response = mapper.writeValueAsString(message);
		sendResponse(response);
		log.info("{} <{}> (echo): {}", message.getTimestamp(), message.getUsername(), message.getContents());
	}
	
	/**
	 * Command: 'broadcast'
	 * sends the message to all the current clients
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 */
	private void cmdBroadcast() throws JsonProcessingException {
		clientList = server.getClientList();
		response = mapper.writeValueAsString(message);
		for(ClientHandler client: clientList) {
			client.sendResponse(response);
		}
		log.info("{} <{}> (all): {}", message.getTimestamp(), message.getUsername(), message.getContents());
	}
	
	/**
	 * Command: 'direct' [@user]
	 * sends the message to a specified user
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 */
	private void cmdDirect() throws JsonProcessingException {
		response = mapper.writeValueAsString(message);
		clientList = server.getClientList();						
		for(ClientHandler client: clientList) {
			if (client.getUser().equals(receiver)) {
				client.sendResponse(response);
				break;
			}
		}
		log.info("{} <{}> (whisper): {}", message.getTimestamp(), message.getUsername(), message.getContents());
	}
	
	/**
	 * Command: 'users'
	 * pulls all usernames from the client lists and presents them
	 * logs ^ events to console
	 * @throws JsonProcessingException
	 */
	private void cmdUsers() throws JsonProcessingException {
		clientList = server.getClientList();
		String userStr = "";
		for(ClientHandler client: clientList) {
			userStr += "<" + client.getUser() + ">\n";			
		}
			message.setContents(userStr);
			response = mapper.writeValueAsString(message);
			sendResponse(response);
			log.info("{}: currently connected users:\n{}", message.getTimestamp(), userStr);
	}	

	private void sendResponse(String response) {
		writer.write(response);
		writer.flush();		
	}

	public String getUser() {
		return user;		
	}

}
