package com.example.quiz.vo;

public class BasicRes {

	private int statusCode;

	private String message;

	public BasicRes() {
		super();
	}

	public BasicRes(int statusCode, String message) {
		super();
		this.statusCode = statusCode;
		this.message = message;
	}

	// res�̪�get�Bset�򥻤W�����|�Ψ�]�����W�誺�غc��k�F
	// �o�̷|�ͦ�get�Bset��¬O���F�קK���L�C����
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
