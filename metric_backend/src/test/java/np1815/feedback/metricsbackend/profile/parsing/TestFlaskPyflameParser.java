package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestFlaskPyflameParser {


    private static FlaskPyflameParser flaskPyflameParser;

    @BeforeAll
    public static void setUp() {
        flaskPyflameParser = new FlaskPyflameParser("/python/socketserver.py:process_request_thread:\\d*");
    }

    @Test
    public void testFlaskPyflameParser() {
        String input =
                "/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n";

        Profile profile = flaskPyflameParser.parseFlamegraph(input, "/");
        Assertions.assertEquals(37, profile.getTotalSamples());
        Assertions.assertEquals(4, profile.numberOfUniqueLines());
        Assertions.assertEquals(37, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assertions.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());
        Assertions.assertEquals(22, profile.getProfileForLine("app/my_app_main.py", 10).getNumberOfSamples());
    }
}
