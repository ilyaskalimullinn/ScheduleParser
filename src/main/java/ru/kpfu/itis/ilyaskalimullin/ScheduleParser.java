package ru.kpfu.itis.ilyaskalimullin;

import com.google.gson.Gson;

import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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

        saveRootSchedule(
                parseRootFromJSON(
                        getJSONFromSheet()
                )
        );

    }

    public ScheduleParser(int weekDay) throws IOException, InvalidPathException {
        this(DEFAULT_PATH_STRING, DEFAULT_SCRIPT_URL_STRING, weekDay);
    }

    public String getJSONFromSheet() throws IOException {
        String json;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        url.openStream()
                )
        )) {
            json = in.readLine();
            return json;
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    }

    public Root parseRootFromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, Root.class);
    }

    public void saveRootSchedule(Root root) throws IOException {
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(path.toFile())
                )
        );

        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        out.write("<resources>");



        for (int lessonNumber = 1; lessonNumber <= root.schedule.size(); lessonNumber++) {
            for (int groupNumber = 1; groupNumber <= root.schedule.get(lessonNumber - 1).size(); groupNumber++) {

                String currentCell = root.schedule.get(lessonNumber - 1).get(groupNumber - 1);
                String[] currentCellSplit = currentCell.split(";");

                if (currentCellSplit.length == 1) {
                    String lesson = currentCellSplit[0];
                    saveLesson(lesson, lessonNumber, groupNumber, false, out);
                } else {

                    for (String lesson : currentCellSplit) {
                        saveLesson(lesson, lessonNumber, groupNumber, true, out);
                    }
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

    public boolean saveLesson(String lesson, int lessonNumber, int groupNumber, boolean isSplitLesson, Writer out) throws IOException {
        //Pattern pattern = Pattern.compile("([^,]+),([^,]*),([^,]*)");
        Pattern pattern = Pattern.compile("((?:[^,]|[\\\\,])+),((?:[^,]|[\\\\,])*),((?:[^,]|[\\\\,])*)");
        Matcher matcher = pattern.matcher(lesson);
        if (!matcher.find()) return false;

        String lessonName = matcher.group(1);
        String lessonTeacher = matcher.group(2);
        String lessonCab = matcher.group(3);

        lessonName = lessonName.replace("\\,", ",");
        lessonTeacher = lessonTeacher.replace("\\,", ",");
        lessonCab = lessonCab.replace("\\,", ",");

        String group;
        if (isSplitLesson) {
            group = "";
        } else {
            group = " + " + getGroup(groupNumber);
        }

        if (matcher.group(3).equals("")) return false;
        //out.write("<string name = \"cab" + matcher.group(3) + " " + lessonNumber + "\">" + matcher.group(1) + " " + matcher.group(2) + " + " + getGroupNumber(groupNumber) + "</string>");
        out.write(String.format("<string name = \"с%s%d\">%s %s%s</string>", lessonCab, lessonNumber, lessonName, lessonTeacher, group));
        return true;
    }

    public Map<String, String> getScheduleMapFromXML() throws IOException {
        Map<String, String> map = new HashMap<>();

        String xmlString;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(path.toFile())
                )
        )) {
            xmlString = in.lines().reduce((s1, s2) -> s1 + s2).get();
        } catch (IOException ex) {
            throw new IOException("Could not read schedule xml file", ex);
        }

        Pattern pattern = Pattern.compile("<string name = \"(с[0-9]+)\">([^<]+)<\\/string>");
        Matcher matcher = pattern.matcher(xmlString);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            map.put(key, value);
        }

        return map;
    }

    public String getGroup(int number) {
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
