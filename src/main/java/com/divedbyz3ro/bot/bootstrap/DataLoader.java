package com.divedbyz3ro.bot.bootstrap;

import com.divedbyz3ro.bot.entity.Word;
import com.divedbyz3ro.bot.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component // Говорим Spring: "Создай этот объект сам при запуске"
public class DataLoader implements CommandLineRunner {

    private final WordRepository wordRepository;

    public DataLoader(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Сверяю слова в файле CSV и базе данных...");
        loadWordsFromCsv();
    }

    private void loadWordsFromCsv() {
        List<Word> newWordsToSave = new ArrayList<>();
        int skippedWords = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/words.csv")),
                StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Разбиваем строку по запятой
                String[] parts = line.split(",");

                // 4 колонки
                if (parts.length == 4) {
                    int level = Integer.parseInt(parts[0].trim());
                    String originalWord = parts[1].trim();
                    String translation = parts[2].trim();
                    String language = parts[3].trim().toUpperCase(); // Читаем EN или RO

                    //
                    if (!wordRepository.existsByOriginalAndLanguage(originalWord,language)) {
                        Word word = new Word();
                        word.setLevel(level);
                        word.setOriginal(originalWord);
                        word.setTranslation(translation);
                        word.setLanguage(language); // Сохраняем язык в объект

                        newWordsToSave.add(word); // Добавляем в очередь на сохранение
                    } else {
                        skippedWords++; // Слово уже есть, просто считаем его
                    }
                }
            }

            // Если нашли новые слова — сохраняем их все разом
            if (!newWordsToSave.isEmpty()) {
                wordRepository.saveAll(newWordsToSave);
                System.out.println("Успешно добавлено " + newWordsToSave.size() + " НОВЫХ слов!");
            } else {
                System.out.println("Новых слов не найдено. База словаря полностью актуальна.");
            }

            System.out.println("Слов пропущено (уже есть в базе): " + skippedWords);
            System.out.println("Всего слов обработано: " + (skippedWords + newWordsToSave.size()));

        } catch (Exception e) {
            System.err.println("Ошибка при чтении CSV: " + e.getMessage());
        }
    }
}