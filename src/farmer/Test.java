package farmer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {
    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal(1);
        BigDecimal bigDecimal2 = new BigDecimal(3);
        BigDecimal bigDecimal3 = bigDecimal.divide(bigDecimal2, 6, RoundingMode.DOWN);
        System.out.println(bigDecimal3);
    }
}
