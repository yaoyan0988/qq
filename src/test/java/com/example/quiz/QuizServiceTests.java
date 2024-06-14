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
	
	//以下用來測試 QuizService 中的方法是否有問題
	@Test
	public void createTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"飲料喝啥","紅茶;綠茶;奶茶;烏龍茶", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"下午茶吃啥","雞排;蛋糕;餅乾;冰淇淋", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"晚餐吃啥","鐵板燒;拉麵;火鍋;燒肉", //
				OptionType.SINGLE_CHOICE.getType(),true));

		CreateOrUpdateReq req = new CreateOrUpdateReq("請選一餐","請選擇一餐讓公司請客", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		
		BasicRes res=quizService.createOrUpdate(req);
		
		Assert.isTrue(res.getStatusCode()==200, "create test flase!!");
		//刪除測試資料 TODO
	}
	
	@Test
	public void createNameErrorTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"飲料喝啥","紅茶;綠茶;奶茶;烏龍茶", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"下午茶吃啥","雞排;蛋糕;餅乾;冰淇淋", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"晚餐吃啥","鐵板燒;拉麵;火鍋;燒肉", //
				OptionType.SINGLE_CHOICE.getType(),true));
		CreateOrUpdateReq req = new CreateOrUpdateReq("","請選擇一餐讓公司請客", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param name error!!"), "create test flase!!");
	}
	
	@Test
	public void createStartDateErrorTest() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"飲料喝啥","紅茶;綠茶;奶茶;烏龍茶", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(2,"下午茶吃啥","雞排;蛋糕;餅乾;冰淇淋", //
				OptionType.SINGLE_CHOICE.getType(),true));
		questionList.add(new Question(3,"晚餐吃啥","鐵板燒;拉麵;火鍋;燒肉", //
				OptionType.SINGLE_CHOICE.getType(),true));
		//今天是2024.5.30 所以開始日期不能是當天
		CreateOrUpdateReq req = new CreateOrUpdateReq("請選一餐","請選擇一餐讓公司請客", //
				LocalDate.of(2024,5,30),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param start date error!!"), "create test flase!!");
	}
	
	@Test
	public void createTest1() {
		List<Question> questionList=new ArrayList<>();
		questionList.add(new Question(1,"飲料喝啥","紅茶;綠茶;奶茶;烏龍茶", //
				OptionType.SINGLE_CHOICE.getType(),true));
		//測試 name error
		CreateOrUpdateReq req = new CreateOrUpdateReq("","請選擇一餐讓公司請客", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		BasicRes res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param name error!!"), "create test flase!!");
		res=quizService.createOrUpdate(req);
		//測試 start date error
		//今天是2024.5.30 所以開始日期不能是當天
		req = new CreateOrUpdateReq("請選一餐","請選擇一餐讓公司請客", //
				LocalDate.of(2024,5,30),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param start date error!!"), "create test flase!!");
		//測試 end date error 結束日期不能比開始日期早
		req = new CreateOrUpdateReq("請選一餐","請選擇一餐讓公司請客", //
				LocalDate.of(2024,6,30),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getMessage().equalsIgnoreCase("Param end date error!!"), "create test flase!!");
		//剩餘的邏輯全部判斷完之後,最後才是成功的情境
		req = new CreateOrUpdateReq("請選一餐","請選擇一餐讓公司請客", //
				LocalDate.of(2024,6,1),LocalDate.of(2024, 6, 1),questionList,true);
		res=quizService.createOrUpdate(req);
		Assert.isTrue(res.getStatusCode()==200, "create test flase!!");
	}
}
