package mmall.util;

import java.math.BigDecimal;

public class BigDecimalUtils {
    private BigDecimalUtils(){
    }
    public static BigDecimal add(Double a,Double b){
        BigDecimal v1=new BigDecimal(a.toString());
        BigDecimal v2=new BigDecimal(b.toString());
        return v1.add(v2);
    }
    public static BigDecimal sub(Double a,Double b){
        BigDecimal v1=new BigDecimal(a.toString());
        BigDecimal v2=new BigDecimal(b.toString());
        return v1.subtract(v2);
    }
    public static BigDecimal mul(Double a,Double b){
        BigDecimal v1=new BigDecimal(a.toString());
        BigDecimal v2=new BigDecimal(b.toString());
        return v1.multiply(v2);
    }
    public static BigDecimal div(Double a,Double b){
        BigDecimal v1=new BigDecimal(a.toString());
        BigDecimal v2=new BigDecimal(b.toString());
        return v1.divide(v2,2,BigDecimal.ROUND_HALF_UP);
    }
}
