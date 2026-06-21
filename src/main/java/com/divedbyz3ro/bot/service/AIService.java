package com.divedbyz3ro.bot.service;

import com.divedbyz3ro.bot.dto.*;
import com.divedbyz3ro.bot.entity.Word;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service

public class AIService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateStory(List<Word> words,int userLevel, String userLanguage){
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;
        //дается список слов и с ним генерируется промпт
        String wordList = words.stream()
                .map(Word::getOriginal) // ориг слово(мап использует функцию и слова конкатенириуются)
                .collect(Collectors.joining(", "));
        String prompt = "";
        String langName = userLanguage.equals("EN") ? "английском" : "румынском";
        if (userLevel == 0) {
            prompt = String.format(
                    "Напиши от 1 до 3 предложений на %s языке, обязательно используя ЭТИ слова: %s. " +
                            "СТРОГИЕ ПРАВИЛА: " +
                            "1. Количество предложений: строго от 1 до 3. " +
                            "2. Длина каждого предложения: от 1 до 7 слов. " +
                            "3. Используй ТОЛЬКО самую простую лексику уровня Beginner (A1) для всех остальных слов. " +
                            "Верни ТОЛЬКО чистый текст предложений без какого-либо форматирования, списков, кавычек или комментариев.",
                    langName, wordList
            );
        } else {
            // Для остальных уровней — твой старый промпт на 4-5 предложений
            prompt = String.format(
                    "Напиши очень короткую  историю на %s языке, обязательно используя ЭТИ слова: %s. " +
                            "СТРОГИЕ ПРАВИЛА: " +
                            "1. Максимум 4-5 предложений в сумме. " +
                            "2. Длина предложений: от 1 до 9 слов. Разрешается использовать ультракороткие предложения из 1-2 слов, если это подходит по смыслу. " +
                            "3. Предложения могут быть грамматически независимыми и рублеными, но они ОБЯЗАНЫ выстраиваться в логичную, связную и адекватную ситуацию (без бессмыслицы). " +
                            "4. Используй ТОЛЬКО самую простую лексику уровня Beginner (A1) для всех остальных слов. " +
                            "Верни ТОЛЬКО чистый текст истории без какого-либо форматирования, кавычек, приветствий или комментариев.",
                    langName, wordList
            );
        }


        // 1. Создаем part с текстом
        GeminiPart part = new GeminiPart(prompt);
        // part в список и в контент
        GeminiContent content = new GeminiContent(List.of(part));
        //  контент в список и создаем финальный запрос
        GeminiRequest request = new GeminiRequest(List.of(content));


        // 1. Отправляем запрос и просим Spring сразу собрать GeminiResponse
        GeminiResponse response = restTemplate.postForObject(url, request, GeminiResponse.class);

        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            return response.getCandidates().get(0) // Берем первого кандидата
                    .getContent()                  // Заходим в его контент
                    .getParts().get(0)             // Берем первую часть (наш текст)
                    .getText();                    // Достаем саму строку
        }
        return "Упс! Не удалось получить рассказ от AI.";
    }
    public EvaluationResult evaluateTranslation(String originalText, String userTranslation, List<Word> targetWords) {
        // Превращаем список целевых слов в строку для ИИ (например: "car (машина), apple (яблоко)")
        String targetWordsStr = targetWords.stream()
                .map(w -> w.getOriginal() + " (" + w.getTranslation() + ")")
                .collect(Collectors.joining(", "));
        String prompt = "Ты — лояльный и умный преподаватель английского языка. Оцени перевод пользователя.\n\n" +
                "Оригинал: " + originalText + "\n" +
                "Перевод пользователя: " + userTranslation + "\n" +
                " ЦЕЛЕВЫЕ СЛОВА ДЛЯ ПРОВЕРКИ: " + targetWordsStr + "\n\n" +
                " ПРАВИЛА ОЦЕНКИ (ОБЯЗАТЕЛЬНО К ИСПОЛНЕНИЮ): \n" +
                "1. ФОКУС НА ЦЕЛЕВЫЕ СЛОВА: Твоя ГЛАВНАЯ задача — проверить, правильно ли пользователь перевел именно слова из списка 'ЦЕЛЕВЫЕ СЛОВА ДЛЯ ПРОВЕРКИ'. Мелкие неточности в остальных частях предложения игнорируй.\n" +
                "2. МОРФОЛОГИЯ: Разрешены ЛЮБЫЕ падежи, склонения, рода и числа! Если целевое слово 'живой', то переводы 'живую', 'живого', 'живые' — это 100% ПРАВИЛЬНЫЙ ответ. НЕ снижай за это балл!\n" +
                "3. СИНОНИМЫ И СЛЕНГ: Будь гибким. Если пользователь перевел 'case' как 'кейс' или 'point' как 'момент' по контексту — это правильный ответ.\n\n" +
                "4. АНАЛИЗИРУЙ ТОЛЬКО ФАКТЫ: Внимательно читай 'Перевод пользователя'. ЗАПРЕЩЕНО придумывать ошибки или приписывать пользователю значения, которых нет в его тексте. Если пользователь перевел слово правильно по контексту (например, 'ghost' как 'призрачный'), засчитывай это как верный ответ и не упоминай другие словарные значения."+
                "5. ПОЛНОТА ПЕРЕВОДА: Пользователь ОБЯЗАН перевести весь объем оригинального текста. Если переведена только часть текста, а остальное проигнорировано — отнимай минимум 25 баллов за каждое пропущенное предложение или смысловой кусок. \n\n" +
                "6.Будь лоялен к отсутствию специальных румынских символов (диакритики), если смысл слова понятен (si=și)\n" +
                " ФОРМАТ ОТВЕТА (КРИТИЧЕСКИ ВАЖНО): \n" +
                "Твой ответ должен быть ИСКЛЮЧИТЕЛЬНО в формате чистого JSON. \n" +
                "ЗАПРЕЩЕНО писать приветствия, слова до/после JSON или использовать markdown разметку (```json).\n" +
                "Ожидаемая структура (строго соблюдай ключи):\n" +
                "{\n" +
                "  \"isCorrect\": \"YES/NO\",\n" +
                "  \"score\": 100,\n" +
                "  \"feedback\": \"Отличная работа! / Ошибка: слово X нужно перевести как Y.\"\n" +
                "}";

        GeminiRequest request = new GeminiRequest(List.of(new GeminiContent(List.of(new GeminiPart(prompt)))));
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;
        try {
            // 1. Получаем общий ответ от API
            GeminiResponse response = restTemplate.postForObject(url, request, GeminiResponse.class);

            // 2. Вытаскиваем строку с JSON из ответа AI
            String jsonContent = response.getCandidates().get(0).getContent().getParts().get(0).getText();

            // 3. Убираем возможные лишние символы
            jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();

            // маппер берет объекты в json и матчит их с переменными в evaluationresult
            return objectMapper.readValue(  jsonContent, EvaluationResult.class);

        } catch (Exception e) {
            System.err.println("Ошибка при оценке перевода: " + e.getMessage());
            // Возвращаем дефолтный результат, чтобы программа не упала
            return new EvaluationResult("NO", 0, "Ошибка связи с учителем-ИИ.");
        }
    }
}
