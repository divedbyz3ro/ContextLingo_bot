package com.divedbyz3ro.bot.service;

import com.divedbyz3ro.bot.entity.User;
import com.divedbyz3ro.bot.entity.UserWordProgress;
import com.divedbyz3ro.bot.entity.Word;
import com.divedbyz3ro.bot.repository.UserWordProgressRepository;
import com.divedbyz3ro.bot.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class WordService {

    private final WordRepository wordRepository;
    private final UserWordProgressRepository progressRepository;

    // Конструкторы
    public WordService(WordRepository wordRepository, UserWordProgressRepository progressRepository) {
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
    }
    public void updateProgress(User user, List<Word> words) {
        for (Word word : words) {
            // 1. Пытаемся найти существующий прогресс в базе
            UserWordProgress progress = progressRepository.findByUserAndWord(user, word)
                    .orElse(new UserWordProgress(user, word)); // 2. Если нет — создаем новый объект

            // 3. Увеличиваем уровень мастерства (XP)
            int currentMastery = progress.getMasteryLevel();
            if (currentMastery < 5) { // Допустим, 5 — это максимум
                progress.setMasteryLevel(currentMastery + 1);
            }

            // 4. Если доползли до максимума — помечаем как выученное
            if (progress.getMasteryLevel() == 5) {
                progress.setLearned(true);
            }

            // 5. Просим репозиторий сохранить изменения
            progressRepository.save(progress);
        }
    }

    public void updateProgressByIds(User user, String idsString) {
        if (idsString == null || idsString.isEmpty()) return;

        // Превращаем строку  обратно в список ID
        String[] ids = idsString.split(",");

        for (String id : ids) {
            Long wordId = Long.parseLong(id.trim());
            wordRepository.findById(wordId).ifPresent(word -> {
                UserWordProgress progress = progressRepository.findByUserAndWord(user, word)
                        .orElse(new UserWordProgress(user, word));

                if (progress.getMasteryLevel() < 5) {
                    progress.setMasteryLevel(progress.getMasteryLevel() + 1);
                }
                if (progress.getMasteryLevel() == 5) {
                    progress.setLearned(true);
                }
                progressRepository.save(progress);
            });
        }
    }
    public List<Word> getWordsFromIds(String idsString) {
        if (idsString == null || idsString.isEmpty()) return Collections.emptyList();

        List<Word> words = new ArrayList<>();
        String[] ids = idsString.split(",");
        for (String id : ids) {
            wordRepository.findById(Long.parseLong(id.trim())).ifPresent(words::add);
        }
        return words;
    }


    /**
     * Выбирает 5 случайных слов для задания, основываясь на уровне конкретного пользователя.
     */
    public List<Word> getWordsForTask(User user) {
        // Берем уровень прямо из карточки пользователя
        int level = user.getCurrentLevel();

        List<Word> allWordsOfLevel = wordRepository.findByLevelAndLanguage(level,user.getCurrentLanguage());

        if (allWordsOfLevel.size() <= 5) {
            return allWordsOfLevel;
        }

        Collections.shuffle(allWordsOfLevel);
        return allWordsOfLevel.subList(0, 5);
    }

    /**
     * Сохраняет сгенерированную историю в "память" пользователя.
     */
    public void saveStoryToUser(User user, String story) {
        user.setCurrentStory(story);
        // Тут можно было бы сразу вызвать userRepository.save(user),
        // но обычно это делают в вызывающем коде (в боте).
    }

    /**
     * Метод для получения одного случайного слова для пользователя.
     */
    public Word getWordForUser(User user) {
        List<Word> words = wordRepository.findByLevelAndLanguage(user.getCurrentLevel(),user.getCurrentLanguage());
        if (words.isEmpty()) return null;

        return words.get(new Random().nextInt(words.size()));
    }
}