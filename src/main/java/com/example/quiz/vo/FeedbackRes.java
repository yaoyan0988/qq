package com.example.quiz.vo;

import java.util.List;

import com.example.quiz.entity.Quiz;

public class FeedbackRes extends BasicRes {

	private List<Quiz> questions;

	private List<Feedback> feedbackList;

	public FeedbackRes() {
		super();
	}

	public FeedbackRes(int statusCode, String message) {
		super(statusCode, message);
	}

	public FeedbackRes(int statusCode, String message, List<Quiz> questions, List<Feedback> feedbackList) {
		super(statusCode, message);
		this.questions = questions;
		this.feedbackList = feedbackList;
	}

	public List<Quiz> getQuestions() {
		return questions;
	}

	public List<Feedback> getFeedbackList() {
		return feedbackList;
	}

}
