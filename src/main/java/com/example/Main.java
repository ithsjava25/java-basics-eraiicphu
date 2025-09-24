package com.example;

import com.example.api.ElpriserAPI;
import com.example.api.ElpriserAPI.Prisklass;
import com.example.api.ElpriserAPI.Elpris;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();

        String askedDatumInStringFormat = "2025-09-25";

        LocalDate askedDatumInLocalDate = LocalDate.parse(askedDatumInStringFormat, DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate tomorrowOfAskedDatum =  askedDatumInLocalDate.plusDays(1);

        Prisklass priceClass = Prisklass.SE3;

        List<Elpris> mergedList = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);
        mergedList.addAll(elpriserAPI.getPriser(tomorrowOfAskedDatum, priceClass));

        List<Elpris> priceForAskedDate = elpriserAPI.getPriser(askedDatumInLocalDate, priceClass);

        double meanPriceChosenDate = 0;


        //for each loop
        for (Elpris elpris : priceForAskedDate) {
            meanPriceChosenDate += elpris.sekPerKWh();
        }
        meanPriceChosenDate = meanPriceChosenDate/24;
        System.out.println("Mean price for chosen date is: " + meanPriceChosenDate);



        // 1. Kunna välja mitt datum som jag ska hämta elpriser för.
        // 2. Kunna välja för vilken zon som jag ska hämta elprise för.
        System.out.println("Test Elpris data som är hämtad för efterfrågade datumet: "
                + mergedList);

    }
}
