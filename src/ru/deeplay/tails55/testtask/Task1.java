package ru.deeplay.tails55.testtask;

import java.util.*;

public class Task1 {

    private static final int LENGTH = 20;
    private static final int MIN = -5;
    private static final int MAX = 5;

    public static void main(String[] args) {
        Random random = new Random();
        int[] array = new int[LENGTH];
        System.out.println("Random array pre sorting:");
        for (int i = 0; i < LENGTH; i++) {
            array[i] = random.nextInt(-MIN + MAX + 1) + MIN;
            System.out.print(array[i] + " ");
        }

        array = Arrays.stream(array).boxed().sorted((a, b) -> {
            if ((a & 1) != (b & 1)) // Если числа разной чётности
                return (a & 1) == 1 ? -1 : 1; // То нечётное ставим слева, а чётное справа
            if (a == 0 || b == 0) // Иначе если среди них есть ноль
                return a == 0 ? -1 : 1; // То ноль ставим слева, а не ноль справа (если оба нули, то и разницы 0)
            if ((a & 1) == 1) // Иначе если первое число нечётное (а, значит, второе тоже)
                return a.compareTo(b); // Сортируем по возрастанию
            return b.compareTo(a); // Иначе сортируем по убыванию
        }).mapToInt(Integer::intValue).toArray();


        System.out.println("\nRandom array post sorting:");
        for (int i = 0; i < LENGTH; i++) {
            System.out.print(array[i] + " ");
        }
    }
}
