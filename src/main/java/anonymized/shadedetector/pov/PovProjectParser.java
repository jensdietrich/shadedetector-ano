package anonymized.shadedetector.pov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import anonymized.shadedetector.cveverification.TestSignal;

import java.io.*;

/**
 * Parser for PovProjects from json.
 * @author jens dietrich
 */
public class PovProjectParser {

    static class TestSignalAdapter extends TypeAdapter<TestSignal> {
        @Override
        public TestSignal read(JsonReader reader) throws IOException {
            return TestSignal.valueOf(reader.nextString().toUpperCase());
        }

        @Override
        public void write(JsonWriter jsonWriter, TestSignal testSignal) throws IOException {
            throw new UnsupportedOperationException("write not supported");
        }
    }

    public static PovProject parse (Reader json) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(TestSignal.class, new TestSignalAdapter());
        Gson gson = builder.create();
        return gson.fromJson(json,PovProject.class);
    }

    public static PovProject parse (File file) throws FileNotFoundException {
        return parse(new FileReader(file));
    }
}
