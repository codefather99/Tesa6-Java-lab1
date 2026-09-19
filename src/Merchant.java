/**
 Merchant is a class and not a record because it has a mutable identity: the same
 merchant keeps its id for life while its display name and bank account reference
 change over time, whereas a Payment is a finished fact whose values must never
 change once captured.
*/
public class Merchant {
    private final String id;

    private String displayName;

    private String bankAccountRef;

    public Merchant(String id, String displayName, String bankAccountRef){
        this.id = id;
        this.displayName = displayName;
        this.bankAccountRef = bankAccountRef;
    }

    public String getID(){
        return id;
    }

    public String getDisplayName(){
        return displayName;
    }

    public String getBankAccountRef(){
        return bankAccountRef;
    }

    /**
      Points this merchant's payouts at a different bank account reference.
      The guard is here and not at the call site so that no caller can leave a
      merchant with a missing payout destination.
     */
    public void updateBankAccountREf(String bankAccountRef){
        if (bankAccountRef == null || bankAccountRef.isBlank()) {
            throw new IllegalArgumentException(
                    "bankAccountRef must not be null or blank");
        }

        this.bankAccountRef = bankAccountRef;
    }


}
