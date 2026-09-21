/**
 * A single payment taken by Ledger on behalf of a merchant.
 *
 * Payment is a record because it is an immutable value: once a payment has been
 * captured, its id, merchant, amount and currency never change. The compiler
 * generates the private final fields, the accessors, and equals/hashCode/toString.
 *
 * amountMinor is a long count of minor units (pence for GBP, kobo for NGN), never
 * a double. Binary floating point cannot represent 0.10 exactly, so repeated
 * fee arithmetic on a double drifts - that drift is the rounding gap Meera saw
 * on MR-4471.
 */
public record Payment(
        String id,
        String merchantId,
        long amountMinor,
        String currency,
        PaymentStatus status) {

    /**
     * Compact constructor. It runs before the compiler-generated field
     * assignments, so an invalid Payment can never be constructed. Each guard
     * names the offending field in its message, so a failure in production
     * points straight at the bad input.
     */
    public Payment {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("merchantId must not be null");
        }
        if (amountMinor <= 0) {
            throw new IllegalArgumentException(
                    "amountMinor must be greater than zero, was: " + amountMinor);
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException(
                    "currency must be a 3 letter code, was: " + currency);
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }
}