package com.pictet.AdventureBookApplication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Consequence {

    @JsonProperty("type")
    private ConsequenceType type;

    @JsonProperty("value")
    private Integer value;

    @JsonProperty("text")
    private String text;
}
