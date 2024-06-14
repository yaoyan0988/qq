package com.example.quiz.service.ifs;

import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.CreateOrUpdateReq;
import com.example.quiz.vo.DeleteReq;
import com.example.quiz.vo.SeachReq;
import com.example.quiz.vo.SeachRes;

public interface QuizService {

	public BasicRes createOrUpdate(CreateOrUpdateReq req);
	
	public SeachRes search(SeachReq req);
	
	public BasicRes delete(DeleteReq req);
	
	
}
  