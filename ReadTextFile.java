import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadTextFile {
    public static void main(String[] args) {
        try {
            File file = new File("input.txt"); // File name
            Scanner sc = new Scanner(file);

           StringBuilder sb = new StringBuilder();
while (sc.hasNextLine()) {
    String line = sc.nextLine();
    sb.append(line).append("\n");
}
System.out.println("Contents of StringBuilder:");
System.out.println(sb.toString());


            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }
}