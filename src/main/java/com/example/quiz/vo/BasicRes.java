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

	// res裡的get、set基本上都不會用到因為有上方的建構方法了
	// 這裡會生成get、set單純是為了避免紅蚯蚓產生
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
