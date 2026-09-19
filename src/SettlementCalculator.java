/**
  Works out what Ledger keeps and what a merchant is owed.

  All arithmetic is long arithmetic on minor units. A basis point is one
  hundredth of a percent, so 310 basis points is 3.10 percent and the divisor
  is 10000. Integer division truncates towards zero, which means the fee is
   always rounded down and the merchant is never short-changed by rounding.
 */

public class SettlementCalculator {

    private static final int BASIS_POINT_DIVISOR = 10000;

    public static long feeMinor(long amountMinor, int rateBasisPoints){
        if (amountMinor < 0 ){
            throw new IllegalArgumentException("amountMinor must not be negative");
        }

        if (rateBasisPoints < 0 || rateBasisPoints > BASIS_POINT_DIVISOR){
            throw new IllegalArgumentException("rateBasisPoints must be between 0 and 10000");
        }

        return amountMinor * rateBasisPoints / BASIS_POINT_DIVISOR;
    }

    public static long netMinor(long amountMinor, int rateBasisPoints){
        if (amountMinor < 0){
            throw new IllegalArgumentException("amount minor must not be negative");
        }

        if (rateBasisPoints < 0 || rateBasisPoints > BASIS_POINT_DIVISOR){
            throw new IllegalArgumentException("rateBasisPoints must be between 0 and 10000");
        }

        return amountMinor - feeMinor(amountMinor, rateBasisPoints);
    }

    public static void main(String[] args){

        Payment payment1 = new Payment("PAY-90312", "MR-4471", 128450, "GBP", PaymentStatus.RECEIVED);
        System.out.println("Payment Made: " + payment1.amountMinor());

        long fee = feeMinor(payment1.amountMinor(), 310);
        System.out.println("Fee at 310 basis points: " + fee);

        long netMoney = netMinor(payment1.amountMinor(), 310);
        System.out.println("The net amount: " + netMoney);

    }
}
