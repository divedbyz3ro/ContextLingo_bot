# ContextLingo Bot
* @ContextLingoBot via Telegram ( https://t.me/ContextLingoBot )

* ContextLingo Bot is a Telegram bot built with Spring Boot that helps users learn English and Romanian. The bot's interface is in Russian and it uses the free Gemini AI API.

## Stack

* **Java** (Core, Collections, Stream API)
* **Spring Boot** (Web, Data JPA)
* **Database:** PostgreSQL + Hibernate (storing user data, custom vocabularies, and learning progress).
* **Integrations:**
  * Telegram Bot API (via Long Polling).
  * Google Gemini AI API (content generation).
* **Build Tool:** Maven.

## Key Features

* **AI Integration:** Handles prompts to GeminiAI API and parses JSON responses into mapped DTOs.
* **Different learning technique:** The user must review the new vocabulary before translating the text with words from new vocab.
* **Database Operations:** Persists word history and tracks correct answer statistics for each user to monitor their progress.


## Architecture

The codebase follows a standard Layered Architecture:
* `controller` (`telegram`) — handles incoming updates and messages from users.
* `service` — contains the core logic and manages external API calls.
* `repository` — Spring Data interfaces for database interactions.
* `entity` —  database persistent models.  
*  `dto` — Data Transfer Objects (used for Telegram and Gemini API payloads).

