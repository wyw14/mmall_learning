package mmall.commons;

public class Const {


    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface role {
        int ROLE_CUSTOMER = 0;//代表普通用户
        int ROLE_ADMIN = 1;//代表管理员

    }

    public interface cart {
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum OrderStatusEnum {
        CANCELED(0,"已取消"),
        NO_PAY(10,"未付款"),
        PAID(20,"已付款"),
        SHIPPINGEN(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭")
        ;
        private String value;
        private int code;

        OrderStatusEnum(int code,String value){
            this.code=code;
            this.value=value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }

    }
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY="WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS="TRADE_SUCCESS";

        String RESPONSE_SUCCESS="success";
        String RESPONSE_Failed="failed";
    }
    public enum paymentEnun{
        online_payment(1,"在线支付");
        private String value;
        private int code;

        paymentEnun(int code,String value){
            this.code=code;
            this.value=value;
        }
        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
        public static paymentEnun codeOf(int code){
            for(paymentEnun paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }


    }
    public enum sellStatus{
        ONSELL(1,"正在售卖"),
        UNSELL(0,"已经下架");
        private String value;
        private int code;

        sellStatus(int code,String value){
            this.code=code;
            this.value=value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");
        private String value;
        private int code;

        PayPlatformEnum(int code,String value){
            this.code=code;
            this.value=value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
