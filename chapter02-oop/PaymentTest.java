public class PaymentTest {
    public static void main(String[] args){
        Payment[] payments = new Payment[2];
        payments[0] = new CreditCardPayment(300, "1234-5678-9012-3456");
        payments[1] = new AliPayPayment(200, "user123");

        for(Payment payment: payments){
            payment.pay();
            payment.printReceipt();

            if(payment instanceof Refundable){
                Refundable refundablePayment = (Refundable) payment;
                try {
                    refundablePayment.refund(500);
                } catch (IllegalArgumentException e) {
                    System.out.println("退款失败: " + e.getMessage());
                }
            }

        }
    }

    public static abstract class Payment {
        protected double amount;

        protected Payment(double newAmount){
            amount = newAmount;
        }

        public abstract void pay();

        public void printReceipt(){
            System.out.println("支付金额：" + amount + "元"); 
        }
    }

    public static class CreditCardPayment extends Payment implements Refundable{
        private String cardNumber;

        public CreditCardPayment(double newAmount, String newCardNumber){
            super(newAmount);
            cardNumber = newCardNumber.substring(newCardNumber.length() - 4);

        }

        public void pay(){
            System.out.println("信用卡支付 (尾号 " + cardNumber + "): " + this.amount + "元");
        }

        @Override
        public void refund(double refundAmount) throws IllegalArgumentException {
            if (refundAmount <= 0) {
                throw new IllegalArgumentException("退款金额必须大于零");
            } else if(refundAmount > this.amount) {
                throw new IllegalArgumentException("退款金额不能超过支付金额");
            }
            System.out.println("信用卡退款: " + refundAmount + "元");
        }
    }

    public static class AliPayPayment extends Payment{
        private String account;

        public AliPayPayment (double newAmount, String newAccount){
            super(newAmount);
            this.account = newAccount;
        }

        public void pay() {
            System.out.println("支付宝(账号 " + account + " )支付 : " + this.amount + "元");
        }
    }

    public interface Refundable {
        void refund(double refundAmount);
    }


}
