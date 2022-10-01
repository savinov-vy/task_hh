package resume_stack;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        List<Integer> salariesFirstStack = new ArrayList<>(10000);
        List<Integer> salariesSecondStack = new ArrayList<>(10000);

//        Scanner sc = new Scanner(System.in);
        Scanner sc = new Scanner(new FileReader("test.txt"));

        String line = sc.nextLine();
        List<Integer> terms = toIntArr(line);
        int maxSalaryLimit = terms.get(2);
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            List<Integer> salaries = toIntArr(line);
            Integer salaryFirstStack = salaries.get(0);
            if (salaryFirstStack != null) {
                salariesFirstStack.add(salaryFirstStack);
            }
            Integer salarySecondStack = salaries.get(1);
            if (salarySecondStack != null) {
                salariesSecondStack.add(salarySecondStack);
            }
        }


        Map<Short, Integer> amountBySumFromFirstStack = getAmountResumeBySum(salariesFirstStack, maxSalaryLimit);
        Map<Short, Integer> amountBySumFromSecondStack = getAmountResumeBySum(salariesSecondStack, maxSalaryLimit);
        short result = getMaxCountResumeBySum(amountBySumFromFirstStack, amountBySumFromSecondStack, maxSalaryLimit);
        System.out.println(result);
        long stop = System.currentTimeMillis();
        System.out.println("mills: " + (stop - start));
    }

    private static short getMaxCountResumeBySum(Map<Short, Integer> amountBySumFromFirstStack,
                                                  Map<Short, Integer> amountBySumFromSecondStack,
                                                  Integer maxSalaryLimit) {
        short result = (short) Math.max(amountBySumFromFirstStack.size(), amountBySumFromSecondStack.size());
        for (short amountFirstStack = 1; amountFirstStack <= amountBySumFromFirstStack.size(); amountFirstStack++) {
            Integer sumFromFirstStack = amountBySumFromFirstStack.get(amountFirstStack);
            for (short amountSecondStack = 1; amountSecondStack <= amountBySumFromSecondStack.size(); amountSecondStack++) {
                Integer sumFromSecondStack = amountBySumFromSecondStack.get(amountSecondStack);
                int resultSumSalary = sumFromFirstStack + sumFromSecondStack;
                short resultAmount = (short) (amountFirstStack + amountSecondStack);
                if (resultSumSalary <= maxSalaryLimit && resultAmount > result) {
                    result = resultAmount;
                }
                sayHeap(63);
            }
        }
        return result;
    }

    private static Map<Short, Integer> getAmountResumeBySum(List<Integer> salariesStack, Integer maxSalaryLimit) {
        int sumSalary = 0;
        Map<Short, Integer> amountResumeBySumSalary = new HashMap<>(10000);
        for (short amountResume = 1; sumSalary <= maxSalaryLimit && amountResume <= salariesStack.size(); amountResume++) {
            Integer salary = salariesStack.get(amountResume - 1);
            sumSalary += salary;
            if (sumSalary <= maxSalaryLimit) {
                amountResumeBySumSalary.put(amountResume, sumSalary);
            }
        }
        return amountResumeBySumSalary;
    }

    private static List<Integer> toIntArr(String line) {
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

    private static void sayHeap(int numLine) {
        long usedBytes = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        System.out.println("line number: " + numLine +" used heap: "+ usedBytes/1048576);
    }
}