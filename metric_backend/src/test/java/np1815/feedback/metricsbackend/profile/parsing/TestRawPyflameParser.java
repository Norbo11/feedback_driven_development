package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class TestRawPyflameParser {


    private RawPyflameParser rawPyflameParser;

    @Before
    public void setUp() {
        rawPyflameParser = new RawPyflameParser();
    }

    @Test
    public void testPyflameParser() {
        String input =
                "/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n";

        Profile profile = rawPyflameParser.parseFlamegraph(input, "/");
        Assert.assertEquals(47, profile.getTotalSamples());
        Assert.assertEquals(6, profile.numberOfUniqueLines());
        Assert.assertEquals(10, profile.getProfileForLine("random_module/main.py", 1).getNumberOfSamples());
        Assert.assertEquals(10, profile.getProfileForLine("random_module/function.py", 5).getNumberOfSamples());
        Assert.assertEquals(37, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assert.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assert.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());
        Assert.assertEquals(22, profile.getProfileForLine("app/my_app_main.py", 10).getNumberOfSamples());
        Assert.assertEquals(2, profile.getAllLineProfiles("my_app_main").size());
        Assert.assertEquals(2, profile.getAllLineProfiles("python").size());
    }

    @Test
    public void scratch() {
//        Path path = Paths.get("/app").relativize(Paths.get("/something/else"));
//        System.out.println(path.toString());
    }
}
