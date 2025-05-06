package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.CSV;

@Data
@Builder
@AllArgsConstructor
public class Person {

    @CSV(order = 1, headerField = "First name")
    private String firstName;

    @CSV(order = 2, headerField = "Last name")
    private String lastName;

    @CSV(order = 3, headerField = "Day of birth")
    private int dayOfBirth;

    @CSV(order = 4, headerField = "Month of birth")
    private Months monthOfBirth;

    @CSV(order = 5, headerField = "Year of birth")
    private int yearOfBirth;

}
