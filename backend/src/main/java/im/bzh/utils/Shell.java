package im.bzh.utils;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class Shell {
    public static String exec(String command, String extra) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);

        InputStream in = process.getInputStream();
        OutputStream out = process.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        if (extra != null) {
            writer.write(extra + "\n");
            writer.flush();
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}
