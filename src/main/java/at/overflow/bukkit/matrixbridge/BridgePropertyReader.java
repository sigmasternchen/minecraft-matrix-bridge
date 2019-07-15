package at.overflow.bukkit.matrixbridge;

import java.io.*;
import java.util.Properties;

public class BridgePropertyReader {

    private final String PROPERTIES_FILENAME = "matrixbridge.properties";

    private final String MATRIX_USER_KEY = "matrix.id";
    private final String MATRIX_PASSWORD = "matrix.password";

    private Properties properties = new Properties();

    public BridgePropertyReader() {
        File file = new File(PROPERTIES_FILENAME);

        if (file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            generateEmptyFile(file);
            throw new RuntimeException("Matrixbridge properties are empty.");
        }
    }

    private void generateEmptyFile(File file) {
            properties.setProperty(MATRIX_USER_KEY, "");
            properties.setProperty(MATRIX_PASSWORD, "");
        try {
            properties.store(new FileOutputStream(file), "Matrix Bridge Properties");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getDomain() {
        String tmp = properties.getProperty(MATRIX_USER_KEY);

        return tmp.substring(tmp.indexOf(":") + 1);
    }

    public String getUsername() {
        String tmp = properties.getProperty(MATRIX_USER_KEY);

        return tmp.substring(1, tmp.indexOf(":"));
    }

    public String getPassword() {
        String tmp = properties.getProperty(MATRIX_PASSWORD);
        return tmp;
    }
}
