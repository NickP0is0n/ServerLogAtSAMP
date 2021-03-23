package live.nickp0is0n.serverlogatsamp.models;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return (ArrayList<String>) logEntries.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
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

    public List<String> retrieveTags() {
        List<String> tags = new ArrayList<>();
        tags.add("-");
        logEntries.forEach((String entry) -> {
            String pattern = "\\[.*?]";
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher patternMatcher = compiledPattern.matcher(entry);
            patternMatcher.find(); //ignore timestamp
            if (patternMatcher.find()) {
                String matchedString = entry.substring(patternMatcher.start() + 1, patternMatcher.end() - 1);
                if (!tags.contains(matchedString)) {
                    tags.add(matchedString);
                }
            }
        });
        return tags;
    }

}
