package com.example.quiz.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.quiz.constants.OptionType;
import com.example.quiz.constants.ResMessage;
import com.example.quiz.entity.Quiz;
import com.example.quiz.repository.QuizDao;
import com.example.quiz.service.ifs.QuizService;
import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.CreateOrUpdateReq;
import com.example.quiz.vo.DeleteReq;
import com.example.quiz.vo.Question;
import com.example.quiz.vo.SeachReq;
import com.example.quiz.vo.SeachRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuizServiceImpl implements QuizService {

	@Autowired
	private QuizDao quizDao;
	
	@Override
	public BasicRes createOrUpdate(CreateOrUpdateReq req) {
		//檢查參數
		 BasicRes checkResult = checkParams(req);
		 //checkResult==null時表示參數檢查都正確
		 if(checkResult != null) {
			 return checkResult;
		 }
		 //因為 Quiz 中的 questions的資料格式是String ,所以要將 req 的 List<Question> 轉成String
		 //透過ObjectMapper 可以把物件(類別)轉成 Json格式的字串
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			String questionStr = mapper.writeValueAsString(req.getQuestionList());
			
			//若 req 中的 id>0 表示更新已存在的資料 : 若 id =0 則表示新增
			if(req.getId()>0) {
				//以下兩種方式擇一
				//方法1 透過findById 若有資料就會回傳一整筆的資料(可能資料量會較大)
				//方法2 因為是透過 existsById 來判斷資料是否存在，所以回傳的資料永遠都會是一個bit(0或1)
				//方法 1
//				Optional<Quiz> op=quizDao.findById(req.getId());
//				//判斷是否有資料
//				if(op.isEmpty()) { // op.isEmpty():表示沒資料
//					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(),
//							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
//				}
//				Quiz quiz = op.get();
//				//設定新值(值從 req 來)
//				//將 req 中的新值設定到舊的 quiz中，不設定id因為id一樣
//				quiz.setName(req.getName());
//				quiz.setDescription(req.getDescription());
//				quiz.setStartDate(req.getStartDate());
//				quiz.setEndDate(req.getEndDate());
//				quiz.setQuestions(questionStr);
//				quiz.setPublished(req.isPublished());
				//方法 2.透過 existsById:回傳一個bit的值
				//這邊要判斷從 req 帶進來的 id 是否真的存在DB中
				//因為若ID 不存在，又不檢查 後續程式碼再呼叫 JPA 的 save 方法時會變成新增
				boolean boo = quizDao.existsById(req.getId());
				if(!boo) { //!boo表示資料不存在
					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(),
							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
				}
				
			}
			//===========================
			// 上述一整段 if 程式碼可以縮減成以下這段
			//   if(req.getId() > 0 && !quizDao.existsById(req.getId())) {   
			//     return new BasicRes(ResMessage.UPDATE_ID_NOT_F
			//===========================
			
//			Quiz quiz = new Quiz(req.getName(),req.getDescription(),req.getStartDate(), //
//					req.getEndDate(),questionStr,req.isPublished());
//			quizDao.save(quiz);  因為quiz只使用一次可以使用匿名類別撰寫
			//new Quiz()中帶入 req.getId()是PK，在呼叫save時會先去檢查PK是否有存在於DB中
			//若存在 -> 更新，不存在 -> 新增
			//req 中沒有該欄位時預設是0(因為id的資料型態是int)
			quizDao.save(new Quiz(req.getId(),req.getName(),req.getDescription(),req.getStartDate(), //
					req.getEndDate(),questionStr,req.isPublished()));
			
		} catch (JsonProcessingException e) {
			return new BasicRes(ResMessage.JSON_PROCESSING_EXCEPTION.getCode(),
					ResMessage.JSON_PROCESSING_EXCEPTION.getMessage());
		} 
		 return new BasicRes(ResMessage.SUCCESS.getCode(),
					ResMessage.SUCCESS.getMessage());
	}

	private BasicRes checkParams(CreateOrUpdateReq req) {
		//檢查問卷參數
		// 檢查字串用 StringUtils.hasText，確認是否為null、空字串、空白字串
		// 若符合3種其1則回傳false，要回傳ture才會執行內容，所以會在前方加!反轉結果為ture
		// !StringUtils.hasText(req.getName()) 等同於 StringUtils.hasText(req.getName())==false
		// 有驚嘆號 沒有驚嘆號
		if(!StringUtils.hasText(req.getName())){
			return new BasicRes(ResMessage.PARAM_QUIZ_NAME_ERROR.getCode(),
					ResMessage.PARAM_QUIZ_NAME_ERROR.getMessage());
		}
		if(!StringUtils.hasText(req.getDescription())){
			return new BasicRes(ResMessage.PARAM_DESCRIPTION_ERROR.getCode(),
					ResMessage.PARAM_DESCRIPTION_ERROR.getMessage());
		}
		//開始時間不能在今天之前
		//LocalDate.now():取得系統當前時間
		//req.getStartDate().isBefore(LocalDate.now())表示req中的開始時間不能在今天之前
		//req.getStartDate().isEqual(LocalDate.now())表示req中的開始時間不能是今天
		//因為開始時間不能在今天(含)之前，所以上兩個比較若是任一個結果為 true，則表示開始時間要比當前(含)時間早
		// !req.getStartDate().isAfter(LocalDate.now()) <=也可以用isAfter的反向去執行
		if(req.getStartDate()==null || req.getStartDate().isBefore(LocalDate.now()) //
				||req.getStartDate().isEqual(LocalDate.now())){
			return new BasicRes(ResMessage.PARAM_START_DATE_ERROR.getCode(),
					ResMessage.PARAM_START_DATE_ERROR.getMessage());
		}
		//1.結束時間不能小於等於當前時間 2.也不能小於開始時間
		//因為開始時間已經過濾了小於等於當前時間，所以結束時間只要判斷跟開始時間之間的關係即可
		if(req.getEndDate()==null || req.getEndDate().isBefore(req.getStartDate())){
			return new BasicRes(ResMessage.PARAM_END_DATE_ERROR.getCode(),
					ResMessage.PARAM_END_DATE_ERROR.getMessage());
		}
		//檢查問題參數
		if(CollectionUtils.isEmpty(req.getQuestionList())) {
			return new BasicRes(ResMessage.PARAM_QUESTION_LIST_NOT_FOUND.getCode(),
					ResMessage.PARAM_QUESTION_LIST_NOT_FOUND.getMessage());
		}
		//一張問卷會有很多筆問題，每個問題參數都要逐一檢查
		for(Question item:req.getQuestionList()) {
			if(item.getId()<=0) {
				return new BasicRes(ResMessage.PARAM_QUESTION_ID_ERROR.getCode(),
						ResMessage.PARAM_QUESTION_ID_ERROR.getMessage());
			}
			if(!StringUtils.hasText(item.getTitle())){
				return new BasicRes(ResMessage.PARAM_TITLE_ERROR.getCode(),
						ResMessage.PARAM_TITLE_ERROR.getMessage());
			}
			
			if(!StringUtils.hasText(item.getType())) {
				return new BasicRes(ResMessage.PARAM_TYPE_ERROR.getCode(),
						ResMessage.PARAM_TYPE_ERROR.getMessage());
			}
			//當option_type 是單選或多選時，options 就不能是空字串
			//但option_type 是文字時，options 允許是空字串
			//以下條件檢查:當option_type 是單選或多選時，且options是空字串返回重複
			if(item.getType().equals(OptionType.SINGLE_CHOICE.getType()) //
					||item.getType().equals(OptionType.MULTI_CHOICE.getType())) {
				if(!StringUtils.hasText(item.getOptions())){
					return new BasicRes(ResMessage.PARAM_OPTIONS_ERROR.getCode(),
							ResMessage.PARAM_OPTIONS_ERROR.getMessage());
				}
			}
		}
		return null;
//		return new BasicRes(ResMessage.SUCCESS.getCode(),
//				ResMessage.SUCCESS.getMessage());
		
	}

	@Override
	public SeachRes search(SeachReq req) {
		String name =req.getName();
		LocalDate start = req.getStartDate();
		LocalDate end = req.getEndDate();
		//假設 name 是 null 或是全空白字串，可以視為沒有輸入條件值
		//需要把name變成空字串
		if(!StringUtils.hasText(name)) {
			name="";
		}
		//下方為開始時間沒設置時的預設時間
		if(start==null) {
			start=LocalDate.of(1970,1,1);
		}
		//下方為結束時間沒設置時的預設時間
		if(end==null) {
			end=LocalDate.of(2999,12,31);
		}
		List<Quiz> res = quizDao.findByNameContainingAndStartDateGreaterThanEqualAndEndDateLessThanEqual(name,start,end);
		return new SeachRes(ResMessage.SUCCESS.getCode(),
				ResMessage.SUCCESS.getMessage(), //
				res);
	}

	@Override
	public BasicRes delete(DeleteReq req) {
		//檢查參數
		//如果沒有勾選任何項目則不執行刪除，直接回傳成功
		//如果有勾選值，則進入刪除步驟
		if(!CollectionUtils.isEmpty(req.getIdList())) {
			//刪除問卷
			try {
				quizDao.deleteAllById(req.getIdList());
			} catch (Exception e) {
				//當deleteAllById方法中，id的值不存在時，JPA會報錯
				//因為在刪除之前 JPA 會先搜尋帶入的 id 值，若沒結果就會報錯
				//但實際上也沒刪除任何東西，所以就不需要對這個 Exception 做處理
			}
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(),
				ResMessage.SUCCESS.getMessage());
	}
}
