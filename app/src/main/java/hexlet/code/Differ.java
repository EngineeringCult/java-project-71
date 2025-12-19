package hexlet.code;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Command(name = "gendiff", mixinStandardHelpOptions = true, version = "gendiff 1.0",
        description = "Compares two configuration files and shows a difference.")
public class Differ implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String INDENT = "    ";
    private static final String INDENT_WITHOUT_SIGN = INDENT + "  ";
    private static final String COLON_SYMBOL = ": ";
    private static final String MINUS_SIGN = "- ";
    private static final String PLUS_SIGN = "+ ";

    @Parameters(index = "0", description = "path to first file")
    private String filePath1;

    @Parameters(index = "1", description = "path to second file")
    private String filePath2;

    @Option(
            names = {"-f", "--format"},
            description = "output format",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private String format = "stylish";

    @Override
    public Integer call() throws Exception {
        Map<String, String> map1 = getData(filePath1);
        Map<String, String> map2 = getData(filePath2);
        System.out.println(generate(map1, map2));
        return 0;
    }

    private static Map<String, String> getData(String filePath) {
        return MAPPER.readValue(Path.of(filePath), new TypeReference<SortedMap<String, String>>() {
        });
    }

    private static String generate(Map<String, String> map1, Map<String, String> map2) {
        StringBuffer result = new StringBuffer();

        Iterator<Map.Entry<String, String>> it1 = map1.entrySet().iterator();
        Iterator<Map.Entry<String, String>> it2 = map2.entrySet().iterator();

        Map.Entry<String, String> e1 = it1.hasNext() ? it1.next() : null;
        Map.Entry<String, String> e2 = it2.hasNext() ? it2.next() : null;

        result.append("{").append(System.lineSeparator());

        while (e1 != null || e2 != null) {

            if (e1 != null && e2 != null) {
                int cmp = e1.getKey().compareTo(e2.getKey());

                if (cmp == 0) {
                    // ключ есть в обоих
                    String key = e1.getKey();
                    String oldValue = e1.getValue();
                    String newValue = e2.getValue();

                    if (oldValue.equals(newValue)) {
                        addKeyValueWithoutChange(result, key, newValue);
                    } else {
                        addChangedKeyValue(result, key, oldValue, newValue);
                    }

                    e1 = it1.hasNext() ? it1.next() : null;
                    e2 = it2.hasNext() ? it2.next() : null;

                } else if (cmp < 0) {
                    // ключ удалён (есть только в map1)
                    addDeletedKeyValue(result, e1.getKey(), e1.getValue());

                    e1 = it1.hasNext() ? it1.next() : null;

                } else {
                    // ключ добавился (есть только в map2)
                    addNewKeyValue(result,e2.getKey(), e2.getValue());

                    e2 = it2.hasNext() ? it2.next() : null;
                }

            } else if (e1 != null) {
                // остались только удалённые
                addDeletedKeyValue(result, e1.getKey(), e1.getValue());

                e1 = it1.hasNext() ? it1.next() : null;

            } else {
                // остались только добавленные
                addNewKeyValue(result, e2.getKey(), e2.getValue());

                e2 = it2.hasNext() ? it2.next() : null;
            }
        }

        result.append("}");
        return result.toString();
    }

    private static void addChangedKeyValue(StringBuffer buffer, String key, String oldValue, String newValue) {
        addDeletedKeyValue(buffer, key, oldValue);
        addNewKeyValue(buffer, key, newValue);
    }

    private static void addNewKeyValue(StringBuffer buffer, String key, String value) {
        buffer.append(INDENT)
                .append(PLUS_SIGN).append(key)
                .append(COLON_SYMBOL).append(value)
                .append(System.lineSeparator());
    }

    private static void addDeletedKeyValue(StringBuffer buffer, String key, String value) {
        buffer.append(INDENT)
                .append(MINUS_SIGN).append(key)
                .append(COLON_SYMBOL).append(value)
                .append(System.lineSeparator());
    }

    private static void addKeyValueWithoutChange(StringBuffer buffer, String key, String newValue) {
        buffer.append(INDENT_WITHOUT_SIGN)
                .append(key).append(COLON_SYMBOL).append(newValue)
                .append(System.lineSeparator());
    }
}
