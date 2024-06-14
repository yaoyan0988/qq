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
		//�ˬd�Ѽ�
		 BasicRes checkResult = checkParams(req);
		 //checkResult==null�ɪ�ܰѼ��ˬd�����T
		 if(checkResult != null) {
			 return checkResult;
		 }
		 //�]�� Quiz ���� questions����Ʈ榡�OString ,�ҥH�n�N req �� List<Question> �নString
		 //�z�LObjectMapper �i�H�⪫��(���O)�ন Json�榡���r��
		 ObjectMapper mapper = new ObjectMapper();
		 try {
			String questionStr = mapper.writeValueAsString(req.getQuestionList());
			
			//�Y req ���� id>0 ��ܧ�s�w�s�b����� : �Y id =0 �h��ܷs�W
			if(req.getId()>0) {
				//�H�U��ؤ覡�ܤ@
				//��k1 �z�LfindById �Y����ƴN�|�^�Ǥ@�㵧�����(�i���ƶq�|���j)
				//��k2 �]���O�z�L existsById �ӧP�_��ƬO�_�s�b�A�ҥH�^�Ǫ���ƥû����|�O�@��bit(0��1)
				//��k 1
//				Optional<Quiz> op=quizDao.findById(req.getId());
//				//�P�_�O�_�����
//				if(op.isEmpty()) { // op.isEmpty():��ܨS���
//					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(),
//							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
//				}
//				Quiz quiz = op.get();
//				//�]�w�s��(�ȱq req ��)
//				//�N req �����s�ȳ]�w���ª� quiz���A���]�wid�]��id�@��
//				quiz.setName(req.getName());
//				quiz.setDescription(req.getDescription());
//				quiz.setStartDate(req.getStartDate());
//				quiz.setEndDate(req.getEndDate());
//				quiz.setQuestions(questionStr);
//				quiz.setPublished(req.isPublished());
				//��k 2.�z�L existsById:�^�Ǥ@��bit����
				//�o��n�P�_�q req �a�i�Ӫ� id �O�_�u���s�bDB��
				//�]���YID ���s�b�A�S���ˬd ����{���X�A�I�s JPA �� save ��k�ɷ|�ܦ��s�W
				boolean boo = quizDao.existsById(req.getId());
				if(!boo) { //!boo��ܸ�Ƥ��s�b
					return new BasicRes(ResMessage.UPDATE_ID_NOT_FOUND.getCode(),
							ResMessage.UPDATE_ID_NOT_FOUND.getMessage());
				}
				
			}
			//===========================
			// �W�z�@��q if �{���X�i�H�Y��H�U�o�q
			//   if(req.getId() > 0 && !quizDao.existsById(req.getId())) {   
			//     return new BasicRes(ResMessage.UPDATE_ID_NOT_F
			//===========================
			
//			Quiz quiz = new Quiz(req.getName(),req.getDescription(),req.getStartDate(), //
//					req.getEndDate(),questionStr,req.isPublished());
//			quizDao.save(quiz);  �]��quiz�u�ϥΤ@���i�H�ϥΰΦW���O���g
			//new Quiz()���a�J req.getId()�OPK�A�b�I�ssave�ɷ|���h�ˬdPK�O�_���s�b��DB��
			//�Y�s�b -> ��s�A���s�b -> �s�W
			//req ���S�������ɹw�]�O0(�]��id����ƫ��A�Oint)
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
		//�ˬd�ݨ��Ѽ�
		// �ˬd�r��� StringUtils.hasText�A�T�{�O�_��null�B�Ŧr��B�ťզr��
		// �Y�ŦX3�ب�1�h�^��false�A�n�^��ture�~�|���椺�e�A�ҥH�|�b�e��[!���൲�G��ture
		// !StringUtils.hasText(req.getName()) ���P�� StringUtils.hasText(req.getName())==false
		// ����ĸ� �S����ĸ�
		if(!StringUtils.hasText(req.getName())){
			return new BasicRes(ResMessage.PARAM_QUIZ_NAME_ERROR.getCode(),
					ResMessage.PARAM_QUIZ_NAME_ERROR.getMessage());
		}
		if(!StringUtils.hasText(req.getDescription())){
			return new BasicRes(ResMessage.PARAM_DESCRIPTION_ERROR.getCode(),
					ResMessage.PARAM_DESCRIPTION_ERROR.getMessage());
		}
		//�}�l�ɶ�����b���Ѥ��e
		//LocalDate.now():���o�t�η�e�ɶ�
		//req.getStartDate().isBefore(LocalDate.now())���req�����}�l�ɶ�����b���Ѥ��e
		//req.getStartDate().isEqual(LocalDate.now())���req�����}�l�ɶ�����O����
		//�]���}�l�ɶ�����b����(�t)���e�A�ҥH�W��Ӥ���Y�O���@�ӵ��G�� true�A�h��ܶ}�l�ɶ��n���e(�t)�ɶ���
		// !req.getStartDate().isAfter(LocalDate.now()) <=�]�i�H��isAfter���ϦV�h����
		if(req.getStartDate()==null || req.getStartDate().isBefore(LocalDate.now()) //
				||req.getStartDate().isEqual(LocalDate.now())){
			return new BasicRes(ResMessage.PARAM_START_DATE_ERROR.getCode(),
					ResMessage.PARAM_START_DATE_ERROR.getMessage());
		}
		//1.�����ɶ�����p�󵥩��e�ɶ� 2.�]����p��}�l�ɶ�
		//�]���}�l�ɶ��w�g�L�o�F�p�󵥩��e�ɶ��A�ҥH�����ɶ��u�n�P�_��}�l�ɶ����������Y�Y�i
		if(req.getEndDate()==null || req.getEndDate().isBefore(req.getStartDate())){
			return new BasicRes(ResMessage.PARAM_END_DATE_ERROR.getCode(),
					ResMessage.PARAM_END_DATE_ERROR.getMessage());
		}
		//�ˬd���D�Ѽ�
		if(CollectionUtils.isEmpty(req.getQuestionList())) {
			return new BasicRes(ResMessage.PARAM_QUESTION_LIST_NOT_FOUND.getCode(),
					ResMessage.PARAM_QUESTION_LIST_NOT_FOUND.getMessage());
		}
		//�@�i�ݨ��|���ܦh�����D�A�C�Ӱ��D�ѼƳ��n�v�@�ˬd
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
			//��option_type �O���Φh��ɡAoptions �N����O�Ŧr��
			//��option_type �O��r�ɡAoptions ���\�O�Ŧr��
			//�H�U�����ˬd:��option_type �O���Φh��ɡA�Boptions�O�Ŧr���^����
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
		//���] name �O null �άO���ťզr��A�i�H�����S����J�����
		//�ݭn��name�ܦ��Ŧr��
		if(!StringUtils.hasText(name)) {
			name="";
		}
		//�U�謰�}�l�ɶ��S�]�m�ɪ��w�]�ɶ�
		if(start==null) {
			start=LocalDate.of(1970,1,1);
		}
		//�U�謰�����ɶ��S�]�m�ɪ��w�]�ɶ�
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
		//�ˬd�Ѽ�
		//�p�G�S���Ŀ���󶵥ثh������R���A�����^�Ǧ��\
		//�p�G���Ŀ�ȡA�h�i�J�R���B�J
		if(!CollectionUtils.isEmpty(req.getIdList())) {
			//�R���ݨ�
			try {
				quizDao.deleteAllById(req.getIdList());
			} catch (Exception e) {
				//��deleteAllById��k���Aid���Ȥ��s�b�ɡAJPA�|����
				//�]���b�R�����e JPA �|���j�M�a�J�� id �ȡA�Y�S���G�N�|����
				//����ڤW�]�S�R������F��A�ҥH�N���ݭn��o�� Exception ���B�z
			}
		}
		return new BasicRes(ResMessage.SUCCESS.getCode(),
				ResMessage.SUCCESS.getMessage());
	}
}
