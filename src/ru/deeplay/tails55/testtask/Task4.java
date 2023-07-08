package ru.deeplay.tails55.testtask;

import java.util.*;

// В худшем случае выходит примерно K^Count, что печально, но не думаю, что эта задача полиномиально разрешима в принципе
// а в плане оптимизации ничего лучше пары эвристик в голову не приходит
// В любом случае, я так понял, "тема" этой задачи это рекурсии, так что вот рекурсия)
public class Task4 {
    // Сначала идут все отрицательные элементы в порядке убывания (тут порядок не важен), потом все неотрицательные в нём же (тут важен)
    private static final Comparator<Long> comparator = (a, b) -> {
        if ((a < 0) == (b < 0)) // Если оба неотрицательные либо оба отрицательные
            return b.compareTo(a); // Сортируем по убыванию
        return a.compareTo(b); // Иначе по возрастанию (отрицательный слева)
    };

    public static boolean recursiveSolve(long[] values, int k, long minSum, long[] sums, int[] input, int currentStep) {
        //Массив заполнен, подводим итоги
        if (currentStep == values.length) {
            for (int i = 0; i < k; i++) {
                if (sums[i] != minSum + i)
                    return false;
                return true;
            }
        }
        long currentValue = values[currentStep];
        for (int i = 0; i < k; i++) {
            // values поступит сюда уже отсортированным, если отрицательные элементы уже кончились, понизить сумму до допустимой уже не выйдет
            if (sums[i] + currentValue > minSum + i && (currentStep == values.length - 1 || values[currentStep + 1] >= 0))
                continue;
            // Готовим к следующему шагу
            input[currentStep] = i;
            sums[i] += currentValue;
            // Если нашли решение, то возвращаем его наверх
            if (recursiveSolve(values, k, minSum, sums, input, currentStep + 1)) {
                return true;
            } else {
                //Откатываем правки выше
                input[currentStep] = -1;
                sums[i] -= currentValue;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Укажите количество чисел в массиве:");
        int count = scanner.nextInt();
        if (count <= 0) {
            System.out.println("В массиве должно быть не менее 1 числа!");
            return;
        }

        System.out.println("Введите числа, входящие в массив:");
        long[] values = new long[count];
        for (int i = 0; i < count; i++)
            values[i] = scanner.nextLong();

        System.out.println("Введите число подгрупп K:");
        int k = scanner.nextInt();
        if (k <= 0) {
            System.out.println("Число K должно быть не менее 1!");
            return;
        } else if (k > count + 1) { // k=count+1 возможен, например, при разбиении [-1; 1] на 3 группы с суммами -1, 0 и 1
            System.out.println("Разбиение невозможно: число K выше размера массива +1!");
            return;
        }
        long sum = 0;
        for (int i = 0; i < count; i++)
            sum += values[i];
        // Эвристика по остатку от деления суммы всех чисел на k
        if ((sum % k + k) % k != (k % 2 != 0 ? 0 : k / 2)) { // При нечётном k остаток от деления суммы всех чисел на k равен 0, при чётном - k/2
            System.out.println("Разбиение невозможно: сумма имеет некорректный отстаток от деления на K!");
            return;
        }
        long minSum = (sum - k * (k - 1) / 2) / k; // Сумма первой подгруппы

        // Сортируем массив значений для ещё одной эвристики в решателе
        // Раньше использовался просто Arrays.sort(values), но такой компаратор, как мне кажется, лучше
        values = Arrays.stream(values).boxed().sorted(comparator).mapToLong(i -> i).toArray();

        // Будущее сопоставление числа подгруппе, -1 - плейсхолдер для ещё не включенных чисел
        int[] input = new int[count];
        for (int i = 0; i < count; i++)
            input[i] = -1;
        // Суммы чисел в подгруппах, чтобы не пересчитывать каждый раз, в найденном решении сумма чисел i-й подгруппы должна быть равна minSum+i
        long[] sums = new long[k];
        for (int i = 0; i < k; i++)
            sums[i] = 0;
        if (recursiveSolve(values, k, minSum, sums, input, 0)) {
            System.out.print("Решение найдено!");
            //Собираем по values и input сами подгруппы
            List<Long>[] subArrays = new List[k];
            for (int i = 0; i < k; i++) {
                subArrays[i] = new ArrayList<>();
            }
            for (int i = 0; i < count; i++)
                subArrays[input[i]].add(values[i]);
            for (int i = 0; i < k; i++) {
                System.out.print("\nПодгруппа " + i + ": ");
                for (long value : subArrays[i])
                    System.out.print(value + " ");
                System.out.print("сумма " + (minSum + i));
            }
        } else {
            System.out.println("Решения не существует!");
        }
    }
}
