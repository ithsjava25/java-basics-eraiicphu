package com.example;

import com.example.api.ElpriserAPI;
import com.example.api.ElpriserAPI.Prisklass;
import com.example.api.ElpriserAPI.Elpris;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Usage: args needs to include zone, date or sorted. Give argument --help for more information.");
            return;
        }

        if(args[0].equals("--help")){
            System.out.println("Usage: To use the program, you need to give arguments:\n--zone in the format SE1, SE2, SE3 or SE4.\n--date in the format YYYY-MM-DD e.g. 2025-01-01.\n--charging in the format 2h, 4h or 8h to get the best charging hours for the date requested.\n--sorted to get the list sorted by price in descending order.");
            return;
        }


        ElpriserAPI elpriserAPI = new ElpriserAPI();

        String askedDatumInStringFormat = null;
        Prisklass priceClass = null;
        int sizeOfWindow = 0;

        if(args[0].equals("--zone")){
            try{
                priceClass = Prisklass.valueOf(args[1]);
            } catch(IllegalArgumentException e){
                System.out.println("invalid zone");
                return;
            }
        }
        else{
            System.out.println("--zone is required");
            return;
        }

        if(args.length > 2 && args[2].equals("--date")){
            askedDatumInStringFormat = args[3];
        }
        else {
            askedDatumInStringFormat = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        if(args.length > 4 && args[4].equals("--charging")){
            sizeOfWindow = Integer.parseInt(args[5].replace("h",""));
        }

        try{
            LocalDate askedDatumInLocalDate = LocalDate.parse(askedDatumInStringFormat, DateTimeFormatter.ISO_LOCAL_DATE);

            List<Elpris> todaysElpriser = getSingleDayElpriser(elpriserAPI, askedDatumInLocalDate, priceClass);

            for (String arg : args) {
                if (arg.equals("--sorted")) {
                    printSortedPrices(todaysElpriser);
                    return;
                }
            }

            List<Elpris> todayAndTomorrowElpriser = getTwoDaysElpriser(elpriserAPI, askedDatumInLocalDate, priceClass);

            if(todayAndTomorrowElpriser.isEmpty() || todaysElpriser.isEmpty()){
                System.out.println("no data");
                return;
            }


            double meanPriceChosenDate = getMeanPriceChosenDate(todaysElpriser);

            String cheapestDailyPrice = getCheapestDailyPrice(todayAndTomorrowElpriser);

            String mostExpensiveDailyPrice = getMostExpensiveDailyPrice(todayAndTomorrowElpriser);

            String cheapestHour = getCheapestDailyHour(todayAndTomorrowElpriser);

            String mostExpensiveHour = getMostExpensiveDailyHour(todayAndTomorrowElpriser);

            String cheapestHoursToCharge = getCheapestHourToChargeStartTime(todayAndTomorrowElpriser, sizeOfWindow);

            System.out.println("lägsta pris: " + cheapestDailyPrice);
            System.out.println("billigaste timman: " + cheapestHour);
            System.out.println("högsta pris: " + mostExpensiveDailyPrice);
            System.out.println("dyraste timman: " + mostExpensiveHour);
            System.out.println("medelpris för vald datum: " + meanPriceChosenDate);
            System.out.println("Påbörja laddning mellan kl " + cheapestHoursToCharge);

        } catch (DateTimeParseException e){
            System.out.println("Invalid date");
        }
    }


    private static List<Elpris> getTwoDaysElpriser(ElpriserAPI elpriserAPI, LocalDate askedDatumInLocalDate, Prisklass zone) {
        LocalDate tomorrowOfAskedDatum = askedDatumInLocalDate.plusDays(1);

        List<Elpris> todayAndTomorrowsElprisList = elpriserAPI.getPriser(askedDatumInLocalDate, zone);
        todayAndTomorrowsElprisList.addAll(elpriserAPI.getPriser(tomorrowOfAskedDatum, zone));

        return todayAndTomorrowsElprisList;
    }

    private static List<Elpris> getSingleDayElpriser(ElpriserAPI elpriserAPI, LocalDate askedDatumInLocalDate, Prisklass zone) {
        return elpriserAPI.getPriser(askedDatumInLocalDate, zone);
    }

    private static double getMeanPriceChosenDate(List<Elpris> elprisForChosenDate) {
        double meanPriceChosenDate = 0;

        for (Elpris elpris : elprisForChosenDate) {
            meanPriceChosenDate += elpris.sekPerKWh();
        }
        meanPriceChosenDate = meanPriceChosenDate / elprisForChosenDate.size();

        return meanPriceChosenDate;
    }

    private static String getCheapestDailyPrice(List<Elpris> elprisersForChosenDate) {
        List<Double> sekPerKwhList = new ArrayList<>();

        for(Elpris elpris : elprisersForChosenDate) {
            sekPerKwhList.add(elpris.sekPerKWh());
        }

        return formatToOre(Collections.min(sekPerKwhList));
    }


    private static String getCheapestDailyHour(List<Elpris> elprisersForChosenDate) {
        double cheapestDailyPrice = Double.MAX_VALUE;
        int cheapestHourStart = 0;
        int cheapestHourEnd = 0;

        for (Elpris elpris : elprisersForChosenDate) {
            double price = elpris.sekPerKWh();
            if (price < cheapestDailyPrice) {
                cheapestDailyPrice = price;
                cheapestHourStart = elpris.timeStart().getHour();
                cheapestHourEnd = elpris.timeEnd().getHour();
            }
        }

        return String.format("%02d", cheapestHourStart) + "-"  + String.format("%02d", cheapestHourEnd) ;
    }

    private static String getMostExpensiveDailyPrice(List<Elpris> elpriserForChosenDate) {
        List<Double> sekPerKwhList = new ArrayList<>();

        for(Elpris elpris : elpriserForChosenDate) {
            sekPerKwhList.add(elpris.sekPerKWh());
        }

        return formatToOre(Collections.max(sekPerKwhList));
    }

    private static String getMostExpensiveDailyHour(List<Elpris> elpriserForChosenDate) {
        double mostExpensiveDailyPrice = Double.MIN_VALUE;
        int mostExpensiveHourStart = 0;
        int mostExpensiveHourEnd = 0;

        for (Elpris elpris : elpriserForChosenDate) {
            double price = elpris.sekPerKWh();
            if (price > mostExpensiveDailyPrice) {
                mostExpensiveDailyPrice = price;
                mostExpensiveHourStart = elpris.timeStart().getHour();
                mostExpensiveHourEnd = elpris.timeEnd().getHour();
            }
        }
        return String.format("%02d", mostExpensiveHourStart) + "-"  + String.format("%02d", mostExpensiveHourEnd);
    }

    private static String getCheapestHourToChargeStartTime(List<Elpris> elpriserForChosenDate, int sizeOfWindow) {
        //Sliding window
        double currentCheapestPrices = Double.MAX_VALUE;
        double sumOfSlidingWindow = 0;
        int cheapestHoursStartTime = 0;
        int cheapestHoursEndTime = 0;
        int tempCheapestHoursEndTime = 0;

        for (int i = 0; i < elpriserForChosenDate.size() - (sizeOfWindow - 1); i++) {
            for(int j = 0; j < sizeOfWindow; j++) {
                sumOfSlidingWindow += elpriserForChosenDate.get(i+j).sekPerKWh();
                tempCheapestHoursEndTime = elpriserForChosenDate.get(i+j).timeEnd().getHour();
            }

            if(sumOfSlidingWindow < currentCheapestPrices) {
                currentCheapestPrices = sumOfSlidingWindow;
                cheapestHoursStartTime = elpriserForChosenDate.get(i).timeStart().getHour();
                cheapestHoursEndTime = tempCheapestHoursEndTime;

                System.out.println("Medelpris för fönster: " + formatToOre(sumOfSlidingWindow / sizeOfWindow) + " öre");
            }

            sumOfSlidingWindow = 0;
        }

        return formatToHHMM(cheapestHoursStartTime) + "-"  + formatToHHMM(cheapestHoursEndTime);
    }

    public static String formatToOre(double number) {
        double percent = number * 100;
        String formatted = String.format("%.2f", percent);
        return formatted.replace('.', ',');
    }

    public static String formatToHHMM(int hour){
        return String.format("%02d", hour) + ":00";
    }

    public static void printSortedPrices(List<Elpris> elprisList){
        elprisList.sort(Comparator.comparingDouble(Elpris::sekPerKWh));

        System.out.println(elprisList);

        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH");
        for (Elpris elpris : elprisList) {
            String startHour = elpris.timeStart().format(hourFormatter);
            String endHour = elpris.timeEnd().format(hourFormatter);
            double sek = elpris.sekPerKWh();


            System.out.println(startHour + "-" + endHour + " " + formatToOre(sek) + " öre");
        }
    }
}
