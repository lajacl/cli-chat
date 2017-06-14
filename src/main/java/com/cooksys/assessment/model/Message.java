package com.cooksys.assessment.model;

import java.sql.Timestamp;

public class Message {

	private String username;
	private String command;
	private String contents;
	private String timestamp;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public String getTimestamp() {
		this.timestamp = new Timestamp(0).toString();
		return timestamp;
	}

	public void setTimestamp() {
		this.timestamp = new Timestamp(0).toString();
	}
	
}
