package ru.deeplay.tails55.testtask;

import java.util.*;

// Альтернативное решение динамическим программированием
// Значительно медленнее, но в плане точности ограничено только даблами (можно переписать на BigInteger,
// чтобы не терять очень маленькие шансы вроде "33 очка из 100", но роли это не сыграет, а память съест)
public class Task3NonMonteCarlo {
    private static final int SCORE_DIFF = 0;
    private static final int P1SEQ_STEP = 1;
    private static final int P2SEQ_STEP = 2;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите количество сторон у игральной кости:");
        int dieSize = scanner.nextInt();
        if (dieSize <= 0) {
            System.out.println("У игральной кости должно быть не менее 1 стороны!");
            return;
        }

        System.out.println("Введите длину выбираемой каждым игроком последовательности результатов:");
        int sequenceSize = scanner.nextInt();
        if (sequenceSize <= 0) {
            System.out.println("Длина выбираемой каждым игроком последовательности должна быть не менее 1!");
            return;
        }

        System.out.println("Введите длину игры в бросках игральной кости:");
        int gameLength = scanner.nextInt();
        if (gameLength <= 0) {
            System.out.println("Длина игры должна быть не менее 0 бросков!");
            return;
        }

        System.out.println("Введите выбранную первым игроком последовательность чисел:");
        int[] p1target = new int[sequenceSize];
        for (int i = 0; i < sequenceSize; i++) {
            p1target[i] = scanner.nextInt() - 1;
            if (p1target[i] < 0 || p1target[i] >= dieSize) {
                System.out.println("Игроки не могут выбирать числа, меньшие 1 или большие количества сторон кости!");
                return;
            }
        }

        System.out.println("Введите выбранную вторым игроком последовательность чисел:");
        int[] p2target = new int[sequenceSize];
        for (int i = 0; i < sequenceSize; i++) {
            p2target[i] = scanner.nextInt() - 1;
            if (p2target[i] < 0 || p2target[i] >= dieSize) {
                System.out.println("Игроки не могут выбирать числа, меньшие 1 или большие количества сторон кости!");
                return;
            }
        }

        // i-й битсет отображает позицию j на то, заканчивается ли в позиции j подстрока длины i+1, равная таковой, начинающейся с позиции 1
        BitSet[] p1subArrayEqualities = new BitSet[sequenceSize];
        BitSet[] p2subArrayEqualities = new BitSet[sequenceSize];
        for (int i = 0; i < sequenceSize; i++) {
            p2subArrayEqualities[i] = new BitSet();
            p1subArrayEqualities[i] = new BitSet();
            for (int j = i; j < sequenceSize; j++) {
                //Если символ в j и i одинаков и в j-1 есть совпадение длины i-1, в j есть совпадение длины i
                if (p1target[i] == p1target[j] && (i == 0 || p1subArrayEqualities[i - 1].get(j - 1)))
                    p1subArrayEqualities[i].set(j);
                if (p2target[i] == p2target[j] && (i == 0 || p2subArrayEqualities[i - 1].get(j - 1)))
                    p2subArrayEqualities[i].set(j);
            }
        }

        // pXnextProgress[i][j] - прогресс после того, как к последовательности с прогрессом i выпало j
        int[][] p1nextProgress = new int[sequenceSize][];
        int[][] p2nextProgress = new int[sequenceSize][];

        for (int i = 0; i < sequenceSize; i++) {
            p1nextProgress[i] = new int[dieSize];
            p2nextProgress[i] = new int[dieSize];
            for (int j = 0; j < dieSize; j++) {
                p1nextProgress[i][j] = 0;
                p2nextProgress[i][j] = 0;

                // Продолжение прогресса
                if (p1target[i] == j)
                    p1nextProgress[i][j] = i + 1;
                    // Падаем назад, вопрос как далеко
                else for (int k = i - 1; k >= 0; k--) {
                    //Можем упасть до k+1, если в k-й позиции j, а хвост до i-1 длиной k совпадает с головой с 0 той же длины
                    if (p1target[k] == j && (k == 0 || p1subArrayEqualities[k - 1].get(i - 1))) {
                        p1nextProgress[i][j] = k + 1;
                    }
                }

                if (p2target[i] == j)
                    p2nextProgress[i][j] = i + 1;
                else for (int k = i - 1; k >= 0; k--) {
                    if (p2target[k] == j && (k == 0 || p2subArrayEqualities[k - 1].get(i - 1))) {
                        p2nextProgress[i][j] = k + 1;
                    }
                }
            }
        }
        // Вход - вектор длиной 3, первая координата - счёт (разница между рез-ми 1го и 2го игроков), вторая и третья - текущие длины цепочек игроков 1 и 2;
        // Выход - вероятность такого вектора
        Map<List<Integer>, Double> results = new HashMap<>();
        // Выход - средний результат первого игрока, помноженный на вероятность из results
        Map<List<Integer>, Double> scores = new HashMap<>();

        results.put(List.of(0, 0, 0), 1.);
        scores.put(List.of(0, 0, 0), 0.);


        for (int rollNumber = 1; rollNumber <= gameLength; rollNumber++) {
            Map<List<Integer>, Double> nextResults = new HashMap<>();
            Map<List<Integer>, Double> nextScores = new HashMap<>();

            // Для всех возможных текущих счётов, состояний прогресса и результатов броска игральной кости обновляем соответствующую запись в nextResults
            for (List<Integer> keys : results.keySet()) {
                int i = keys.get(SCORE_DIFF);
                int j = keys.get(P1SEQ_STEP);
                int k = keys.get(P2SEQ_STEP);
                double output = results.getOrDefault(List.of(i, j, k), 0.) / dieSize;
                if (Double.compare(output, 0) <= 0)
                    continue;
                double score = scores.getOrDefault(List.of(i, j, k), 0.) / dieSize;
                for (int d = 0; d < dieSize; d++) {
                    double tempScore=score;
                    List<Integer> input = new ArrayList<>(List.of(i, p1nextProgress[j][d], p2nextProgress[k][d]));
                    if (input.get(P1SEQ_STEP) == sequenceSize) {
                        input.set(P1SEQ_STEP, 0);
                        input.set(SCORE_DIFF, input.get(SCORE_DIFF) + 1);
                        tempScore += output;
                    }
                    if (input.get(P2SEQ_STEP) == sequenceSize) {
                        input.set(P2SEQ_STEP, 0);
                        input.set(SCORE_DIFF, input.get(SCORE_DIFF) - 1);
                    }
                    nextResults.put(input, nextResults.getOrDefault(input,0.) + output);
                    nextScores.put(input, nextScores.getOrDefault(input,0.) + tempScore);
                }
            }
            results = nextResults;
            scores = nextScores;
        }

        // Опять же, знаю, что не обязательно, но раз уж могу эти данные тоже получить
        double p1winChance = 0.;
        double p2winChance = 0.;
        double p1scores = 0.;
        double p2scores = 0.;

        for (int i = -gameLength; i <= gameLength; i++) {
            for (int j = 0; j < sequenceSize; j++) {
                for (int k = 0; k < sequenceSize; k++) {
                    double output = results.getOrDefault(List.of(i, j, k),0.);
                    if(Double.compare(output,0)<=0)
                        continue;
                    if (i > 0)
                        p1winChance += output;
                    else if (i < 0)
                        p2winChance += output;
                    p1scores+=scores.getOrDefault(List.of(i,j,k),0.0);
                    p2scores+=scores.getOrDefault(List.of(i,j,k),0.0)-output*i;
                }
            }
        }

        System.out.println("Вероятность победы игрока 1 ~= " + (100. * p1winChance) + "%;");
        System.out.println("Вероятность победы игрока 2 ~= " + (100. * p2winChance) + "%;");
        System.out.println("Вероятность ничьи ~= " + (100. * (1 - p1winChance - p2winChance)) + "%;");
        System.out.println("Средний результат игрока 1 ~= " + (p1scores) + " очков;");
        System.out.println("Средний результат игрока 2 ~= " + (p2scores) + " очков;");
    }
}
