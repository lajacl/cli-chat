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

	private Socket socket;
	private PrintWriter writer;
	private Server server;
	
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

				switch (message.getCommand()) {
					case "connect":
						log.info("{}: <{}> has connected", message.getTimestamp(), message.getUsername());
						// no dups
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
						List<ClientHandler> clientList = server.getClientList();
						String cast = mapper.writeValueAsString(message);
						for(ClientHandler client: clientList) {
							System.out.println("client: " + client);
							client.writer.write(cast);
							client.writer.flush();
						}
						break;
					case "direct":
						log.info("{} <{}> (whisper): {}", message.getTimestamp(), message.getUsername(), message.getContents());
						// TO DO
						break;
					case "users":
						log.info("{}: currently connected users:\n {}", message.getTimestamp(), server.getClientList());
						// TO DO
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
