package com.example;

import com.example.api.ElpriserAPI;
import com.example.api.ElpriserAPI.Prisklass;
import com.example.api.ElpriserAPI.Elpris;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();

        String askedDatumInStringFormat = "2025-09-21";
        Prisklass priceClass = Prisklass.SE3;
        double meanPriceChosenDate = 0;

        LocalDate askedDatumInLocalDate = LocalDate.parse(askedDatumInStringFormat, DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate tomorrowOfAskedDatum = askedDatumInLocalDate.plusDays(1);

        List<Elpris> mergedList = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);
        mergedList.addAll(elpriserAPI.getPriser(tomorrowOfAskedDatum, priceClass));

        List<Elpris> priceForAskedDate = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);


        //for each loop
        for (Elpris elpris : priceForAskedDate) {
            meanPriceChosenDate += elpris.sekPerKWh();
        }
        meanPriceChosenDate = meanPriceChosenDate / 24;
        System.out.println("Mean price for chosen date is: " + meanPriceChosenDate);

        double cheapestDailyPrice = Double.MAX_VALUE;

        double mostExpensiveDailyPrice = Double.MIN_VALUE;

        ZonedDateTime cheapestHourStart = null;
        ZonedDateTime mostExpensiveHourStart = null;
        for (Elpris elpris : mergedList) {
            double price = elpris.sekPerKWh();
            ZonedDateTime time =elpris.timeStart();

            if (price < cheapestDailyPrice) {
                cheapestDailyPrice = price;
                cheapestHourStart = time;
            }
            if (price > mostExpensiveDailyPrice) {
                mostExpensiveDailyPrice = price;
                mostExpensiveHourStart = time;
            }

        }

        //Sliding window
        double currentCheapestPrices = Double.MAX_VALUE;
        double sumOfSlidingWindow = 0;
        ZonedDateTime cheapestHours = null;
        int sizeOfWindow = 8;

        for (int i = 0; i < mergedList.size() - (sizeOfWindow - 1); i++) {
            for(int j = 0; j < sizeOfWindow; j++) {
                sumOfSlidingWindow += mergedList.get(i+j).sekPerKWh();
            }

            if(sumOfSlidingWindow < currentCheapestPrices) {
                currentCheapestPrices = sumOfSlidingWindow;
                cheapestHours = mergedList.get(i).timeStart();

            }

            sumOfSlidingWindow = 0;
        }

        System.out.println("Cheapest " + sizeOfWindow + " hours starts at: " + cheapestHours);



        System.out.println("Printing merged list" + mergedList);
        System.out.println(cheapestDailyPrice);
        System.out.println(mostExpensiveDailyPrice);
        System.out.println("cheapest time: " + cheapestHourStart + " most expensive time: " + mostExpensiveHourStart);

    }
}
