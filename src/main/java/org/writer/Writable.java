package org.writer;

import java.util.List;

/**
 * Интерфейс для записи данных в файл.
 * <p>
 * Определяет контракт для сериализации коллекции объектов в файловое хранилище.
 * Реализации могут преобразовывать данные в различные форматы (CSV, JSON, XML и др.).
 *
 * <p>Пример использования:
 * <pre>{@code
 * Writable csvWriter = new CsvWriter();
 * csvWriter.writeToFile(data, "output.csv");
 * }</pre>
 */
public interface Writable {

    /**
     * Сериализует и сохраняет коллекцию объектов в файл
     *
     * @param data     список объектов для сохранения (может быть пустым)
     * @param fileName имя целевого файла (включая расширение)
     * @implSpec Реализации должны:
     * <ul>
     *   <li>Создавать новый файл или перезаписывать существующий</li>
     *   <li>Обрабатывать null-значения в данных</li>
     *   <li>Бросать {@link IllegalArgumentException} при несовместимых данных</li>
     * </ul>
     */
    void writeToFile(List<?> data, String fileName);

}
