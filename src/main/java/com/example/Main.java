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

        String askedDatumInStringFormat = "2025-09-28";
        Prisklass priceClass = Prisklass.SE3;
        double meanPriceChosenDate = 0;

        LocalDate askedDatumInLocalDate = LocalDate.parse(askedDatumInStringFormat, DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate tomorrowOfAskedDatum =  askedDatumInLocalDate.plusDays(1);

        List<Elpris> mergedList = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);
        mergedList.addAll(elpriserAPI.getPriser(tomorrowOfAskedDatum, priceClass));

        List<Elpris> priceForAskedDate = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);


        //for each loop
        for (Elpris elpris : priceForAskedDate) {
            meanPriceChosenDate += elpris.sekPerKWh();
        }
        meanPriceChosenDate = meanPriceChosenDate/24;
        System.out.println("Mean price for chosen date is: " + meanPriceChosenDate);

        double cheapestDailyPrice = Double.MAX_VALUE;

        double mostExpensiveDailyPrice = Double.MIN_VALUE;

        for (Elpris elpris : mergedList) {
            double price = elpris.sekPerKWh();
            ZonedDateTime cheapestHourStart = elpris.timeStart();
            ZonedDateTime mostExpensiveHourStart = elpris.timeStart();

            if (price < cheapestDailyPrice){
                cheapestDailyPrice = price;
                cheapestHourStart = elpris.timeStart();
            }
            if (price > mostExpensiveDailyPrice){
                mostExpensiveDailyPrice = price;
                mostExpensiveHourStart = elpris.timeStart();
            }

        }
        System.out.println("Printing merged list" + mergedList);
        System.out.println(cheapestDailyPrice);
        System.out.println(mostExpensiveDailyPrice);
        System.out.println("cheapest hour: " + cheapestDailyPrice + " most expensive hour: " + mostExpensiveDailyPrice);





    }
}
