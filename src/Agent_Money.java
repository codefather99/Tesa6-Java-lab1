import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Represents a money amount in a payments system.
 */
public class Agent_Money {

    private double amount;
    private String currencyCode;

    public Agent_Money() {
    }

    public Agent_Money(double amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Agent_Money add(Agent_Money other) {
        return new Agent_Money(this.amount + other.amount, this.currencyCode);
    }

    public Agent_Money subtract(Agent_Money other) {
        return new Agent_Money(this.amount - other.amount, this.currencyCode);
    }

    public Agent_Money multiply(double factor) {
        return new Agent_Money(this.amount * factor, this.currencyCode);
    }

    public float toFloat() {
        return (float) amount;
    }

    public String format() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setCurrency(Currency.getInstance(currencyCode));
        return formatter.format(amount);
    }

    @Override
    public String toString() {
        return amount + " " + currencyCode;
    }
}