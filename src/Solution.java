import java.util.Random;

public class Solution {

    static Random random = new Random();

    //Матрица вероятностей переходов
    private final static double[][] transitionMatrix = {
            {0.4, 0, 0, 0, 0.5, 0.1},
            {0, 0.1, 0.5, 0.2, 0, 0.2},
            {0, 0.3, 0.3, 0.3, 0.1, 0},
            {0, 0.1, 0.5, 0.3, 0.1, 0},
            {0, 0, 0.3, 0.2, 0.1, 0.4},
            {0, 0.1, 0.3, 0, 0.4, 0.2}
    };

    //Матрица количества шагов
    private final static int[] steps = {10, 50, 100, 1000};

    //Матрица состояний
    private final static int[] startingStates = {1, 2, 3, 4, 5, 6};

    //Алгоритм составления траектории
    private static void move(int startState, int steps) {
        int currentState = startState;
        System.out.print("Траектория: " + currentState + " ");

        for (int i = 0; i < steps; i++) {
            double[] probabilities = transitionMatrix[currentState-1];
            double randomValue = random.nextDouble();
            double cumulativeProbability = 0;
            int nextState = 0;
            for (int j = 0; j < probabilities.length; j++) {
                cumulativeProbability += probabilities[j];
                if (randomValue <= cumulativeProbability) {
                    nextState = j+1;
                    break;
                }
            }
            currentState = nextState;
            System.out.print(currentState + " ");
        }
    }

    public static void main(String[] args) {
        //Составляем траекторию
        for (int step : steps) {
            System.out.println();
            System.out.println("-------------------------------------------");
            System.out.println("Количество шагов: " + step);

            int randomStartIndex = random.nextInt(startingStates.length);
            int startState = startingStates[randomStartIndex];
            System.out.println("Начальное состояние: " + startState);
            move(startState, step);
        }
        System.out.println();
    }

}
