package com.pictet.AdventureBookApplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pictet.AdventureBookApplication.model.Book;
import com.pictet.AdventureBookApplication.model.BookStatus;
import com.pictet.AdventureBookApplication.model.Difficulty;
import com.pictet.AdventureBookApplication.model.Option;
import com.pictet.AdventureBookApplication.model.Section;
import com.pictet.AdventureBookApplication.model.SectionType;
import com.pictet.AdventureBookApplication.validation.BookValidatorService;
import com.pictet.AdventureBookApplication.validation.ValidationResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookCatalogServiceTest {

    @Mock
    private BookValidatorService validatorService;

    @InjectMocks
    private BookCatalogService catalogService;

    private Book bookA;
    private Book bookB;

    @BeforeEach
    void setUp() {
        // Prevent loadDefaultBooks() from calling the real validatorService
        // by ensuring the mock handles any validate() call
        ValidationResult valid = new ValidationResult();
        when(validatorService.validate(any())).thenReturn(valid);

        // Recreate service after mock is set (constructor triggers loadDefaultBooks)
        catalogService = new BookCatalogService(validatorService);

        bookA = buildBook("book-a", "Pirates", "MEDIUM", BookStatus.VALID);
        bookB = buildBook("book-b", "Garden", "EASY", BookStatus.VALID);

        ValidationResult validResult = new ValidationResult();
        when(validatorService.validate(any())).thenReturn(validResult);

        catalogService.saveUploadedBook(bookA);
        catalogService.saveUploadedBook(bookB);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_withNoFilters_returnsAll() {
        List<Book> result = catalogService.findAll(null, null, null);

        // May contain seeded books from classpath + our two
        assertThat(result).extracting(Book::getId)
            .contains("book-a", "book-b");
    }

    @Test
    void findAll_filterByQueryMatchingTitle_returnsMatchingBooks() {
        List<Book> result = catalogService.findAll("Pirates", null, null);

        assertThat(result).extracting(Book::getId).contains("book-a");
        assertThat(result).extracting(Book::getId).doesNotContain("book-b");
    }

    @Test
    void findAll_filterByQuery_caseInsensitive() {
        List<Book> result = catalogService.findAll("pirates", null, null);

        assertThat(result).extracting(Book::getId).contains("book-a");
    }

    @Test
    void findAll_filterByDifficulty_returnsOnlyMatchingNormalizedDifficulty() {
        List<Book> resultIntermediate = catalogService.findAll(null, "INTERMEDIATE", null);
        assertThat(resultIntermediate).extracting(Book::getId).contains("book-a");

        List<Book> resultBeginner = catalogService.findAll(null, "BEGINNER", null);
        assertThat(resultBeginner).extracting(Book::getId).contains("book-b");
    }

    @Test
    void normalizeDifficulty_mapsPictetEasyMediumHard_toCorrectBuckets() {
        assertThat(Difficulty.normalize("EASY")).isEqualTo(Difficulty.BEGINNER);
        assertThat(Difficulty.normalize("MEDIUM")).isEqualTo(Difficulty.INTERMEDIATE);
        assertThat(Difficulty.normalize("HARD")).isEqualTo(Difficulty.ADVANCED);
    }

    @Test
    void normalizeDifficulty_unknownValue_throwsInsteadOfDefaultingSilently() {
        assertThatThrownBy(() -> Difficulty.normalize("NIGHTMARE"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"crystal-caverns", "pirates-jade-sea", "the-prisoner"})
    void loadDefaultBooks_pictetSampleFiles_areLoadedWithDerivedId(String derivedId) {
        Book book = catalogService.findById(derivedId);
        assertThat(book).isNotNull();
        assertThat(book.getId()).isEqualTo(derivedId);
    }

    @Test
    void loadDefaultBooks_malformedJsonFile_doesNotPreventApplicationStartup() {
        assertThat(catalogService.findAll(null, null, null)).isNotEmpty();
    }

    @Test
    void findAll_filterByStatus_returnsFiltered() {
        List<Book> result = catalogService.findAll(null, null, "VALID");

        assertThat(result).extracting(Book::getId).contains("book-a", "book-b");
    }

    @Test
    void findAll_filterByUnknownDifficulty_returnsEmpty() {
        List<Book> result = catalogService.findAll(null, "EXTREME", null);

        assertThat(result).extracting(Book::getId)
            .doesNotContain("book-a", "book-b");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_existingBook_returnsBook() {
        Book result = catalogService.findById("book-a");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Pirates");
    }

    @Test
    void findById_unknownId_returnsNull() {
        Book result = catalogService.findById("does-not-exist");

        assertThat(result).isNull();
    }

    @Test
    void findById_nullId_returnsNull() {
        Book result = catalogService.findById(null);

        assertThat(result).isNull();
    }

    // ── saveUploadedBook ──────────────────────────────────────────────────────

    @Test
    void saveUploadedBook_validBook_savesAndReturnsBook() {
        Book newBook = buildBook("book-new", "New Adventure", "HARD", null);
        ValidationResult valid = new ValidationResult();
        when(validatorService.validate(newBook)).thenReturn(valid);

        Book saved = catalogService.saveUploadedBook(newBook);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("book-new");
        assertThat(saved.getDifficulty()).isEqualTo("ADVANCED");
        assertThat(saved.getStatus()).isEqualTo(BookStatus.VALID);
        assertThat(catalogService.findById("book-new")).isNotNull();
    }

    @Test
    void saveUploadedBookIdempotently_returnsExistingBookForAnIdenticalUpload() {
        Book firstUpload = buildBook("duplicate-book", "Duplicate", "EASY", null);
        Book repeatedUpload = buildBook("duplicate-book", "Duplicate", "EASY", null);

        BookCatalogService.UploadResult first = catalogService.saveUploadedBookIdempotently(firstUpload);
        BookCatalogService.UploadResult repeated = catalogService.saveUploadedBookIdempotently(repeatedUpload);

        assertThat(first.created()).isTrue();
        assertThat(repeated.created()).isFalse();
        assertThat(repeated.book()).isSameAs(first.book());
        assertThat(catalogService.findAll("Duplicate", null, null)).hasSize(1);
    }

    @Test
    void saveUploadedBook_invalidBook_throwsIllegalArgumentException() {
        Book invalidBook = buildBook("bad-book", "Invalid", "HARD", null);
        ValidationResult invalid = new ValidationResult();
        invalid.addError("Missing BEGIN section.");
        when(validatorService.validate(invalidBook)).thenReturn(invalid);

        assertThatThrownBy(() -> catalogService.saveUploadedBook(invalidBook))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Missing BEGIN");
    }

    @Test
    void saveUploadedBook_missingId_throwsIllegalArgumentException() {
        Book invalidBook = buildBook(null, "No ID", "HARD", null);

        assertThatThrownBy(() -> catalogService.saveUploadedBook(invalidBook))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("id' is required");
    }

    @Test
    void saveUploadedBook_overwritesExistingBookWithSameId() {
        Book updated = buildBook("book-a", "Pirates Updated", "HARD", null);
        ValidationResult valid = new ValidationResult();
        when(validatorService.validate(updated)).thenReturn(valid);

        catalogService.saveUploadedBook(updated);

        assertThat(catalogService.findById("book-a").getTitle()).isEqualTo("Pirates Updated");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Book buildBook(String id, String title, String difficulty, BookStatus status) {
        Option opt = new Option();
        opt.setGotoId("end");
        opt.setDescription("Proceed");

        Section begin = new Section();
        begin.setId("begin");
        begin.setType(SectionType.BEGIN);
        begin.setText("Start");
        begin.setOptions(List.of(opt));

        Section end = new Section();
        end.setId("end");
        end.setType(SectionType.END);
        end.setText("The end");

        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setDifficulty(difficulty);
        book.setStatus(status);
        book.setSections(List.of(begin, end));
        return book;
    }
}
