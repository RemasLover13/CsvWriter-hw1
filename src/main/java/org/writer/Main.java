package org.writer;

import net.datafaker.Faker;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Sample;
import org.writer.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        Faker faker = new Faker();
        List<Student> studentsFaker;
        Months[] months = Months.values();

        List<String> scoresFaker = IntStream.range(0, 11)
                .mapToObj(i -> String.valueOf(ThreadLocalRandom.current().nextInt(5)))
                .toList();

        List<Person> personsFaker = IntStream.range(0, 100)
                .mapToObj(i -> new Person(
                        faker.name().firstName(),
                        faker.name().lastName(),
                        faker.number().numberBetween(1, 31),
                        months[ThreadLocalRandom.current().nextInt(months.length)],
                        faker.number().numberBetween(1900, 2025)
                ))
                .toList();

        studentsFaker = IntStream.range(0, 100)
                .mapToObj(i -> new Student(
                        faker.name().fullName(),
                        new ArrayList<>(scoresFaker)
                ))
                .toList();

        List<Sample> samplesFaker = IntStream.range(0, 5)
                .mapToObj(i -> {
                    String[] strings = IntStream.range(0, 5)
                            .mapToObj(j -> faker.studioGhibli()
                                    .character())
                            .toArray(String[]::new);

                    Double[] doubles = IntStream.range(0, 5)
                            .mapToObj(j -> faker.number().randomDouble(1, 1, 500))
                            .toArray(Double[]::new);

                    Set<Integer> integerSet = IntStream.range(0, 5)
                            .map(j -> faker.number().numberBetween(100, 999))
                            .boxed()
                            .collect(Collectors.toSet());

                    Map<String, String> map = IntStream.range(0, 5)
                            .boxed()
                            .collect(Collectors.toMap(
                                    k -> "key_" + faker.name().firstName(),
                                    v -> "value_" + faker.code().asin()
                            ));

                    return new Sample(strings, doubles, integerSet, map);
                })
                .toList();

        new WriterCSV().writeToFile(personsFaker, "persons.csv");
        new WriterCSV().writeToFile(studentsFaker, "students.csv");
        new WriterCSV().writeToFile(samplesFaker, "samples.csv");
    }
}