package com.pictet.AdventureBookApplication.model;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Section {

    @JsonProperty("id")
    @JsonDeserialize(using = FlexibleIdDeserializer.class)
    private String id;

    @JsonProperty("type")
    private SectionType type;

    @JsonProperty("text")
    private String text;

    @JsonProperty("options")
    private List<Option> options = new ArrayList<>();

    public void setOptions(List<Option> options) {
        this.options = options == null ? new ArrayList<>() : options;
    }
}
