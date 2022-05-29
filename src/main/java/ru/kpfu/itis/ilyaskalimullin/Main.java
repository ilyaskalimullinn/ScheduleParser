package ru.kpfu.itis.ilyaskalimullin;

public class Main {
    public static void main(String[] args) {
        try {
            ScheduleParser parser = new ScheduleParser(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
