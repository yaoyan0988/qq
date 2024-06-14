package com.example.quiz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.quiz.repository.QuizDao;

//@SpringBootTest
class QuizApplicationTests {

	@Autowired
	private QuizDao quizDao;
	
	@Test
	void test3() {
		List<String> list=List.of("A","B","C","D","E");
		String str = "AABBBCCAAEDDBBCC";
		Map<String,Integer>map=new HashMap<>();
		for(String item:list) {
			String newStr = str.replace(item, "");
			int count=str.length()-newStr.length();
			map.put(item, count);
		}
		System.out.println(map);
	}
	
	
	
	
}
