package com.divedbyz3ro.bot.repository;

import com.divedbyz3ro.bot.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByLevelAndLanguage(int level, String language); //объявление поиска слов по уровню сложности
    boolean existsByOriginalAndLanguage(String original, String language);
}
