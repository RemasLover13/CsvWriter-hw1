package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.CSV;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class Sample {

    @CSV(order = 1, headerField = "strings")
    private String[] strings;

    @CSV(order = 2, headerField = "doubles")
    private Double[] doubles;

    @CSV(order = 3, headerField = "set")
    private Set<Integer> set;

    @CSV(order = 4, headerField = "map")
    private Map<String, String> map;

}
