package com.example.quiz.service.ifs;

import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.DeleteReq;
import com.example.quiz.vo.FeedbackReq;
import com.example.quiz.vo.FeedbackRes;
import com.example.quiz.vo.FillinReq;
import com.example.quiz.vo.StatisticsRes;

public interface FillinService {

	public BasicRes fillin(FillinReq req);

	public FeedbackRes feeback(FeedbackReq req);
	
	public StatisticsRes statistics(FeedbackReq req);
	
	public BasicRes deleteResponse(DeleteReq req);
}
