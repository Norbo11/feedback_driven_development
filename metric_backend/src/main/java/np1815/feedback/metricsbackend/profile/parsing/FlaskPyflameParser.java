package np1815.feedback.metricsbackend.profile.parsing;

import com.google.common.base.Splitter;
import np1815.feedback.metricsbackend.profile.Profile;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlaskPyflameParser implements PyflameParser {

    //TODO: Make paths relative to project
    private static final String DEFUALT_ROOT_STRING = "/usr/local/lib/python3.6/socketserver.py:process_request_thread:\\d*";

    private final RawPyflameParser rawPyflameParser;
    private final Pattern rootPattern;

    public FlaskPyflameParser() {
        this(DEFUALT_ROOT_STRING);
    }

    public FlaskPyflameParser(String rootPattern) {
        this.rawPyflameParser = new RawPyflameParser();
        this.rootPattern = Pattern.compile(rootPattern);
    }

    @Override
    public Profile parseFlamegraph(String input, String basePath) {
        List<String> lines = Splitter.on("\n").trimResults().omitEmptyStrings().splitToList(input);
        input = lines.stream().filter(rootPattern.asPredicate()).collect(Collectors.joining("\n"));
        return rawPyflameParser.parseFlamegraph(input, basePath);
    }
}