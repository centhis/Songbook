package ru.centhis.songbook.util;



import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MBFileUtils {

    public static boolean fileExists(String filePath){
        File file = new File(filePath);
        boolean test = file.exists();
        return file.exists();
    }

    public static void createFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        PrintWriter out = new PrintWriter(filePath);
        out.print(content);
        out.close();
    }

    public static String readFile(String filePath) throws IOException{
        File file = new File(filePath);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        try (Scanner scanner = new Scanner(file)){
            while (scanner.hasNextLine()){
                fileContents.append(scanner.nextLine()).append(System.lineSeparator());
            }
            return fileContents.toString();
        }
    }

    public static void writeToFile(String content, String filePath) throws IOException {
        PrintWriter out = new PrintWriter(filePath);
        out.print(content);
        out.close();
    }
}
