package resume_stack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<Integer> terms = dataFromLine();

        List<Integer> salariesFirstStack = new ArrayList<>();
        List<Integer> salariesSecondStack = new ArrayList<>();
        fillStackSalary(terms, salariesFirstStack, salariesSecondStack);
        Integer maxSalaryLimit = terms.get(2);

        Map<Integer, Integer> amountBySumFromFirstStack = getAmountResumeBySum(salariesFirstStack, maxSalaryLimit);
        Map<Integer, Integer> amountBySumFromSecondStack = getAmountResumeBySum(salariesSecondStack, maxSalaryLimit);
        Integer result = getMaxCountResumeBySum(amountBySumFromFirstStack, amountBySumFromSecondStack, maxSalaryLimit);
        System.out.println(result);
    }

    private static Integer getMaxCountResumeBySum(Map<Integer, Integer> amountBySumFromFirstStack,
                                                  Map<Integer, Integer> amountBySumFromSecondStack,
                                                  Integer maxSalaryLimit) {
        int result = Math.max(amountBySumFromFirstStack.size(), amountBySumFromSecondStack.size());
        for (int amountFirstStack = 1; amountFirstStack <= amountBySumFromFirstStack.size(); amountFirstStack++) {
            Integer sumFromFirstStack = amountBySumFromFirstStack.get(amountFirstStack);
            for (int amountSecondStack = 1; amountSecondStack <= amountBySumFromSecondStack.size(); amountSecondStack++) {
                Integer sumFromSecondStack = amountBySumFromSecondStack.get(amountSecondStack);
                int resultSumSalary = sumFromFirstStack + sumFromSecondStack;
                int resultAmount = amountFirstStack + amountSecondStack;
                if (resultSumSalary <= maxSalaryLimit && resultAmount > result) {
                    result = resultAmount;
                }
            }
        }
        return result;
    }

    private static Map<Integer, Integer> getAmountResumeBySum(List<Integer> salariesStack, Integer maxSalaryLimit) {
        int sumSalary = 0;
        Map<Integer, Integer> amountResumeBySumSalary = new HashMap<>();
        for (int amountResume = 1; sumSalary <= maxSalaryLimit && amountResume <= salariesStack.size(); amountResume++) {
            Integer salary = salariesStack.get(amountResume - 1);
            sumSalary += salary;
            if (sumSalary <= maxSalaryLimit) {
                amountResumeBySumSalary.put(amountResume, sumSalary);
            }
        }
        return amountResumeBySumSalary;
    }

    private static void fillStackSalary(List<Integer> terms, List<Integer> salariesFirstStack,
                                        List<Integer> salariesSecondStack) {
        int amountLineSalaries = Math.max(terms.get(0), terms.get(1));
        for (int i = 0; i < amountLineSalaries; i++) {
            List<Integer> salariesLine = dataFromLine();
            Integer salaryFirstStack = salariesLine.get(0);
            if (salaryFirstStack != null) {
                salariesFirstStack.add(salaryFirstStack);
            }
            Integer salarySecondStack = salariesLine.get(1);
            if (salarySecondStack != null) {
                salariesSecondStack.add(salarySecondStack);
            }
        }
    }

    private static List<Integer> dataFromLine() {
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        String[] dataArr = line.split(" ");
        return Arrays.stream(dataArr)
                .map(data -> isNumber(data) ? Integer.parseInt(data) : null)
                .collect(Collectors.toList());
    }

    private static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }
}