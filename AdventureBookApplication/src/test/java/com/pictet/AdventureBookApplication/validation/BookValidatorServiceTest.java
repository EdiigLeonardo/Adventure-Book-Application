package com.pictet.AdventureBookApplication.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookValidatorServiceTest {

    private BookValidatorService validator;

    @BeforeEach
    void setUp() {
        validator = new BookValidatorService();
    }

    // ── validate(Book) ────────────────────────────────────────────────────────

    @Test
    void validate_withValidBook_returnsNoErrors() {
        Book book = buildValidBook();

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validate_withNullBook_returnsError() {
        ValidationResult result = validator.validate(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("required"));
    }

    @Test
    void validate_withNoSections_returnsError() {
        Book book = new Book();
        book.setId("b1");
        book.setTitle("Empty");
        book.setSections(List.of());

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("at least one section"));
    }

    @Test
    void validate_missingBeginSection_returnsError() {
        Section end = buildSection("end", SectionType.END, List.of());

        Book book = new Book();
        book.setId("b1");
        book.setTitle("No Begin");
        book.setSections(List.of(end));

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("BEGIN"));
    }

    @Test
    void validate_missingEndSection_returnsError() {
        Section begin = buildSection("begin", SectionType.BEGIN, List.of());

        Book book = new Book();
        book.setId("b1");
        book.setTitle("No End");
        book.setSections(List.of(begin));

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("END"));
    }

    @Test
    void validate_optionPointsToNonExistentSection_returnsError() {
        Option badOpt = buildOption("ghost-section");
        Section begin = buildSection("begin", SectionType.BEGIN, List.of(badOpt));
        Section end = buildSection("end", SectionType.END, List.of());

        Book book = new Book();
        book.setId("b1");
        book.setTitle("Bad Reference");
        book.setSections(List.of(begin, end));

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("ghost-section"));
    }

    @Test
    void validate_unreachableSection_addsWarning() {
        Option opt = buildOption("end");
        Section begin = buildSection("begin", SectionType.BEGIN, List.of(opt));
        Section end = buildSection("end", SectionType.END, List.of());
        Section orphan = buildSection("orphan", SectionType.NODE, List.of());
        orphan.setOptions(List.of()); // no outbound edges

        Book book = new Book();
        book.setId("b1");
        book.setTitle("With Orphan");
        book.setSections(List.of(begin, end, orphan));

        ValidationResult result = validator.validate(book);

        // The orphan itself has no options so also an error – but the warning must be present
        assertThat(result.getWarnings()).anyMatch(w -> w.contains("orphan"));
    }

    @Test
    void validate_reachableNodeWithNoOptions_returnsError() {
        Option opt = buildOption("dead-end-node");
        Section begin = buildSection("begin", SectionType.BEGIN, List.of(opt));

        // NODE with no options — not an END type
        Section node = new Section();
        node.setId("dead-end-node");
        node.setType(SectionType.NODE);
        node.setText("Stuck here");
        node.setOptions(List.of());

        Book book = new Book();
        book.setId("b1");
        book.setTitle("Dead End");
        book.setSections(List.of(begin, node));

        ValidationResult result = validator.validate(book);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("dead-end-node"));
    }

    @Test
    void validate_multipleSections_sectionWithoutId_returnsError() {
        Section noId = new Section();
        noId.setType(SectionType.NODE);
        noId.setText("Missing id");

        Option opt = buildOption("end");
        Section begin = buildSection("begin", SectionType.BEGIN, List.of(opt));
        Section end = buildSection("end", SectionType.END, List.of());

        Book book = new Book();
        book.setId("b1");
        book.setTitle("Bad Section");
        book.setSections(List.of(begin, end, noId));

        ValidationResult result = validator.validate(book);

        assertThat(result.getErrors()).anyMatch(e -> e.contains("missing a valid identifier"));
    }

    // ── validateRawJson(String) ───────────────────────────────────────────────

    @Test
    void validateRawJson_validJson_returnsNoErrors() {
        String json = """
            {
              "id": "test-book",
              "title": "Test",
              "sections": [
                {"id":"1","type":"BEGIN","text":"Start","options":[{"description":"Go","gotoId":"2"}]},
                {"id":"2","type":"END","text":"End"}
              ]
            }
            """;

        ValidationResult result = validator.validateRawJson(json);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateRawJson_malformedJson_returnsError() {
        String json = "{ this is not valid json }";

        ValidationResult result = validator.validateRawJson(json);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.toLowerCase().contains("invalid json"));
    }

    @Test
    void validateRawJson_emptyString_returnsError() {
        ValidationResult result = validator.validateRawJson("");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("empty"));
    }

    @Test
    void validateRawJson_nullString_returnsError() {
        ValidationResult result = validator.validateRawJson(null);

        assertThat(result.isValid()).isFalse();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Book buildValidBook() {
        Option opt = buildOption("end");
        Section begin = buildSection("begin", SectionType.BEGIN, List.of(opt));
        Section end = buildSection("end", SectionType.END, List.of());

        Book book = new Book();
        book.setId("valid-book");
        book.setTitle("Valid Adventure");
        book.setSections(List.of(begin, end));
        return book;
    }

    private Section buildSection(String id, SectionType type, List<Option> options) {
        Section s = new Section();
        s.setId(id);
        s.setType(type);
        s.setText("Text for " + id);
        s.setOptions(options);
        return s;
    }

    private Option buildOption(String gotoId) {
        Option opt = new Option();
        opt.setGotoId(gotoId);
        opt.setDescription("Go to " + gotoId);
        return opt;
    }
}
