package org.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса {@link Writable} для записи объектов в CSV-файлы с использованием Reflection.
 * <p>
 * Класс обеспечивает:
 * <ul>
 *   <li>Автоматическое определение структуры данных через аннотацию {@link CSV}</li>
 *   <li>Корректное экранирование специальных символов</li>
 *   <li>Поддержку коллекций и enum-типов</li>
 * </ul>
 *
 * @see CSV
 */
public class WriterCSV implements Writable {


    /**
     * Записывает список объектов в CSV-файл.
     * <p>
     * Особенности:
     * <ul>
     *   <li>Игнорирует null и пустые коллекции</li>
     *   <li>Автоматически определяет структуру через аннотацию {@link CSV}</li>
     * </ul>
     *
     * @param data     список объектов одного типа
     * @param fileName имя целевого файла (например, "data.csv")
     * @throws IllegalArgumentException если аргументы некорректны
     */
    @Override
    public void writeToFile(List<?> data, String fileName) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data should not be null or empty");
        }

        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name should not be null or empty");
        }

        Class<?> clazz = data.get(0).getClass();

        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {


            List<Field> fields = getAnnotatedFields(clazz);
            writeHeader(writer, fields);
            for (Object obj : data) {
                writeRow(writer, fields, obj);
            }
        } catch (IOException | IllegalAccessException e) {
            System.err.println("Error while writing data to file " + fileName);
            System.err.println("Message: " + e.getMessage());
        }
    }

    /**
     * Возвращает аннотированные поля класса, отсортированные по порядку.
     *
     * @param clazz анализируемый класс
     * @return список полей с аннотацией {@link CSV}, отсортированный по order()
     */
    List<Field> getAnnotatedFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CSV.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(CSV.class).order()))
                .collect(Collectors.toList());
    }

    /**
     * Генерирует строку заголовка CSV на основе аннотаций полей.
     *
     * @param writer объект для записи
     * @param fields список аннотированных полей
     */
    private void writeHeader(PrintWriter writer, List<Field> fields) {
        String headerLine = fields.stream()
                .map(f -> {
                    String header = f.getAnnotation(CSV.class).headerField();
                    return header.isEmpty() ? f.getName() : header;
                })
                .map(this::escapeCsv)
                .collect(Collectors.joining(","));
        writer.println(headerLine);
    }

    /**
     * Преобразует объект в строку CSV.
     *
     * @param writer объект для записи
     * @param fields список аннотированных полей
     * @param obj    объект для сериализации
     * @throws IllegalAccessException при ошибке доступа к полю
     */
    private void writeRow(PrintWriter writer, List<Field> fields, Object obj) throws IllegalAccessException {
        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            values.add(convertValue(value));
        }
        writer.println(String.join(",", values));
    }

    /**
     * Конвертирует значение поля в строку.
     * <p>
     * Правила преобразования:
     * <ul>
     *   <li>null → пустая строка</li>
     *   <li>Все коллекции и массивы → элементы через ";"</li>
     *   <li>Enum → имя константы</li>
     *   <li>Map → key:value;</li>
     *   <li>Остальные типы → toString()</li>
     * </ul>
     *
     * @param value исходное значение
     * @return CSV-совместимая строка
     */
    private String convertValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).entrySet().stream()
                    .map(entry ->
                            escapeCsv(entry.getKey().toString()) + ":" +
                            escapeCsv(entry.getValue().toString())
                    )
                    .collect(Collectors.joining(";"));
        }

        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).stream()
                    .map(this::convertValue)
                    .collect(Collectors.joining(";"));
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> elements = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                elements.add(Array.get(value, i));
            }
            return elements.stream()
                    .map(this::convertValue)
                    .collect(Collectors.joining(";"));
        }

        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        return escapeCsv(value.toString());
    }

    /**
     * Экранирует специальные символы CSV.
     * <p>
     * Условия добавления кавычек:
     * <ul>
     *   <li>Содержит запятые, кавычки или переносы строк</li>
     *   <li>Содержит числа с разделителями (например, "1,234")</li>
     * </ul>
     *
     * @param value исходная строка
     * @return экранированная строка
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuotes = value.contains(",")
                              || value.contains("\"")
                              || value.contains("\n")
                              || value.matches(".*\\d+[,.]\\d+.*");

        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
