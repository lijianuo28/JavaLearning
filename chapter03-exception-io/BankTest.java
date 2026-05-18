public class BankTest {
    public static void main(String[] args){
        try{
            BankAccount account1 = new BankAccount("liudazhuang", 50000);
            account1.deposit(300);
            account1.withdraw(1000);
            System.out.println("liudazhuang  "+ account1.getbalance());
        }
        catch(IllegalArgumentException ex){
            String msg = ex.getMessage();
            System.out.println(msg);
        }
        catch(InsufficientFundsException ex){
            System.out.println(ex);
        }

        try{
            BankAccount account2 = new BankAccount("zhangliang", -10000);
            account2.deposit(500);
            account2.withdraw(20000);
            System.out.println("zhangliang  " + account2.getbalance());
        }
        catch(IllegalArgumentException ex){
            String msg = ex.getMessage();
            System.out.println(msg);
        }
        catch(InsufficientFundsException ex){
            System.out.println(ex);
        }

        

    }

    public  static class BankAccount {
        private String accountNumber;
        private double balance;

        //构造方法
        BankAccount(String newAccountNumber, double newBalance) throws IllegalArgumentException{
            if(newBalance < 0){
                throw new IllegalArgumentException("Balance can't be negative.");
            } else {
                accountNumber = newAccountNumber;
                balance = newBalance;
            }
        }

    //————————————————实例方法————————————————

        //存款
        public double deposit(double amount) throws IllegalArgumentException{
            if(amount < 0){
                throw new IllegalArgumentException("Amount can't be negative.");
            }
            else{
                balance += amount;
            }
            return balance;
        }

        //取款
        public double withdraw(double amount) throws InsufficientFundsException{
            if(amount < 0){
                throw new IllegalArgumentException("Amount can't be negative.");
            } else if(amount > balance){
                throw new InsufficientFundsException(amount);
            } else{
                balance -= amount;
            }
            return balance;
        }

        //输出余额
        public double getbalance(){
            return balance;
        }

        @Override
        public String toString(){
           return String.format("Your accountNumber: " + accountNumber + "\nYour balance: %.2f", balance);
        }
    }

    //自定义异常
    public static class InsufficientFundsException extends Exception{
        public InsufficientFundsException (double amount) {
            super("Insufficient fund exception: " + amount);
        }
    }

}

