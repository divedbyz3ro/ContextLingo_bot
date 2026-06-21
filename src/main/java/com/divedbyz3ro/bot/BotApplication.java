package com.divedbyz3ro.bot;

import com.divedbyz3ro.bot.dto.EvaluationResult;
import com.divedbyz3ro.bot.entity.User;
import com.divedbyz3ro.bot.service.AIService;
import com.divedbyz3ro.bot.service.WordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BotApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    /*@Bean
    public CommandLineRunner testAi(WordService wordService, AIService aiService) {
        return args -> {
            System.out.println("--- ЗАПУСК ТЕСТА AI ---");
            // Создаем "кукольного" пользователя для теста
            // Допустим, ID 1L и имя "TestUser"
            User testUser = new User(1L, "Alice");
            testUser.setCurrentLevel(1); // Обязательно ставим уровень
            // 1. Берем слова из базы через твой сервис
            var words = wordService.getWordsForTask(testUser); // Убедись, что метод возвращает List или измени логику
            String result="";
            if (words != null && !words.isEmpty()) {
                // 2. Отправляем их в AI
                result = aiService.generateStory(words);

                // 3. Смотрим, что пришло
                System.out.println("ОТВЕТ ОТ GEMINI:");
                System.out.println(result);
            } else {
                System.out.println("База данных пуста! Добавь слова в data.sql");
            }
            String userTry = "Алиса шла и нашла яблоко. Ее друг что то там... я забыл";
            EvaluationResult evalResult = aiService.evaluateTranslation(result,userTry);
            System.out.println("=== РЕЗУЛЬТАТ ПРОВЕРКИ ===");
            System.out.println("ОРИГИНАЛ: " + result);
            System.out.println("ТВОЙ ПЕРЕВОД: " + userTry);
            System.out.println("--------------------------");

// Выводим данные из нашего нового DTO
            System.out.println("ОЦЕНКА: " + evalResult.getScore() + "/100");
            System.out.println("ПРОШЕЛ? : " + evalResult.getIsCorrect()); // Вернет YES или NO
            System.out.println("КОММЕНТАРИЙ: " + evalResult.getFeedback());
            if ("YES".equals(evalResult.getIsCorrect())) {
                wordService.updateProgress(testUser, words);
                System.out.println("Прогресс обновлен! +1 XP к словам.");
            }

            System.out.println("==========================");
        };
    }
    */
	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
	}

}
