package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.writer.CSV;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    @CSV(order = 1, headerField = "Name")
    private String name;

    @CSV(order = 2, headerField = "Scores")
    private List<String> score;
}