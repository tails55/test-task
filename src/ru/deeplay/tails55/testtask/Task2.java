package ru.deeplay.tails55.testtask;

import java.util.*;

public class Task2 {
    static class Pair<F, S> {
        private F first;
        private S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public void setFirst(F first) {
            this.first = first;
        }

        public S getSecond() {
            return second;
        }

        public void setSecond(S second) {
            this.second = second;
        }
    }

    private static final int length = 20;
    private static final int min = -5;
    private static final int max = 5;

    public static void main(String[] args) {
        Random random = new Random();
        int[] array = new int[length];
        System.out.println("Random array pre sorting:");
        for (int i = 0; i < length; i++) {
            array[i] = random.nextInt(-min + max + 1) + min;
            System.out.print(array[i] + " ");
        }

        //Считаем частоту упоминания каждого числа в массиве
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int i = 0; i < length; i++)
            frequencyMap.put(array[i], frequencyMap.getOrDefault(array[i], 0) + 1);
        //Находим числа с максимальной частотой упоминания и эту самую частоту (можно было бы объединить с заполнением HashMap, но eh...)
        Pair<Integer, Set<Integer>> result = new Pair<>(0, new TreeSet<>());
        for (int key : frequencyMap.keySet()) {
            int count = frequencyMap.get(key);
            if (count > result.getFirst()) {
                result.getSecond().clear();
                result.setFirst(count);
            }
            if (count >= result.getFirst())
                result.getSecond().add(key);
        }

        System.out.println("\nThe most frequent numbers in the array, at "+result.getFirst()+" total occurences each, are:");
        for(int value : result.getSecond())
            System.out.print(value+" ");
    }
}
