package com.example.quiz.vo;

import java.time.LocalDateTime;

public class Feedback {

	private int id;

	private String userName;

	private LocalDateTime fillinDateTime;

	private FeedbackDetail feedbackDetail;

	public Feedback() {
		super();
	}

	public Feedback(int id, String userName, LocalDateTime fillinDateTime, FeedbackDetail feedbackDetail) {
		super();
		this.id = id;
		this.userName = userName;
		this.fillinDateTime = fillinDateTime;
		this.feedbackDetail = feedbackDetail;
	}

	public int getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public LocalDateTime getFillinDateTime() {
		return fillinDateTime;
	}

	public FeedbackDetail getFeedbackDetail() {
		return feedbackDetail;
	}

}
