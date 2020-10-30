package live.nickp0is0n.serverlogatsamp.models;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Log {
    private ArrayList<String> logEntries = new ArrayList<>();

    public Log(File logFile) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), "Windows-1251"))) {
            String line;
            while((line = reader.readLine()) != null) {
                logEntries.add(line);
            }
        }
    }

    public Log (ArrayList<String> logEntries) {
        this.logEntries = logEntries;
    }

    public ArrayList<String> getLogEntries() {
        return logEntries;
    }

    public Log filterByKeyword(String keyword) {
        ArrayList<String> filteredEntries = new ArrayList<>();
        logEntries.forEach((entry) -> {
            if (entry.contains(keyword)) filteredEntries.add(entry);
        });
        return new Log(filteredEntries);
    }

    public Log filterByTag(String tag) {
        return filterByKeyword("[" + tag + "]");
    }

    public Log filterByTime(String time) {
        return filterByKeyword("[" + time);
    }

}
