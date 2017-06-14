package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private Set<String> users = new HashSet<String>();

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "connect":
						log.info("{}: <{}> has connected", message.getTimestamp(), message.getUsername());
						// no dups
						users.add(message.getUsername());
						break;
					case "disconnect":
						log.info("{}: <{}> has disconnected", message.getTimestamp(), message.getUsername());
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
						// TO DO
						break;
					case "direct":
						log.info("{} <{}> (whisper): {}", message.getTimestamp(), message.getUsername(), message.getContents());
						// TO DO
						break;
					case "users":
						log.info("{}: currently connected users:\n {}", message.getTimestamp(), getUsers());
						// TO DO
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	private String getUsers() {
		// TODO Auto-generated method stub
		return "<${username}>\n";
	}

}
