package yqb.com.example.weatherdemo.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    public static void closeIO(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

}
