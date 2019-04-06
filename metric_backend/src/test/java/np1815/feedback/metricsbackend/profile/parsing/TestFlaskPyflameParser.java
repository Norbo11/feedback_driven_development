package np1815.feedback.metricsbackend.profile.parsing;

import np1815.feedback.metricsbackend.profile.Profile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFlaskPyflameParser {


    private FlaskPyflameParser flaskPyflameParser;

    @Before
    public void setUp() {
        this.flaskPyflameParser = new FlaskPyflameParser("/python/socketserver.py:process_request_thread:\\d*");
    }

    @Test
    public void testFlaskPyflameParser() {
        String input =
                "/random_module/main.py:main:1;/random_module/function.py:function:5 10\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:main:4 15\n" +
                "/python/threading.py:run:864;/python/socketserver.py:process_request_thread:639;/app/my_app_main.py:function:10 22\n";

        Profile profile = flaskPyflameParser.parseFlamegraph(input, "/");
        Assert.assertEquals(37, profile.getTotalSamples());
        Assert.assertEquals(4, profile.numberOfUniqueLines());
        Assert.assertEquals(37, profile.getProfileForLine("python/threading.py", 864).getNumberOfSamples());
        Assert.assertEquals(37, profile.getProfileForLine("python/socketserver.py", 639).getNumberOfSamples());
        Assert.assertEquals(15, profile.getProfileForLine("app/my_app_main.py", 4).getNumberOfSamples());
        Assert.assertEquals(22, profile.getProfileForLine("app/my_app_main.py", 10).getNumberOfSamples());
    }
}
