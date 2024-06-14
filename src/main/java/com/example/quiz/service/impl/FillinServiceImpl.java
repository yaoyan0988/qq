package com.example.quiz.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.example.quiz.constants.OptionType;
import com.example.quiz.constants.ResMessage;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.Response;
import com.example.quiz.repository.QuizDao;
import com.example.quiz.repository.ResponseDao;
import com.example.quiz.service.ifs.FillinService;
import com.example.quiz.vo.BasicRes;
import com.example.quiz.vo.DeleteReq;
import com.example.quiz.vo.Feedback;
import com.example.quiz.vo.FeedbackDetail;
import com.example.quiz.vo.FeedbackReq;
import com.example.quiz.vo.FeedbackRes;
import com.example.quiz.vo.Fillin;
import com.example.quiz.vo.FillinReq;
import com.example.quiz.vo.Question;
import com.example.quiz.vo.StatisticsRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FillinServiceImpl implements FillinService {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private QuizDao quizDao;

	@Autowired
	private ResponseDao responseDao;

	@Override
	public BasicRes fillin(FillinReq req) {
		// 參數檢查
		BasicRes checkResult = checkParams(req);
		if (checkResult != null) {
			return checkResult;
		}
		// 檢查同一個電話號碼是否有重複填寫同一張問卷
		if (responseDao.existsByQuizIdAndPhone(req.getQuizId(), req.getPhone())) {
			return new BasicRes(ResMessage.DUPLICATED_FILLIN.getCode(), //
					ResMessage.DUPLICATED_FILLIN.getMessage());
		}
		// 檢查 quiz_id 是否存在於DB中
		// 因為後續會比對 req 中的答案與題目的選項是否符合，所以要用 findById
		Optional<Quiz> op = quizDao.findById(req.getQuizId());
		if (op.isEmpty()) {
			return new BasicRes(ResMessage.QUIZ_NOT_FOUND.getCode(), ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		Quiz quiz = op.get();
		// 從 quiz 中取出 questions 字串
		String questionsStr = quiz.getQuestions();
		// 使用 ObjectMapper 將 questionsStr 轉成 List<Question>
		// fillinStr 要給空字串，不然預設會是 null
		// 若 fillinStr = null，後續執行 fillinStr =
		// mapper.writeValueAsString(req.getqIdAnswerMap());
		// 把執行得到的結果塞回給 fillinStr 時，會報錯
		String fillinStr = "";
		try {
			List<Fillin> newFillinList = new ArrayList<>();
			// 建立已新增的 question_id list
			List<Integer> qIds = new ArrayList<>();
			List<Question> quList = mapper.readValue(questionsStr, new TypeReference<>() {
			});

			// 比對每一個 Question 與 fillin 中的答案
			for (Question item : quList) {

				List<Fillin> fillinList = req.getFillinList();
				for (Fillin fillin : fillinList) {
					// id 不一致，跳過
					if (item.getId() != fillin.getqId()) {
						continue;
					}
					// 如果 qIds 已經有符合題目編號的答案，則跳過下方驗證
					// 避免在答案列表有相同題號的答案出現
					if (qIds.contains(fillin.getqId())) {
						continue;
					}

					// 將已新增問題之題號加入
					qIds.add(fillin.getqId());
					// 新增相同題號的fillin
					// 不直接把fillin加到list的原因是
					// 上面的程式碼只有對 question_id 和 answer 檢查，所以其餘的屬性內容可能是不合法的
					// 直接使用 Question item 的值是因為這些值都是從DB來的，當初已有檢查過
					newFillinList.add(new Fillin(item.getId(), item.getTitle(), item.getOptions(), fillin.getAnswer(),
							item.getType(), item.isNecessary()));
					// 用下方的私有方法 checkOptionAnswer 檢查選項與答案
					checkResult = checkOptionAnswer(item, fillin);
					// 當 checkOptionAnswer 回傳null表示資料檢查都正確
					if (checkResult != null) {
						return checkResult;
					}

				}
				// 正常情況是:問題是必填，然後又有回答，每跑完一題 qIds 就會包含該必填的問題id
				// 因此當答案未必填，但是 qIds 中無此編號就會報錯
				if (item.isNecessary() && !qIds.contains(item.getId())) {
					return new BasicRes(ResMessage.ANSWER_IS_REQUIRED.getCode(),
							ResMessage.ANSWER_IS_REQUIRED.getMessage());
				}
			}
			fillinStr = mapper.writeValueAsString(newFillinList.subList(0, quList.size()));
		} catch (JsonProcessingException e) {
			return new BasicRes(ResMessage.JSON_PROCESSING_EXCEPTION.getCode(),
					ResMessage.JSON_PROCESSING_EXCEPTION.getMessage());
		}
		responseDao.save(new Response(req.getQuizId(), req.getName(), req.getPhone(), req.getEmail(), //
				req.getAge(), fillinStr));
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	private BasicRes checkParams(FillinReq req) {
		if (req.getQuizId() <= 0) {
			return new BasicRes(ResMessage.PARAM_QUIZ_ID_ERROR.getCode(), ResMessage.PARAM_QUIZ_ID_ERROR.getMessage());
		}
		if (!StringUtils.hasText(req.getName())) {
			return new BasicRes(ResMessage.PARAM_NAME_IS_REQUIRED.getCode(),
					ResMessage.PARAM_NAME_IS_REQUIRED.getMessage());
		}
		if (!StringUtils.hasText(req.getPhone())) {
			return new BasicRes(ResMessage.PARAM_PHONE_IS_REQUIRED.getCode(),
					ResMessage.PARAM_PHONE_IS_REQUIRED.getMessage());
		}
		if (!StringUtils.hasText(req.getEmail())) {
			return new BasicRes(ResMessage.PARAM_EMAIL_IS_REQUIRED.getCode(),
					ResMessage.PARAM_EMAIL_IS_REQUIRED.getMessage());
		}
		if (req.getAge() < 12 || req.getAge() > 99) {
			return new BasicRes(ResMessage.PARAM_AGE_IS_NOT_QUALIFIED.getCode(),
					ResMessage.PARAM_AGE_IS_NOT_QUALIFIED.getMessage());
		}

		return null;
	}

	private BasicRes checkOptionAnswer(Question item, Fillin fillin) {
		// 此處檢查
		// 1. 檢查必填也要有答案
		// fillin 中的答案沒有值，返回錯誤
		if (item.isNecessary() && !StringUtils.hasText(fillin.getAnswer())) {
			return new BasicRes(ResMessage.ANSWER_IS_REQUIRED.getCode(), ResMessage.ANSWER_IS_REQUIRED.getMessage());
		}
		// 2. 排除題型是單選 但 answerArray 的長度 > 1
		String answerStr = fillin.getAnswer();
		// 把 answerStr(答案) 切割成陣列
		String[] answerArray = answerStr.split(";");
		if (item.getType().equalsIgnoreCase(OptionType.SINGLE_CHOICE.getType()) //
				&& answerArray.length > 1) {
			return new BasicRes(ResMessage.ANSWER_OPTION_TYPE_IS_NOT_MATCH.getCode(),
					ResMessage.ANSWER_OPTION_TYPE_IS_NOT_MATCH.getMessage());
		}
		// 3. 排除簡答題；option type 是 text
		if (item.getType().equalsIgnoreCase(OptionType.TEXT.getType())) {
			return null;
		}
		// 把 options 切成 Array
		String[] optionArray = item.getOptions().split(";");
		// 把 optionArray 轉成 List，因為要使用 List 中的 contains 方法
		List<String> optionList = List.of(optionArray);
		// 4. 檢查答案跟選項一致
		for (String str : answerArray) {
			// 假設 item.getOptions() 的值是: "AB;BC;C;D"
			// 轉成 List 後的 optionList = ["AB", "BC", "C", "D"]
			// 假設 answerArray = [AB, B]
			// for 迴圈中就是把 AB 和 B 比對是否被包含在 optionList 中
			// List 的 contains 方法是比較元素，所以範例中，AB是有包含，B是沒有
			// 排除以下:
			// 1. 必填 && 答案選項不一致
			if (item.isNecessary() && !optionList.contains(str)) {
				return new BasicRes(ResMessage.ANSWER_OPTION_IS_NOT_MATCH.getCode(),
						ResMessage.ANSWER_OPTION_IS_NOT_MATCH.getMessage());
			}
			// 2. 非必填 && 有答案 && 答案選項不一致
			if (!item.isNecessary() && StringUtils.hasText(str) && !optionList.contains(str)) {
				return new BasicRes(ResMessage.ANSWER_OPTION_IS_NOT_MATCH.getCode(),
						ResMessage.ANSWER_OPTION_IS_NOT_MATCH.getMessage());
			}
		}
		return null;
	}

	@Override
	public FeedbackRes feeback(FeedbackReq req) {
		// 抓出對應問卷
		Optional<Quiz> op = quizDao.findById(req.getQuizId());
		// 檢查是否有此張問卷
		if (op.isEmpty()) {
			return new FeedbackRes(ResMessage.QUIZ_NOT_FOUND.getCode(), ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		Quiz quiz = op.get();
		List<Feedback> feedbackList = new ArrayList<>();
		try {
			List<Response> resList = responseDao.findByQuizId(req.getQuizId());
			// 遍歷 resList
			for (Response resItem : resList) {
				List<Fillin> fillinList = mapper.readValue(resItem.getFillin(), new TypeReference<>() {
				});
				FeedbackDetail detail = new FeedbackDetail(quiz.getName(), quiz.getDescription(), //
						quiz.getStartDate(), quiz.getEndDate(), resItem.getName(), resItem.getPhone(), //
						resItem.getEmail(), resItem.getAge(), fillinList);
				Feedback feedback = new Feedback(resItem.getId(),resItem.getName(), resItem.getFillinDateTime(), //
						detail);
				feedbackList.add(feedback);
			}
		} catch (Exception e) {
			return new FeedbackRes(ResMessage.JSON_PROCESSING_EXCEPTION.getCode(),
					ResMessage.JSON_PROCESSING_EXCEPTION.getMessage());
		}
		// 創建一個新的 Quiz 對象，僅包含需要的屬性
		Quiz simpleQuiz = new Quiz();
		simpleQuiz.setId(quiz.getId());
		simpleQuiz.setName(quiz.getName());
		simpleQuiz.setDescription(quiz.getDescription());
		simpleQuiz.setStartDate(quiz.getStartDate());
		simpleQuiz.setEndDate(quiz.getEndDate());
		simpleQuiz.setQuestions(quiz.getQuestions());
		simpleQuiz.setPublished(quiz.isPublished());

		// 將該對象添加到 List<Quiz> 中
		List<Quiz> res = new ArrayList<>();
		res.add(simpleQuiz);
		return new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), res, feedbackList);
	}

	@Override
	public StatisticsRes statistics(FeedbackReq req) {
		List<Response> responseList = responseDao.findByQuizId(req.getQuizId());
		// 計算所有答案之 題號 選項 次數
		// qId(題號) 選項 統計結果
		Map<Integer, Map<String, Integer>> countMap = new HashMap<>();

		for (Response item : responseList) {
			String fillinStr = item.getFillin();
			try {
				List<Fillin> fillinList = mapper.readValue(fillinStr, new TypeReference<>() {
				});
				for (Fillin fillin : fillinList) {
					Map<String, Integer> optionCountMap = new HashMap<>();
					// 排除簡答題不列入統計
					if (fillin.getType().equalsIgnoreCase(OptionType.TEXT.getType())) {
						continue;
					}
					// 每個選項之間是用分號(;)串接
					String optionsStr = fillin.getOptions();
					String[] optionArray = optionsStr.split(";");
					String answer = fillin.getAnswer();
					answer = ";" + answer + ";";

					for (String option : optionArray) {
						// 比對答案中每個選項出現的次數
						// 避免某個選項是另一個選項的其中一部份
						// 例如:綠茶;烏龍綠茶;梅子綠茶 都是選項,要計算綠茶次數但不能算到烏龍綠茶、梅子綠茶
						// 所以需要再每個選項前後加上";"以利後續判斷
						// 後續要找出現次數時就會是用 ;綠茶; 來找
						String newOption = ";" + option + ";";
						String newAnswerStr = answer.replace(newOption, "");
						// 計算該選項出現的次數(要除以選項長度才會是真正的次數)
						int count = (answer.length() - newAnswerStr.length()) / newOption.length();
						// 記錄每一題的統計
						optionCountMap = countMap.getOrDefault(fillin.getqId(), optionCountMap);
						// 先取出選項對應的次數
						// getOrDefault(option, 0) map中沒有key的話，就會返回0
						int oldCount = optionCountMap.getOrDefault(option, 0);
						// 累加 oldCount+count
						optionCountMap.put(option, oldCount + count);
						// 把有累加次數的 optionCountMap 覆蓋回 countMap 中(相同的題號)
						countMap.put(fillin.getqId(), optionCountMap);
					}
				}
			} catch (JsonProcessingException e) {
				return new StatisticsRes(ResMessage.JSON_PROCESSING_EXCEPTION.getCode(),
						ResMessage.JSON_PROCESSING_EXCEPTION.getMessage());
			}
		}
		Optional<Quiz> op = quizDao.findById(req.getQuizId());
		if (op.isEmpty()) {
			return new StatisticsRes(ResMessage.QUIZ_NOT_FOUND.getCode(), ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		Quiz quiz = op.get();
		return new StatisticsRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), quiz.getName(),
				quiz.getStartDate(), //
				quiz.getEndDate(), countMap);
	}

	@Override
	public BasicRes deleteResponse(DeleteReq req) {
		// 檢查參數
		// 如果沒有勾選任何項目則不執行刪除，直接回傳成功
		// 如果有勾選值，則進入刪除步驟
		if (!CollectionUtils.isEmpty(req.getIdList())) {
			// 刪除問卷
			try {
				responseDao.deleteAllById(req.getIdList());
			} catch (Exception e) {
				// 當deleteAllById方法中，id的值不存在時，JPA會報錯
				// 因為在刪除之前 JPA 會先搜尋帶入的 id 值，若沒結果就會報錯
				// 但實際上也沒刪除任何東西，所以就不需要對這個 Exception 做處理
			}
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}
}
