package ru.kpfu.itis.ilyaskalimullin;

import com.google.gson.Gson;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleParser {
    private final static String DEFAULT_SCRIPT_URL_STRING = "https://script.google.com/macros/s/AKfycbzifKRNe70fgVxiSr63d59R8yNUflk4oTyFtuXYebHbc0ScvSExAF_le9b5K1oPNw6oDA/exec";
    private final static String DEFAULT_PATH_STRING = "C:\\Users\\comp\\Documents\\JavaProjects\\ScheduleParser\\schedule.xml";

    private URL url;
    private Path path;
    private int weekDay;

    public ScheduleParser(String pathString, String scriptUrlString, int weekDay) throws InvalidPathException, IOException {
        path = Paths.get(pathString);
        setWeekDay(weekDay);
        url = new URL(scriptUrlString + "?weekDay=" + weekDay);

        writeSchedule(parseSchedule());

    }

    public ScheduleParser(int weekDay) throws IOException, InvalidPathException {
        this(DEFAULT_PATH_STRING, DEFAULT_SCRIPT_URL_STRING, weekDay);
    }

    public Root parseSchedule() throws IOException {
        String json;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        url.openStream()
                )
        )) {
            json = in.readLine();
        } catch (IOException ex) {
            throw new IOException(ex);
        }

        Gson gson = new Gson();
        return gson.fromJson(json, Root.class);
    }

    public void writeSchedule(Root root) throws IOException {
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path.toFile())
                )
        );

        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        out.write("<resources>");

        Pattern pattern = Pattern.compile("([^,]+),([^,]*),([^,]*)");
        Matcher matcher;

        for (int i = 1; i <= root.schedule.size(); i++) {
            for (int j = 1; j <= root.schedule.get(i - 1).size(); j++) {

                String currentCell = root.schedule.get(i - 1).get(j - 1);
                String[] currentCellSplit = currentCell.split(";");

                if (currentCellSplit.length == 1) {
                    String lesson = currentCellSplit[0];
                    matcher = pattern.matcher(lesson);
                    if (!matcher.find()) continue;
                    if (matcher.group(3).equals("")) continue;
                    out.write("<string name = \"cab" + matcher.group(3) + "\">" + matcher.group(1) + " " + matcher.group(2) + " + " + getGroupNumber(j) + "</string>");
                }

                for (String lesson : currentCellSplit) {
                    matcher = pattern.matcher(lesson);
                    if (!matcher.find()) continue;
                    if (matcher.group(3).equals("")) continue;
                    out.write("<string name = \"cab" + matcher.group(3) + "\">" + matcher.group(1) + " " + matcher.group(2) + " + </string>");
                }
            }
        }

        out.write("</resources>");
        out.flush();

    }

    public void setWeekDay(int weekDay) {
        if (weekDay < 1 || weekDay > 6) throw new IllegalArgumentException();
        this.weekDay = weekDay;
    }

    public String getGroupNumber(int number) {
        String numberString = "11-%03d";

        //52 groups
        if (number < 1) throw new IllegalArgumentException();
        if (number <= 13) {
            number += 100;
        } else if (number <= 26) {
            number -= 13;
        } else if (number <= 37) {
            number = 900 + number - 26;
        } else if (number <= 47) {
            number = number - 37 + 800;
        } else if (number <= 49) {
            number = number - 47 + 120;
        } else {
            number = 100 + (number - 47) * 10 + 1;
        }

        return String.format(numberString, number);
    }
}
