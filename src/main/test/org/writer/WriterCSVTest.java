package org.writer;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class WriterCSVTest {

    private final Faker faker = new Faker();
    private final WriterCSV csvWriter = new WriterCSV();

    @TempDir
    Path tempDir;

    Months[] months = Months.values();

    @Test
    void writeToFile_NormalData_WritesCorrectCSV() throws Exception {
        List<Person> persons = IntStream.range(0, 30)
                .mapToObj(i -> Person.builder()
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .dayOfBirth(faker.number().numberBetween(1, 31))
                        .monthOfBirth(months[ThreadLocalRandom.current().nextInt(months.length)])
                        .yearOfBirth(faker.number().numberBetween(1900, 2023))
                        .build())
                .toList();

        Path file = tempDir.resolve("persons.csv");
        csvWriter.writeToFile(persons, file.toString());

        List<String> lines = Files.readAllLines(file);
        assertEquals(31, lines.size());

        String header = lines.get(0);
        assertTrue(header.contains("First name") && header.contains("Last name"));
    }

    @Test
    void escapeCsv_SpecialCharacters_EscapesCorrectly() throws Exception {
        Student student = Student.builder()
                .name(faker.name().fullName() + ",\"Test\"")
                .score(List.of("A,1", "B;2", "C\"3"))
                .build();

        Path file = tempDir.resolve("special.csv");
        csvWriter.writeToFile(List.of(student), file.toString());

        String content = Files.readString(file);
        assertTrue(content.contains("\"\""));
        assertTrue(content.contains("\"A,1\""));
    }

    @Test
    void getAnnotatedFields_OrderCorrect() {
        List<Field> fields = csvWriter.getAnnotatedFields(Person.class);

        System.out.println(fields);
        String[] expectedOrder = {"firstName", "lastName", "dayOfBirth", "monthOfBirth", "yearOfBirth"};        for (int i = 0; i < expectedOrder.length; i++) {
            assertEquals(expectedOrder[i], fields.get(i).getName());
        }
    }

    @Test
    void convertValue_NullHandling_ReturnsEmptyString() throws Exception {
        Person person = Person.builder()
                .firstName(null)
                .lastName("")
                .dayOfBirth(0)
                .monthOfBirth(null)
                .yearOfBirth(0)
                .build();

        Path file = tempDir.resolve("nulls.csv");
        csvWriter.writeToFile(List.of(person), file.toString());

        String line = Files.readAllLines(file).get(1);
        assertEquals(",,0,,0", line);
    }

    @Test
    void convertValue_ListHandling_JoinsWithSemicolon() throws Exception {
        Student student = Student.builder()
                .name("Test")
                .score(List.of("1", "5", "3"))
                .build();

        Path file = tempDir.resolve("list.csv");
        csvWriter.writeToFile(List.of(student), file.toString());

        String content = Files.readString(file);
        assertTrue(content.contains("Test,1;5;3"));
    }

    @Test
    void convertValue_EnumHandling_ReturnsName() throws Exception {
        Person person = Person.builder()
                .monthOfBirth(Months.JUNE)
                .build();

        Path file = tempDir.resolve("enum.csv");
        csvWriter.writeToFile(List.of(person), file.toString());

        String content = Files.readString(file);
        assertTrue(content.contains("JUNE"));
    }

    @Test
    void writeToFile_EmptyList_NoFileCreated() {
        Path file = tempDir.resolve("empty.csv");
        csvWriter.writeToFile(List.of(), file.toString());
        assertFalse(Files.exists(file));
    }
}