package com.helospark.tactview.ui.javafx.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DurationFormatter {

    public static String fromSeconds(BigDecimal seconds) {
        double secondsDouble = seconds.doubleValue();

        int hoursInt = (int) (secondsDouble / (60 * 60));
        int minutesInt = (int) (secondsDouble - hoursInt * (60 * 60)) / 60;
        int secondInt = (int) (secondsDouble - hoursInt * (60 * 60) - minutesInt * 60);
        int millisecondsInt = (int) ((secondsDouble - hoursInt * 60 * 60 - minutesInt * 60 - secondInt) * 1000);

        return String.format("%01d:%02d:%02d.%03d", hoursInt, minutesInt, secondInt, millisecondsInt);
    }

    public static BigDecimal toSeconds(String data) {
        List<String> parts = Arrays.asList(data.split(":"));
        Collections.reverse(parts);

        int hoursInt = 0;
        int minutesInt = 0;
        int secondInt = 0;
        int millisecondsInt = 0;

        if (parts.size() > 0) {
            String[] secondsAndMillisecondsPart = parts.get(0).split("\\.");
            if (secondsAndMillisecondsPart.length > 1) {
                millisecondsInt = Integer.parseInt(secondsAndMillisecondsPart[1]);
            }
            if (secondsAndMillisecondsPart.length > 0) {
                secondInt = Integer.parseInt(secondsAndMillisecondsPart[0]);
            }
        }
        if (parts.size() > 1) {
            minutesInt = Integer.parseInt(parts.get(1));
        }
        if (parts.size() > 2) {
            hoursInt = Integer.parseInt(parts.get(2));
        }

        return new BigDecimal(hoursInt * 3600 + minutesInt * 60 + secondInt + millisecondsInt / 1000.0);
    }

}
