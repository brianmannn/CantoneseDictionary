package com.crazyhands.dictionary.Api.Model;

public class Word
{
    private int id;
    private String english;
    private String cantonese;
    private String jyutping;
    private String soundAddress;
    private Integer type;


    public Word(String english, String cantonese, String jyutping, String soundAddress, Integer type) {
        this.english = english;
        this.cantonese = cantonese;
        this.jyutping = jyutping;
        this.type = type;
        this.soundAddress = soundAddress;
    }
    public Word(int id, String english, String cantonese, String jyutping, String soundAddress, Integer type) {
        this.id=id;
        this.english = english;
        this.cantonese = cantonese;
        this.jyutping = jyutping;
        this.type = type;
        this.soundAddress = soundAddress;
    }

    public int getId() {
        return id;
    }

    public String getEnglish() {
        return english;
    }

    public String getCantonese() {
        return cantonese;
    }

    public String getJyutping() {
        return jyutping;
    }

    public String getSoundAddress() {
        return soundAddress;
    }

    public Integer getType() {
        return type;
    }
}
