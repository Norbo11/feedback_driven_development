package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;
import np1815.feedback.metricsbackend.profile.ProfiledLine;
import np1815.feedback.metricsbackend.profile.ProfiledLineKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestRawPyflameParser {


    private static RawPyflameParser rawPyflameParser;

    @BeforeAll
    public static void setUp() {
        rawPyflameParser = new RawPyflameParser();
    }

    @Test
    public void regularInput() {
        String input =
            "/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n";

        Profile profile = rawPyflameParser.parseFlamegraph(input, "/");
        Assertions.assertEquals(47, profile.getTotalSamples());
        Assertions.assertEquals(6, profile.numberOfUniqueLines());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/main.py", 1).getNumberOfSamples());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/function.py", 5).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assertions.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());
        Assertions.assertEquals(22, profile.getProfileForLine("app/my_app_main.py", 10).getNumberOfSamples());

        // Because two unique invocations of lines in my_app_main
        Assertions.assertEquals(2, profile.getAllLineProfilesRegex("my_app_main").size());

        // Because /python/threading.py:run:864 and /python/socketserver.py:process_request_thread:639 both appear twice with the same parents, so two unique
        // invocations
        Assertions.assertEquals(2, profile.getAllLineProfilesRegex("python").size());
    }

    @Test
    public void inputWithIrregularPath() {
        String input =
            "<frozen importlib._bootstrap>:run:864;/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n";

        Profile profile = rawPyflameParser.parseFlamegraph(input, "/");
        Assertions.assertEquals(47, profile.getTotalSamples());
        Assertions.assertEquals(6, profile.numberOfUniqueLines());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/main.py", 1).getNumberOfSamples());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/function.py", 5).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assertions.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());
        Assertions.assertEquals(22, profile.getProfileForLine("app/my_app_main.py", 10).getNumberOfSamples());

        // Because two unique invocations of lines in my_app_main
        Assertions.assertEquals(2, profile.getAllLineProfilesRegex("my_app_main").size());

        // Because /python/threading.py:run:864 and /python/socketserver.py:process_request_thread:639 both appear twice with the same parents, so two unique
        // invocations
        Assertions.assertEquals(2, profile.getAllLineProfilesRegex("python").size());
    }

    @Test
    public void inputWithMultipleCallers() {
        String input =
            "/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n" +
            "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:641;/app/my_app_main.py:function:10 28\n";

        Profile profile = rawPyflameParser.parseFlamegraph(input, "/");
        Assertions.assertEquals(75, profile.getTotalSamples());
        Assertions.assertEquals(8, profile.numberOfUniqueLines());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/main.py", 1).getNumberOfSamples());
        Assertions.assertEquals(10, profile.getProfileForLine("random_module/function.py", 5).getNumberOfSamples());
        Assertions.assertEquals(65, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assertions.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assertions.assertEquals(28, profile.getProfileForLine("python/socketserver.py", 641).getNumberOfSamples());
        Assertions.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());

        // There is more than one unique chain of execution of app/my_app_main.py - one through prq:639 and another through prq:641
        Assertions.assertThrows(Profile.MoreThanOneProfileException.class, () ->
            profile.getProfileForLine("app/my_app_main.py", 10)
        );

        ProfiledLine one = profile.getProfileForLine("app/my_app_main.py", 10,
            new ProfiledLineKey("python/socketserver.py", 639,
                new ProfiledLineKey("python/threading.py", 864, null)
            )
        );

        ProfiledLine two = profile.getProfileForLine("app/my_app_main.py", 10,
            new ProfiledLineKey("python/socketserver.py", 641,
                new ProfiledLineKey("python/threading.py", 864, null)
            )
        );

        Assertions.assertEquals(22, one.getNumberOfSamples());
        Assertions.assertEquals(28, two.getNumberOfSamples());
    }
}
