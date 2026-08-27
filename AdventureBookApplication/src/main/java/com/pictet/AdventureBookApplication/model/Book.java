package com.pictet.AdventureBookApplication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pictet.AdventureBookApplication.json.FlexibleIdDeserializer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {

    @JsonProperty("id")
    @JsonDeserialize(using = FlexibleIdDeserializer.class)
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private BookStatus status = BookStatus.VALID;

    @JsonProperty("sections")
    private List<Section> sections = new ArrayList<>();

    public void setSections(List<Section> sections) {
        this.sections = sections == null ? new ArrayList<>() : sections;
    }
}
