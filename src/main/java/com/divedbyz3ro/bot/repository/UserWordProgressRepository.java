package com.divedbyz3ro.bot.repository;

import com.divedbyz3ro.bot.entity.User;
import com.divedbyz3ro.bot.entity.UserWordProgress;
import com.divedbyz3ro.bot.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserAndWord(User user, Word word);
    // Считает, сколько слов у этого пользователя имеют isLearned = true
    long countByUserAndIsLearnedTrue(User user);
}