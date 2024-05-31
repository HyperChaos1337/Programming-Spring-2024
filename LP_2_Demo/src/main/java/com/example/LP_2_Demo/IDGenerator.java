package com.example.LP_2_Demo;

import java.util.HashSet;
import java.util.Random;

public class IDGenerator {

    private static final HashSet<Integer> usedIDs = new HashSet<>();
    private static final Random random = new Random();

    public static int generateUniqueID() {
        int id;
        do {
            id = 100000 + random.nextInt(900000); // Генерация числа от 100000 до 999999
        } while (usedIDs.contains(id));
        usedIDs.add(id);
        return id;
    }
}
