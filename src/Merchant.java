// Merchant is a class and not a record because it has a mutable identity: the same
// merchant keeps its id for life while its display name and bank account reference
// change over time, whereas a Payment is a finished fact whose values must never
// change once captured.

/**
 * A business that Ledger settles money to.
 *
 * The id is private final: it identifies the merchant and is fixed at construction.
 * displayName and bankAccountRef are private and non-final because they legitimately
 * change during the merchant's life, and they are only reachable through methods, so
 * every change goes through one place that can later log or validate it.
 */
public class Merchant {

    private final String id;
    private String displayName;
    private String bankAccountRef;

    public Merchant(String id, String displayName, String bankAccountRef) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be null or blank");
        }
        if (bankAccountRef == null || bankAccountRef.isBlank()) {
            throw new IllegalArgumentException("bankAccountRef must not be null or blank");
        }
        this.id = id;
        this.displayName = displayName;
        this.bankAccountRef = bankAccountRef;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBankAccountRef() {
        return bankAccountRef;
    }

    /**
     * Points this merchant's payouts at a different bank account reference.
     * The guard is here and not at the call site so that no caller can leave a
     * merchant with a missing payout destination.
     */
    public void updateBankAccountRef(String newBankAccountRef) {
        if (newBankAccountRef == null || newBankAccountRef.isBlank()) {
            throw new IllegalArgumentException(
                    "bankAccountRef must not be null or blank");
        }
        this.bankAccountRef = newBankAccountRef;
    }

    @Override
    public String toString() {
        return "Merchant[id=" + id
                + ", displayName=" + displayName
                + ", bankAccountRef=" + bankAccountRef + "]";
    }
}