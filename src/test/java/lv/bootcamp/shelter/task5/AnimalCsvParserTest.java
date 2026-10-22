package lv.bootcamp.shelter.task5;

import lv.bootcamp.shelter.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AnimalCsvParser")
class AnimalCsvParserTest {

    private AnimalCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new AnimalCsvParser();
    }

    // ==================== parseRow tests ====================

    @Nested
    @DisplayName("When parsing valid rows")
    class ValidRows {

        @Test
        @DisplayName("parses a complete row into an Animal")
        void shouldParseCompleteRow() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,2026-01-15");

            assertThat(result).isPresent();
            Animal animal = result.get();
            assertThat(animal.getName()).isEqualTo("Buddy");
            assertThat(animal.getSpecies()).isEqualTo("Dog");
            assertThat(animal.getAge()).isEqualTo(3);
            assertThat(animal.isVaccinated()).isTrue();
            assertThat(animal.getIntakeDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        }

        @Test
        @DisplayName("trims whitespace from fields")
        void shouldTrimWhitespace() {
            Optional<Animal> result = parser.parseRow("  Buddy , Dog , 3 , true , 2026-01-15 ");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Buddy");
            assertThat(result.get().getSpecies()).isEqualTo("Dog");
        }

        @Test
        @DisplayName("parses vaccinated=false correctly")
        void shouldParseFalseVaccination() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,false,2026-01-15");

            assertThat(result).isPresent();
            assertThat(result.get().isVaccinated()).isFalse();
        }
    }

    @Nested
    @DisplayName("When parsing malformed rows")
    class MalformedRows {

        @Test
        @DisplayName("returns empty for null input")
        void shouldReturnEmptyForNull() {
            Optional<Animal> result = parser.parseRow(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty for blank input")
        void shouldReturnEmptyForBlank() {
            Optional<Animal> result = parser.parseRow("   ");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when row has fewer than 5 fields")
        void shouldReturnEmptyForTooFewFields() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when name is missing")
        void shouldReturnEmptyForMissingName() {
            Optional<Animal> result = parser.parseRow(",Dog,3,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is not a number")
        void shouldReturnEmptyForBadAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,old,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is negative")
        void shouldReturnEmptyForNegativeAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,-1,true,2026-01-15");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when date is invalid")
        void shouldReturnEmptyForBadDate() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,not-a-date");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("handles vaccinated field as any non-true string → false")
        void shouldTreatNonTrueAsFalse() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,maybe,2026-01-15");

            assertThat(result).isPresent();
            assertThat(result.get().isVaccinated()).isFalse();
        }

        @Test
        @DisplayName("handles age 0 as valid")
        void shouldAcceptAgeZero() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,0,true,2026-01-15");

            assertThat(result).isPresent();
            assertThat(result.get().getAge()).isZero();
        }
    }

    // ==================== parseFile tests ====================

    @Nested
    @DisplayName("When parsing a CSV file")
    class ParseFile {

        @Test
        @DisplayName("parses valid rows and counts skipped rows")
        void shouldParseFileAndCountSkipped() throws IOException {
            String content = String.join("\n",
                    "name,species,age,vaccinated,intakeDate",
                    "Buddy,Dog,3,true,2026-01-15",
                    "Luna,Cat,2,true,2026-01-16",
                    "Max,Dog,5,false,2026-01-18",
                    "Simba,Cat,-1,true,2026-02-08");
            Path tempFile = Files.createTempFile("test-intake", ".csv");
            try {
                Files.writeString(tempFile, content, StandardCharsets.UTF_8);

                AnimalCsvParser.ParseResult result = parser.parseFile(tempFile);

                assertThat(result.animals()).hasSize(3);
                assertThat(result.skippedRows()).isEqualTo(1);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("returns empty result for file with only a header")
        void shouldReturnEmptyForHeaderOnly() throws IOException {
            Path tempFile = Files.createTempFile("test-intake", ".csv");
            try {
                Files.writeString(tempFile, "name,species,age,vaccinated,intakeDate", StandardCharsets.UTF_8);

                AnimalCsvParser.ParseResult result = parser.parseFile(tempFile);

                assertThat(result.animals()).isEmpty();
                assertThat(result.skippedRows()).isZero();
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("throws IOException for non-existent file")
        void shouldThrowForMissingFile() {
            Path missing = Path.of("does-not-exist.csv");

            assertThrows(IOException.class, () -> parser.parseFile(missing));
        }
    }
}
