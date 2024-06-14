package com.example.quiz.vo;

import java.util.List;

import com.example.quiz.entity.Quiz;

public class SeachRes extends BasicRes {

	private List<Quiz> questions;

	public SeachRes() {
		super();
	}

	public SeachRes(int statusCode, String message, List<Quiz> questions) {
		super(statusCode, message);
		this.questions = questions;
	}

	public List<Quiz> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Quiz> questions) {
		this.questions = questions;
	}

}
