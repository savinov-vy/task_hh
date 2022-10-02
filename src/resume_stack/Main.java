package resume_stack;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        List<Resume> resumeFirstStack = new ArrayList<>(10000);
        List<Resume> resumeSecondStack = new ArrayList<>(10000);

        Scanner sc = new Scanner(System.in);
//        Scanner sc = new Scanner(new FileReader("test1.txt"));

        String line = sc.nextLine();
        List<Integer> terms = toIntArr(line);
        int maxSalaryLimit = terms.get(2);

        fillResumeStacks(resumeFirstStack, resumeSecondStack, sc, maxSalaryLimit, terms);

        short result = getMaxCountResumeBySum(resumeFirstStack, resumeSecondStack, maxSalaryLimit);
        System.out.println(result);
    }

    private static void fillResumeStacks(List<Resume> resumeFirstStack, List<Resume> resumeSecondStack,
                                         Scanner sc, int maxSalaryLimit,  List<Integer> terms) {
        char salaryStackSize = (char) Math.max(terms.get(0), terms.get(1));
        int sumSalaryInFirstStack = 0;
        int sumSalaryInSecondStack = 0;
        String line;
        for (short i = 1; i <= salaryStackSize; i++) {
            line = sc.nextLine();
            List<Integer> salaries = toIntArr(line);
            Integer salaryFirstStack = salaries.get(0);
            if (salaryFirstStack != null) {
                sumSalaryInFirstStack += salaryFirstStack;
                if (sumSalaryInFirstStack <= maxSalaryLimit) {
                    resumeFirstStack.add(new Resume(i, salaryFirstStack, sumSalaryInFirstStack));
                }
            }
            Integer salarySecondStack = salaries.get(1);
            if (salarySecondStack != null) {
                sumSalaryInSecondStack += salarySecondStack;
                if (sumSalaryInSecondStack <= maxSalaryLimit) {
                    resumeSecondStack.add(new Resume(i, salarySecondStack, sumSalaryInSecondStack));
                }
            }
        }
    }

    private static short getMaxCountResumeBySum(List<Resume> resumesFirstStack,
                                                List<Resume> resumesSecondStack,
                                                int maxSalaryLimit) {
        short result = (short) Math.max(resumesFirstStack.size(), resumesSecondStack.size());
        for (Resume resumeFirstStack : resumesFirstStack) {
            int sumFromFirstStack = resumeFirstStack.getSumSalaryInStack();
            short numberOfStack1 = resumeFirstStack.getNumberOfStack();
            for (Resume resumeSecondStack : resumesSecondStack) {
                int sumFromSecondStack = resumeSecondStack.getSumSalaryInStack();
                short numberOfStack2 = resumeSecondStack.getNumberOfStack();
                int resultSumSalary = sumFromFirstStack + sumFromSecondStack;
                short sumAmountResumes = (short) (numberOfStack1 + numberOfStack2);
                if (resultSumSalary <= maxSalaryLimit && sumAmountResumes > result) {
                    result = sumAmountResumes;
                }
            }
        }
        return result;
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
}

class Resume {
    short numberOfStack;
    int salary;
    int sumSalaryInStack;

    public Resume(short numberOfStack, int value, int sumSalaryInStack) {
        this.numberOfStack = numberOfStack;
        this.salary = value;
        this.sumSalaryInStack = sumSalaryInStack;
    }

    public short getNumberOfStack() {
        return numberOfStack;
    }

    public int getSalary() {
        return salary;
    }

    public int getSumSalaryInStack() {
        return sumSalaryInStack;
    }
}