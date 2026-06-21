package com.divedbyz3ro.bot.telegram;

import com.divedbyz3ro.bot.dto.EvaluationResult;
import com.divedbyz3ro.bot.entity.User;
import com.divedbyz3ro.bot.entity.Word;
import com.divedbyz3ro.bot.repository.UserRepository;
import com.divedbyz3ro.bot.repository.UserWordProgressRepository;
import com.divedbyz3ro.bot.repository.WordRepository;
import com.divedbyz3ro.bot.service.AIService;
import com.divedbyz3ro.bot.service.WordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class LingoBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final UserRepository userRepository;
    private final WordService wordService;
    private final AIService aiService;
    private final WordRepository wordRepository;
    private final UserWordProgressRepository progressRepository;

    public LingoBot(@Value("${telegram.bot.token}") String botToken,
                    @Value("${telegram.bot.name}") String botUsername,
                    UserRepository userRepository, WordService wordService,
                    AIService aiService, WordRepository wordRepository,
                    UserWordProgressRepository progressRepository) {
        super(botToken);
        this.botUsername = botUsername;
        this.userRepository = userRepository;
        this.wordService = wordService;
        this.aiService = aiService;
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public void onUpdateReceived(Update update) {
        // 1. ОБРАБОТКА НАЖАТИЯ INLINE-КНОПОК (Например, "Выдай текст")
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }


        // 2. ОБРАБОТКА ОБЫЧНОГО ТЕКСТА
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            String firstName = update.getMessage().getChat().getFirstName();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println(timestamp + " LOG: [" + firstName + "] написал: " + text);

            // Ищем юзера в базе или создаем нового
            User user = userRepository.findById(chatId).orElseGet(() -> {
                User newUser = new User(chatId, firstName);
                return userRepository.save(newUser);
            });
            // отмена кнопка
            if (text.equals("❌ Отмена")) {
                user.setBotState("IDLE");
                userRepository.save(user);
                sendMenu(chatId, "Действие отменено. Что выберешь?");
                return; // Важно прервать выполнение метода!
            }

            // Логика в зависимости от состояния пользователя
            if (user.getBotState().equals("TRANSLATING") && !text.startsWith("🚀") && !text.startsWith("📊")) {
                checkTranslation(user, text, chatId);
            } else {
                handleMainMenu(user, text, chatId);
            }
        }
    }

    // --- ОСНОВНОЕ МЕНЮ ---
    private void handleMainMenu(User user, String text, long chatId) {
        switch (text) {
            case "/start":
                user.setBotState("IDLE");
                userRepository.save(user);

                sendMessage(chatId, "Привет! Я ContextLingo — твой умный тренажер. " +
                        "Я буду давать тебе слова, генерировать из них истории, а ты — переводить их.\n\n" +
                        "Для начала давай определимся с твоим уровнем английского 👇\n" +
                        "(Не переживай, если ошибешься — уровень всегда можно будет изменить в настройках профиля)");

                sendLevelSelectionMenu(chatId); // Вызываем меню с кнопками 0-5
                break;
            case "👤 Мой профиль":
                showProfile(user, chatId);
                break;

            case "🚀 Начать тест":
                startTest(user, chatId);
                break;

            default:
                sendMenu(chatId, "Используй кнопки меню 👇");
                break;
        }
    }
    // --- ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ ---
    private void showProfile(User user, long chatId) {
        long totalWords = wordRepository.count();
        long learnedWords = progressRepository.countByUserAndIsLearnedTrue(user);

        String stats = String.format("👤 **Твой профиль:**\n\n" +
                        "Текущий уровень: %d\n" +
                        "Выучено слов: %d из %d",
                user.getCurrentLevel(), learnedWords, totalWords);

        SendMessage message = new SendMessage(String.valueOf(chatId), stats);
        message.setParseMode("Markdown");

        // Кнопка под профилем для смены уровня
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText("⚙️ Сложность");
        btn.setCallbackData("CHANGE_LEVEL");
        rowInline.add(btn);
        InlineKeyboardButton langBtn = new InlineKeyboardButton();
        langBtn.setText("🌐 Изучаемый язык");
        langBtn.setCallbackData("CHANGE_LANGUAGE");
        rowInline.add(langBtn);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    // --- КЛАВИАТУРА ВЫБОРА УРОВНЯ ---
    private void sendLevelSelectionMenu(long chatId) {
        String text = "Выбери свой текущий уровень:\n\n" +
                "0️⃣ — Вообще не знаю (Beginner)\n" +
                "1️⃣ — Чуть-чуть знаю (Elementary)\n" +
                "2️⃣ — Могу объясниться (Pre-Intermediate)\n" +
                "3️⃣ — Уверенный средний (Intermediate)\n" +
                "4️⃣ — Продвинутый (Upper-Intermediate)\n" +
                "5️⃣ — Почти носитель (Advanced)";

        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Рисуем кнопки в два ряда (0-1-2 и 3-4-5), чтобы было компактно
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i = 0; i <= 5; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText("Ур. " + i);
            btn.setCallbackData("SET_LEVEL_" + i); // Передаем цифру в коллбэке
            if (i <= 2) row1.add(btn);
            else row2.add(btn);
        }

        rowsInline.add(row1);
        rowsInline.add(row2);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }
    // выбор языка
    private void sendLanguageSelector(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите обучаемый язык:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Кнопка Английского
        InlineKeyboardButton enBtn = new InlineKeyboardButton();
        enBtn.setText("English 🇬🇧");
        enBtn.setCallbackData("SET_LANG_EN");

        // Кнопка Румынского
        InlineKeyboardButton roBtn = new InlineKeyboardButton();
        roBtn.setText("Romanian 🇷🇴");
        roBtn.setCallbackData("SET_LANG_RO");

        rows.add(List.of(enBtn, roBtn));
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // --- ЗАПУСК ТЕСТА ---
    private void startTest(User user, long chatId) {
        sendCancelMenu(chatId, "⏳ Ищу слова и генерирую историю... Подожди пару секунд.");

        List<Word> words = wordService.getWordsForTask(user);
        if (words.isEmpty()) {
            sendMessage(chatId, "Слов для твоего уровня пока нет!");
            return;
        }
        String wordIds = words.stream()
                .map(w -> String.valueOf(w.getId()))
                .collect(java.util.stream.Collectors.joining(","));
        user.setCurrentWordIds(wordIds); // сохраняем айди слов для повтора возможного


        // Генерируем историю ЗАРАНЕЕ и сохраняем в карточку юзера
        String story;
        try {
            story = aiService.generateStory(words, user.getCurrentLevel(), user.getCurrentLanguage());
        } catch (Exception e) {
            // ВЫВОДИМ ОШИБКУ В КОНСОЛЬ, ЧТОБЫ ПОНЯТЬ В ЧЕМ ДЕЛО
            System.err.println("ОШИБКА ПРИ ГЕНЕРАЦИИ ИСТОРИИ: " + e.getMessage());
            e.printStackTrace();

            sendMessage(chatId, "Ой! Мой нейромозг немного перегрелся 🤯\nПодожди немного и попробуй снова!");
            user.setBotState("IDLE");
            userRepository.save(user);
            sendMenu(chatId, "Возвращаемся в главное меню.");
            return;
        }
        wordService.saveStoryToUser(user, story);

        user.setBotState("LEARNING");
        userRepository.save(user);

        // Формируем список слов для зубрежки
        StringBuilder sb = new StringBuilder("Прочитай эти слова и попытайся запомнить. Как запомнишь — жми кнопку.\n\n");
        for (Word w : words) {
            sb.append("🔹 ").append(w.getOriginal()).append(" — ").append(w.getTranslation()).append("\n");
        }

        // Отправляем сообщение с Inline-кнопкой
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sb.toString());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText("📝 Выдай текст!");
        btn.setCallbackData("GIVE_TEXT"); // Этот код мы поймаем при нажатии
        rowInline.add(btn);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // --- ОБРАБОТКА НАЖАТИЯ INLINE-КНОПОК ---
    private void handleCallback(Update update) {
        String callData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // 1. ЕСЛИ НАЖАЛИ "ВЫДАЙ ТЕКСТ"
        if (callData.equals("GIVE_TEXT")) {
            User user = userRepository.findById(chatId).orElse(null);
            if (user != null && user.getBotState().equals("LEARNING")) {

                // АНТИЧИТ: Удаляем сообщение со словами
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    System.err.println("Не удалось удалить сообщение: " + e.getMessage());
                }

                // Меняем статус на "переводит"
                user.setBotState("TRANSLATING");
                userRepository.save(user);

                // Выдаем историю
                sendMessage(chatId, "Переведи этот текст:\n\n" + user.getCurrentStory());
            }
        }

        // 2. ЕСЛИ НАЖАЛИ "ЕЩЁ ИСТОРИЯ НА ЭТИ СЛОВА"
        if (callData.equals("RETRY_SAME_WORDS")) {
            User user = userRepository.findById(chatId).orElse(null);
            if (user != null) {
                // Достаем те же самые слова из базы по сохраненным ID
                List<Word> words = wordService.getWordsFromIds(user.getCurrentWordIds());

                if (!words.isEmpty()) {
                    sendCancelMenu(chatId, "Генерирую новую историю на те же слова... 🪄");

                    // Генерируем новую историю
                    String newStory = aiService.generateStory(words, user.getCurrentLevel(), user.getCurrentLanguage());
                    wordService.saveStoryToUser(user, newStory);

                    // СНОВА переводим в режим зубрежки
                    user.setBotState("LEARNING");
                    userRepository.save(user);

                    // Формируем сообщение со словами
                    StringBuilder sb = new StringBuilder("Снова эти слова! Освежи в памяти и жми кнопку.\n\n");
                    for (Word w : words) {
                        sb.append("🔹 ").append(w.getOriginal()).append(" — ").append(w.getTranslation()).append("\n");
                    }

                    // Снова вешаем кнопку "Выдай текст"
                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();

                    InlineKeyboardButton btn = new InlineKeyboardButton();
                    btn.setText("📝 Выдай текст!");
                    btn.setCallbackData("GIVE_TEXT");
                    rowInline.add(btn);

                    rowsInline.add(rowInline);
                    markupInline.setKeyboard(rowsInline);

                    SendMessage message = new SendMessage(String.valueOf(chatId), sb.toString());
                    message.setReplyMarkup(markupInline);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // ЕСЛИ НАЖАЛИ "ИЗМЕНИТЬ УРОВЕНЬ" В ПРОФИЛЕ
        if (callData.equals("CHANGE_LEVEL")) {
            sendLevelSelectionMenu(chatId);
        }

        // ЕСЛИ ВЫБРАЛИ КОНКРЕТНЫЙ УРОВЕНЬ (0, 1, 2, 3, 4, 5)
        if (callData.startsWith("SET_LEVEL_")) {
            int newLevel = Integer.parseInt(callData.replace("SET_LEVEL_", ""));
            User user = userRepository.findById(chatId).orElse(null);

            if (user != null) {
                user.setCurrentLevel(newLevel);
                userRepository.save(user);

                // Удаляем сообщение с выбором уровня, чтобы оно не висело в истории
                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
                try { execute(deleteMessage); } catch (TelegramApiException e) { e.printStackTrace(); }

                // Пишем подтверждение и показываем главное меню
                sendMenu(chatId, "✅ Твой уровень успешно изменен на " + newLevel + "! Теперь используй кнопки ниже (если их нет попробуй их включить снизу справа).");
            }
        }

        if (callData.equals("CHANGE_LANGUAGE")) {
            sendLanguageSelector(chatId); // Вызываем метод с выбором EN/RO (из прошлого сообщения)
        }
        if (callData.startsWith("SET_LANG_")) {
            User user = userRepository.findById(chatId).orElse(null);
            String selectedLang = callData.replace("SET_LANG_", "");
            user.setCurrentLanguage(selectedLang);
            userRepository.save(user);

            String msg = selectedLang.equals("EN") ?
                    "Выбран английский язык! 🇬🇧" : "Ați ales limba română! 🇷🇴";

            sendMenu(chatId, msg);
            // Тут можешь сразу вызывать showProfile(user, chatId), чтобы юзер увидел обновленный профиль
        }
    }

    // --- ПРОВЕРКА ПЕРЕВОДА ---
    private void checkTranslation(User user, String userTranslation, long chatId) {
        sendMessage(chatId, "Учитель проверяет твой перевод... 🧐");


        List<Word> targetWords = wordService.getWordsFromIds(user.getCurrentWordIds());
        // Обновили вызов, передав targetWords
        EvaluationResult result = aiService.evaluateTranslation(user.getCurrentStory(), userTranslation, targetWords);

        StringBuilder response = new StringBuilder();
        response.append("Оценка: ").append(result.getScore()).append("/100\n\n")
                .append("Отзыв ИИ:\n").append(result.getFeedback());

        if ("YES".equals(result.getIsCorrect())) {
            response.append("\n\n✅ Прогресс обновлен (+1 XP к словам).");
            wordService.updateProgressByIds(user, user.getCurrentWordIds());
        } else {
            response.append("\n\n❌ Нужно постараться лучше.");
        }

        // Создаем кнопку для повтора
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton retryBtn = new InlineKeyboardButton();
        retryBtn.setText("🔄 Ещё история на эти слова");
        retryBtn.setCallbackData("RETRY_SAME_WORDS");

        row.add(retryBtn);
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);

        // Отправляем результат с кнопкой и возвращаем нижнее меню
        user.setBotState("IDLE");
        userRepository.save(user);

        SendMessage message = new SendMessage(String.valueOf(chatId), response.toString());
        message.setReplyMarkup(markupInline);

        // Важно: вызываем обычное меню, чтобы кнопки внизу тоже вернулись
        sendMenu(chatId, "Что делаем дальше?");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    //только отмена кнопка
    private void sendCancelMenu(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("❌ Отмена");

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    // Отправка сообщения с нижними кнопками меню
    private void sendMenu(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true); // Кнопки по размеру телефона
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("🚀 Начать тест");
        row.add("👤 Мой профиль");
        row.add("❌ Отмена");

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try { execute(message); } catch (TelegramApiException e) { e.printStackTrace(); }
    }
}