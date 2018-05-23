package com.crazyhands.dictionary.Api.Model;

import java.util.ArrayList;

public class POJ {

    private String version;
    private String author_url;
    private Words data;

    public String getVersion() {
        return version;
    }

    public String getAuthor_url() {
        return author_url;
    }

    public Words getWords() {
        return data;
    }

    public class Words {

        private int id;
        private String english;
        private String cantonese;
        private String jyutping;
        private String soundAddress;
        private Integer type;


        public Words(int id, String english, String cantonese, String jyutping, String soundAddress, Integer type) {
            this.id = id;
            this.english = english;
            this.cantonese = cantonese;
            this.jyutping = jyutping;
            this.soundAddress = soundAddress;
            this.type = type;
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
/*

{"data":
        {"id":1,"english":"Heart","jyutping":"sam","cantonese":"\u5fc3","soundAddress":"sam.3gp","type":1,"syncsts":0,"remember_token":null,"created_at":null,"updated_at":null
        },
        "version ":"1.0.0",
            "author_url":"brianstein.co.uk"
}
    */

}