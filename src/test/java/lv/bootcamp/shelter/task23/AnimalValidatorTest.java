package lv.bootcamp.shelter.task23;

import lv.bootcamp.shelter.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AnimalValidator")
class AnimalValidatorTest {

    private AnimalValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AnimalValidator();
    }

    @Nested
    @DisplayName("validateName")
    class ValidateName {

        @ParameterizedTest
        @ValueSource(strings = {"Buddy", "Luna", "Mr. Whiskers", "X"})
        @DisplayName("accepts valid names")
        void shouldAcceptValidNames(String name) {
            assertDoesNotThrow(() -> validator.validateName(name));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("rejects blank or null names")
        void shouldRejectBlankNames(String name) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateName(name)
            );

            assertThat(ex.getMessage()).contains("must not be blank");
        }

        @Test
        @DisplayName("rejects name longer than 100 characters")
        void shouldRejectOverlyLongName() {
            String tooLongName = "A".repeat(101);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateName(tooLongName)
            );

            assertThat(ex.getMessage()).contains("100 characters");
        }
    }

    @Nested
    @DisplayName("validateAge")
    class ValidateAge {

        @ParameterizedTest
        @CsvSource({
                "0",
                "1",
                "10",
                "50"
        })
        @DisplayName("accepts valid ages")
        void shouldAcceptValidAges(int age) {
            assertDoesNotThrow(() -> validator.validateAge(age));
        }

        @ParameterizedTest
        @CsvSource({
                "-1, must not be negative",
                "-100, must not be negative",
                "51, seems unrealistic",
                "999, seems unrealistic"
        })
        @DisplayName("rejects invalid ages with correct message")
        void shouldRejectInvalidAges(int age, String expectedMessagePart) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> validator.validateAge(age)
            );

            assertThat(ex.getMessage()).contains(expectedMessagePart);
        }
    }

    // ==================== Task 3: Exception tests ====================

    @Nested
    @DisplayName("validate (full animal)")
    class ValidateFullAnimal {

        @Test
        @DisplayName("throws NullPointerException for null animal")
        void shouldThrowForNullAnimal() {
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("throws for animal with blank name")
        void shouldThrowForBlankName() {
            Animal animal = new Animal("", "Dog", 3, true, LocalDate.now());

            assertThatThrownBy(() -> validator.validate(animal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name must not be blank");
        }

        @Test
        @DisplayName("throws for animal with blank species")
        void shouldThrowForBlankSpecies() {
            Animal animal = new Animal("Buddy", "", 3, true, LocalDate.now());

            assertThatThrownBy(() -> validator.validate(animal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Species must not be blank");
        }

        @Test
        @DisplayName("throws for animal with negative age")
        void shouldThrowForNegativeAge() {
            Animal animal = new Animal("Buddy", "Dog", -1, true, LocalDate.now());

            assertThatThrownBy(() -> validator.validate(animal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("does not throw for fully valid animal")
        void shouldPassForValidAnimal() {
            Animal animal = new Animal("Buddy", "Dog", 3, true, LocalDate.now());

            assertDoesNotThrow(() -> validator.validate(animal));
        }
    }
}
