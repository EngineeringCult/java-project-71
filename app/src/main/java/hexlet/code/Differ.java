package hexlet.code;

import java.nio.file.Path;
import java.util.Map;
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
        System.out.println(map1.toString());
        System.out.println(map2.toString());
        return 0;
    }

    private Map<String, String> getData(String filePath) {
        return MAPPER.readValue(Path.of(filePath), new TypeReference<>() {
        });
    }
}
