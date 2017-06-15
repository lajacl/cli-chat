package com.cooksys.assessment.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		this.timestamp = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a").format(new Date());
		return timestamp;
	}

	public void setTimestamp() {
		this.timestamp = new Timestamp(0).toString();
	}
	
}
