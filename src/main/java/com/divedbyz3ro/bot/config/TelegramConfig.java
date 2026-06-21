package com.divedbyz3ro.bot.config;

import com.divedbyz3ro.bot.telegram.LingoBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(LingoBot lingoBot) throws TelegramApiException {
        // Создаем API для общения с серверами Telegram
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        // Регистрируем нашего бота
        api.registerBot(lingoBot);

        return api;
    }
}