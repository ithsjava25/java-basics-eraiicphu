package com.example;

import com.example.api.ElpriserAPI;

public class Main {
    public static void main(String[] args) {


        ElpriserAPI elpriserAPI = new ElpriserAPI();


        System.out.println(elpriserAPI.getPriser("2025-09-21", ElpriserAPI.Prisklass.SE3));
    }
}
