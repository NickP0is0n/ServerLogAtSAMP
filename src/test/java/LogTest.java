import live.nickp0is0n.serverlogatsamp.models.Log;

import java.io.File;
import java.io.IOException;

public class LogTest {
    public static void main(String[] args) throws IOException {
        Log log = new Log(new File("log.txt"));
        Log filtered = log.filterByKeyword("12:41").filterByTag("debug");
        filtered.getLogEntries().forEach(System.out::println);
    }
}
