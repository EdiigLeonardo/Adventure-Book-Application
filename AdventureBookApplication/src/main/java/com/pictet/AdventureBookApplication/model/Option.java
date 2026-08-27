package com.pictet.AdventureBookApplication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pictet.AdventureBookApplication.json.FlexibleIdDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Option {

    @JsonProperty("id")
    private String id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("description")
    private String description;

    @JsonProperty("gotoId")
    @JsonDeserialize(using = FlexibleIdDeserializer.class)
    private String gotoId;

    @JsonProperty("consequence")
    private Consequence consequence;

    public Option(String gotoId, String text, Consequence consequence) {
        this.gotoId = gotoId;
        this.text = text;
        this.consequence = consequence;
    }
}
