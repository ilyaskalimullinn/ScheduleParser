package ru.kpfu.itis.ilyaskalimullin;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            ScheduleParser parser = new ScheduleParser(1);

            Map<String, String> map = parser.getScheduleMapFromXML();

            System.out.println(map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
