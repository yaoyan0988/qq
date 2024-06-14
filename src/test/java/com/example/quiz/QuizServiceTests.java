package com.example.quiz;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.example.quiz.constants.OptionType;
import com.example.quiz.repository.QuizDao;
import com.example.quiz.service.ifs.QuizService;
import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.CreateOrUpdateReq;
import com.example.quiz.vo.Question;

@SpringBootTest
public class QuizServiceTests {

	@Autowired
	private QuizService quizService;
	
	@Autowired
	private QuizDao quizDao;
	
	//�H�U�ΨӴ��� QuizService ������k�O�_�����D
	@Test
	public void createTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"���Ƴ�ԣ","����;���;����;�Q�s��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"�U�ȯ��Yԣ","����;�J�|;�氮;�B�N�O", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"���\�Yԣ","�K�O�N;����;����;�N��", //
				OptionType.SINGLE_CHOICE.getType(),true));

		CreateOrUpdateReq req = new CreateOrUpdateReq("�п�@�\","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		
		BasicRes res=quizService.createOrUpdate(req);
		
		Assert.isTrue(res.getStatusCode()==200, "create test flase!!");
		//�R�����ո�� TODO
	}
	
	@Test
	public void createNameErrorTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"���Ƴ�ԣ","����;���;����;�Q�s��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"�U�ȯ��Yԣ","����;�J�|;�氮;�B�N�O", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"���\�Yԣ","�K�O�N;����;����;�N��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		CreateOrUpdateReq req = new CreateOrUpdateReq("","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param name error!!"), "create test flase!!");
	}
	
	@Test
	public void createStartDateErrorTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"���Ƴ�ԣ","����;���;����;�Q�s��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"�U�ȯ��Yԣ","����;�J�|;�氮;�B�N�O", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"���\�Yԣ","�K�O�N;����;����;�N��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		//���ѬO2024.5.30 �ҥH�}�l�������O���
		CreateOrUpdateReq req = new CreateOrUpdateReq("�п�@�\","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,5,30),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param start date error!!"), "create test flase!!");
	}
	
	@Test
	public void createTest1() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"���Ƴ�ԣ","����;���;����;�Q�s��", //
				OptionType.SINGLE_CHOICE.getType(),true));
		//���� name error
		CreateOrUpdateReq req = new CreateOrUpdateReq("","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param name error!!"), "create test flase!!");
		res=quizService.createOrUpdate(req);
		//���� start date error
		//���ѬO2024.5.30 �ҥH�}�l�������O���
		req = new CreateOrUpdateReq("�п�@�\","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,5,30),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param start date error!!"), "create test flase!!");
		//���� end date error ������������}�l�����
		req = new CreateOrUpdateReq("�п�@�\","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,6,30),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param end date error!!"), "create test flase!!");
		//�Ѿl���޿�����P�_������,�̫�~�O���\������
		req = new CreateOrUpdateReq("�п�@�\","�п�ܤ@�\�����q�Ы�", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getStatusCode()==200, "create test flase!!");
	}
}
