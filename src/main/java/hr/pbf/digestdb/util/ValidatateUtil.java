package hr.pbf.digestdb.util;


import hr.pbf.digestdb.exception.ValidationException;
public class ValidatateUtil {
    public static void fileMustExist(String filePath) {
        if (filePath == null) {
            throw new ValidationException("File path is null.");
        }

        if (!new java.io.File(filePath).exists()) {
            throw new ValidationException("File not found: " + filePath);
        }
    }

    public static void fileMustNotExist(String filePath) {
        if (filePath == null) {
            throw new RuntimeException("File path is null.");
        }
        if (!new java.io.File(filePath).exists()) {
            throw new RuntimeException("File already exists: " + filePath);
        }
    }
}
