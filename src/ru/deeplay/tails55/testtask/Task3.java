package ru.deeplay.tails55.testtask;


import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Раз можно Монте-Карло, решу через Монте-Карло, заодно многопоточность попробую
public class Task3 {
    private static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Set<MonteCarloThread> activeThreads = new HashSet<>();
    private static long totalP1wins = 0;
    private static long totalP2wins = 0;
    private static long totalP1scores = 0;
    private static long totalP2scores = 0;
    private static long totalDraws = 0;
    private static long totalGames = 0;

    private static int dieSize;
    private static int sequenceSize;

    // Один поток симуляции Монте-Карло, получает нужные для симуляции данные, симулирует свою долю матчей и потом
    // 1 раз обновляет общие данные; если этот поток последний, он сообщит результаты и "выключит за собой свет"
    private static class MonteCarloThread extends Thread {
        private static int nextId = 0;
        private final int id;
        private long p1wins = 0;
        private long p2wins = 0;
        private long draws = 0;
        private long p1scores = 0;
        private long p2scores = 0;
        private final int[] p1target;
        private final int[] p2target;
        private final int gameLength;
        private final Random random = new Random();
        private long matchesLeft;

        public MonteCarloThread(int[] p1target, int[] p2target, int gameLength, long matchesLeft) {
            this.p1target = p1target;
            this.p2target = p2target;
            this.matchesLeft = matchesLeft;
            this.gameLength = gameLength;
            this.id = nextId++;
        }

        @Override
        public void run() {
            // Симуляция очередного матча
            while (matchesLeft > 0) {
                matchesLeft--;
                List<Integer> diceQueue = new ArrayList<>();
                long p1score = 0;
                long p2score = 0;
                // Сколько бросков ещё должно произойти, чтобы можно было получить очередное очко
                int p1scoreCD = sequenceSize;
                int p2scoreCD = sequenceSize;
                // Симуляция i-го броска
                for (int i = 0; i < gameLength; i++) {
                    p1scoreCD--;
                    p2scoreCD--;

                    diceQueue.add(random.nextInt(dieSize));
                    if (diceQueue.size() > sequenceSize)
                        diceQueue.remove(0);
                    // Шанс получить очередное очко, если есть совпадение и мы не на "перезарядке"
                    if (p1scoreCD <= 0) {
                        boolean addScore = true;
                        for (int j = 0; j < sequenceSize && addScore; j++)
                            addScore = diceQueue.get(j) == p1target[j];
                        if (addScore) {
                            p1score++;
                            p1scoreCD = sequenceSize;
                        }
                    }
                    if (p2scoreCD <= 0) {
                        boolean addScore = true;
                        for (int j = 0; j < sequenceSize && addScore; j++)
                            addScore = diceQueue.get(j) == p2target[j];
                        if (addScore) {
                            p2score++;
                            p2scoreCD = sequenceSize;
                        }
                    }
                }
                // Исход матча
                if (p1score == p2score)
                    draws++;
                else if (p1score > p2score)
                    p1wins++;
                else
                    p2wins++;

                p1scores += p1score;
                p2scores += p2score;
            }
            // Обновляем общие данные и закрываем поток
            try {
                readWriteLock.writeLock().lock();
                totalP1wins += p1wins;
                totalP2wins += p2wins;
                totalDraws += draws;
                totalGames += p1wins + p2wins + draws;
                totalP1scores += p1scores;
                totalP2scores += p2scores;
                activeThreads.remove(this);
                // Кто последний, тот и отчитывается
                if (activeThreads.isEmpty()) {
                    System.out.println("Вероятность победы игрока 1 ~= " + (100. * totalP1wins / totalGames) + "%;");
                    System.out.println("Вероятность победы игрока 2 ~= " + (100. * totalP2wins / totalGames) + "%;");
                    System.out.println("Вероятность ничьи ~= " + (100. * totalDraws / totalGames) + "%;");
                    // По условию сначала подумал, что это тоже нужно, решил оставить
                    System.out.println("Средний результат игрока 1 ~= " + (1. * totalP1scores / totalGames) + " очков;");
                    System.out.println("Средний результат игрока 2 ~= " + (1. * totalP2scores / totalGames) + " очков;");
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MonteCarloThread that = (MonteCarloThread) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите количество сторон у игральной кости:");
        dieSize = scanner.nextInt();
        if (dieSize <= 0) {
            System.out.println("У игральной кости должно быть не менее 1 стороны!");
            return;
        }

        System.out.println("Введите длину выбираемой каждым игроком последовательности результатов:");
        sequenceSize = scanner.nextInt();
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

        System.out.println("Введите количество матчей, которые вы хотите проанализировать:");
        int gameCount = scanner.nextInt();
        if (gameCount <= 0) {
            System.out.println("Вы должны проанализировать хотя бы 1 матч!");
            return;
        }

        System.out.println("Введите количество потоков, выделяемых для анализа матчей:");
        int threadCount = scanner.nextInt();
        if (threadCount <= 0 || threadCount > gameCount) {
            System.out.println("Вы должны использовать хотя бы 1 поток, но не больше, чем количество анализируемых матчей!");
            return;
        }

        for (int i = threadCount; i > 0; i--) {
            MonteCarloThread thread = new MonteCarloThread(Arrays.copyOf(p1target, sequenceSize),
                    Arrays.copyOf(p2target, sequenceSize), gameLength, gameCount / i);
            activeThreads.add(thread);
            gameCount -= gameCount / i;
        }

        for (MonteCarloThread thread : activeThreads)
            thread.start();
    }
}
