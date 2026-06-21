package com.divedbyz3ro.bot.entity;

import jakarta.persistence.*;


@Entity //
@Table(name = "words")



public class Word {
    @Id             //каждому слову сделаем уникальный номер(primary key)
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String original; //слово - перевод
    private String translation;
    private int level=0;            //уровень сложности слова
    private String language;



    //getters, setters
    public String getOriginal() {
        return original;
    }
    public void setOriginal(String original) {
        this.original = original;
    }
    public String getTranslation() {
        return translation;
    }
    public void setTranslation(String translation) {
        this.translation = translation;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    public String getLanguage(){
        return language;
    }
    //

}

