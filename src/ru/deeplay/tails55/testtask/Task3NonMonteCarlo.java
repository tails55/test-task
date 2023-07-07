package ru.deeplay.tails55.testtask;

import java.util.*;

// Альтернативное решение динамическим программированием
// В плане точности ограничено даблами (можно переписать на BigInteger), но *гораздо* медленнее
public class Task3NonMonteCarlo {
    private static class Pair<T> {
        private T first;
        private T second;

        public Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public void setFirst(T first) {
            this.first = first;
        }

        public T getSecond() {
            return second;
        }

        public void setSecond(T second) {
            this.second = second;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?> triplet = (Pair<?>) o;

            if (!Objects.equals(first, triplet.first)) return false;
            return Objects.equals(second, triplet.second);
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }

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
                    if (p1target[k] == j && (k == 0 || p1subArrayEqualities[k - 1].get(i-1))) {
                        p1nextProgress[i][j] = k + 1;
                    }
                }

                if (p2target[i] == j)
                    p2nextProgress[i][j] = i + 1;
                else for (int k = i - 1; k >= 0; k--) {
                    if (p2target[k] == j && (k == 0 || p2subArrayEqualities[k - 1].get(i-1))) {
                        p2nextProgress[i][j] = k + 1;
                    }
                }
            }
        }

        Map<Pair<Integer>, Double> p1probabilities = new HashMap<>();
        Map<Pair<Integer>, Double> p2probabilities = new HashMap<>();

        // Вероятность иметь 0 очков и 0 прогресса до 1 раунда равна 1
        p1probabilities.put(new Pair<>(0, 0), 1.);
        p2probabilities.put(new Pair<>(0, 0), 1.);

        for (int rollNumber = 1; rollNumber <= gameLength; rollNumber++) {
            Map<Pair<Integer>, Double> nextp1probabilities = new HashMap<>();
            Map<Pair<Integer>, Double> nextp2probabilities = new HashMap<>();

            // Инициируем следующий уровень
            for (int i = 0; i <= rollNumber; i++) {
                for (int j = 0; j < sequenceSize; j++) {
                    nextp1probabilities.put(new Pair<>(i, j), 0.);
                    nextp2probabilities.put(new Pair<>(i, j), 0.);
                }
            }
            // Для всех возможных текущих счётов, состояний прогресса и результатов броска игральной кости обновляем соответствующее поле таблицы
            for (int i = 0; i < rollNumber; i++) {
                for (int j = 0; j < sequenceSize; j++) {
                    for (int d = 0; d < dieSize; d++) {
                        Pair<Integer> p1triplet = new Pair<>(i, p1nextProgress[j][d]);
                        if (p1triplet.getSecond() == sequenceSize) {
                            p1triplet.setSecond(0);
                            p1triplet.setFirst(p1triplet.getFirst() + 1);
                        }
                        nextp1probabilities.put(p1triplet, nextp1probabilities.get(p1triplet) + p1probabilities.getOrDefault(new Pair<>(i, j),0.) / dieSize);

                        Pair<Integer> p2triplet = new Pair<>(i, p2nextProgress[j][d]);
                        if (p2triplet.getSecond() == sequenceSize) {
                            p2triplet.setSecond(0);
                            p2triplet.setFirst(p2triplet.getFirst() + 1);
                        }
                        nextp2probabilities.put(p2triplet, nextp2probabilities.get(p2triplet) + p2probabilities.getOrDefault(new Pair<>(i, j),0.) / dieSize);
                    }
                }
            }
            // Заменяем старый уровень новым
            p1probabilities=nextp1probabilities;
            p2probabilities=nextp2probabilities;
        }

        double[] p1resultProbabilities=new double[gameLength+1];
        double[] p2resultProbabilities=new double[gameLength+1];
        // Опять же, знаю, что не обязательно, но раз уж могу эти данные тоже получить
        double p1score=0.;
        double p2score=0.;
        double p1winChance=0.;
        double p2winChance=0.;
        double drawChance=0.;

        for(int i=0;i<=gameLength;i++) {
            for(int j=0;j<sequenceSize;j++) {
                p1resultProbabilities[i]+=p1probabilities.getOrDefault(new Pair<>(i,j),0.);
                p2resultProbabilities[i]+=p2probabilities.getOrDefault(new Pair<>(i,j),0.);
            }
            p1score+=i*p1resultProbabilities[i];
            p2score+=i*p2resultProbabilities[i];
        }

        for(int i=0;i<=gameLength;i++)
            for(int j=0;j<=gameLength;j++) {
                if(i<j)
                    p2winChance+=p1resultProbabilities[i]*p2resultProbabilities[j];
                if(i>j)
                    p1winChance+=p1resultProbabilities[i]*p2resultProbabilities[j];
                if(i==j)
                    drawChance+=p1resultProbabilities[i]*p2resultProbabilities[j];
            }


        System.out.println("Вероятность победы игрока 1 ~= " + (100. * p1winChance) + "%;");
        System.out.println("Вероятность победы игрока 2 ~= " + (100. * p2winChance) + "%;");
        System.out.println("Вероятность ничьи ~= " + (100. * drawChance) + "%;");
        System.out.println("Средний результат игрока 1 ~= " + (p1score) + " очков;");
        System.out.println("Средний результат игрока 2 ~= " + (p2score) + " очков;");
    }
}
