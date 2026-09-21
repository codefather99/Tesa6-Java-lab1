/**
 * Works out what Ledger keeps and what a merchant is owed.
 *
 * All arithmetic is long arithmetic on minor units. A basis point is one
 * hundredth of a percent, so 310 basis points is 3.10 percent and the divisor
 * is 10000. Integer division truncates towards zero, which means the fee is
 * always rounded down and the merchant is never short-changed by rounding.
 */
public class SettlementCalculator {

    private static final int BASIS_POINT_DIVISOR = 10_000;

    /**
     * The fee Ledger charges, in minor units.
     */
    public static long feeMinor(long amountMinor, int rateBasisPoints) {
        if (amountMinor < 0) {
            throw new IllegalArgumentException(
                    "amountMinor must not be negative, was: " + amountMinor);
        }
        if (rateBasisPoints < 0 || rateBasisPoints > BASIS_POINT_DIVISOR) {
            throw new IllegalArgumentException(
                    "rateBasisPoints must be between 0 and 10000, was: " + rateBasisPoints);
        }
        return amountMinor * rateBasisPoints / BASIS_POINT_DIVISOR;
    }

    /**
     * What the merchant is owed after the fee, in minor units.
     */
    public static long netMinor(long amountMinor, int rateBasisPoints) {
        if (amountMinor < 0) {
            throw new IllegalArgumentException(
                    "amountMinor must not be negative, was: " + amountMinor);
        }
        if (rateBasisPoints < 0 || rateBasisPoints > BASIS_POINT_DIVISOR) {
            throw new IllegalArgumentException(
                    "rateBasisPoints must be between 0 and 10000, was: " + rateBasisPoints);
        }
        return amountMinor - feeMinor(amountMinor, rateBasisPoints);
    }

    public static void main(String[] args) {
        Payment payment = new Payment(
                "PAY-90312",
                "MR-4471",
                128450L,
                "GBP",
                PaymentStatus.RECEIVED);

        int rateBasisPoints = 310;

        System.out.println(payment);
        System.out.println("fee: " + feeMinor(payment.amountMinor(), rateBasisPoints));
        System.out.println("net: " + netMinor(payment.amountMinor(), rateBasisPoints));
    }
}