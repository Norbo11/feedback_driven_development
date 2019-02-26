package uk.ac.ic.doc.np1815;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestPyflameParser {


    private PyflameParser pyflameParser;

    @Before
    public void setUp() {
        pyflameParser = new PyflameParser();
    }

    @Test
    public void testPyflameParser() {
        String input = "/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:_bootstrap:884;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:_bootstrap_inner:916;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:run:864;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:process_request_thread:639;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:finish_request:361;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:__init__:696;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:handle:293;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/http/server.py:handle:418;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:handle_one_request:328;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:run_wsgi:270;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:execute:258;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:__call__:2309;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:wsgi_app:2292;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:full_dispatch_request:1816;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:finalize_request:1833;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:process_response:2112;/home/np1815/Individual-Project/playground_application/playground_application/__main__.py:pyflame_profile_end:60;/home/np1815/Individual-Project/playground_application/playground_application/__main__.py:kill_process:54;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/subprocess.py:communicate:843;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/subprocess.py:_communicate:1514;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/selectors.py:select:376 1\n" +
                "/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:_bootstrap:884;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:_bootstrap_inner:916;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/threading.py:run:864;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:process_request_thread:639;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:finish_request:361;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:__init__:696;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:handle:293;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/http/server.py:handle:418;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:handle_one_request:328;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:run_wsgi:270;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:execute:258;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:__call__:2309;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:wsgi_app:2292;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:full_dispatch_request:1813;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:dispatch_request:1799;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/connexion/decorators/decorator.py:wrapper:66;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/connexion/decorators/decorator.py:wrapper:42;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/connexion/decorators/parameter.py:wrapper:218;/home/np1815/Individual-Project/playground_application/playground_application/controllers/default_controller.py:two_get:46 158\n" +
                "/home/np1815/miniconda3/envs/playground_application/lib/python3.6/runpy.py:_run_module_as_main:193;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/runpy.py:_run_code:85;/home/np1815/Individual-Project/playground_application/playground_application/__main__.py:<module>:71;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/connexion/apps/flask_app.py:run:94;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/flask/app.py:run:943;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:run_simple:814;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:inner:777;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/site-packages/werkzeug/serving.py:serve_forever:612;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/socketserver.py:serve_forever:236;/home/np1815/miniconda3/envs/playground_application/lib/python3.6/selectors.py:select:376 159";

        ParsedPyflameProfile profile = pyflameParser.parseFlamegraph(input);
        assertEquals(50, profile.size());

        List<LineProfile> p1 = profile.getProfiles().get("threading.py");
        assertEquals(6, p1.size());

        LineProfile p11 = p1.get(0);
        assertEquals("threading.py", p11.getFilePath());
        assertEquals("_bootstrap", p11.getFunction());
        assertEquals(884, p11.getLineNumber());
        assertEquals(1, p11.getNumberOfSamples());

        LineProfile p12 = p1.get(1);
        assertEquals("threading.py", p12.getFilePath());
        assertEquals("_bootstrap_inner", p12.getFunction());
        assertEquals(916, p12.getLineNumber());
        assertEquals(1, p12.getNumberOfSamples());

        LineProfile p13 = p1.get(2);
        assertEquals("threading.py", p13.getFilePath());
        assertEquals("run", p13.getFunction());
        assertEquals(864, p13.getLineNumber());
        assertEquals(1, p13.getNumberOfSamples());

        List<LineProfile> p2 = profile.getProfiles().get("default_controller.py");
        assertEquals(1, p2.size());

        LineProfile p21 = p2.get(0);
        assertEquals("default_controller.py", p21.getFilePath());
        assertEquals("two_get", p21.getFunction());
        assertEquals(46, p21.getLineNumber());
        assertEquals(158, p21.getNumberOfSamples());


    }
}
