# findings.md

## Domain types

| Type | Java declaration | Why this declaration | Precondition enforced |
| --- | --- | --- | --- |
| Payment | `public record Payment(String id, String merchantId, long amountMinor, String currency, PaymentStatus status)` | A captured payment is an immutable value, not an entity with a life of its own — its amount and currency are a finished fact. The record gives private final fields, accessors, and value-based `equals`/`hashCode`/`toString` from one declaration line, and value equality is what a reconciliation job needs. `amountMinor` is `long` so all fee arithmetic is exact integer arithmetic on minor units. | Compact constructor throws `IllegalArgumentException`, naming the field, when: `id` is null or blank; `merchantId` is null; `amountMinor` is zero or negative; `currency` is null or not exactly 3 characters; `status` is null. Because it is the canonical constructor, these also run on deserialization, so no invalid `Payment` can exist. |
| Merchant | `public class Merchant` with `private final String id`, `private String displayName`, `private String bankAccountRef` | A merchant is an entity with continuous identity: it is the same merchant after it renames itself or changes bank. That mutability rules out a record, whose components are final by definition. `id` is `final` so identity cannot be rewritten, while the two mutable fields stay private and are reachable only through `updateBankAccountRef`, giving one choke point for validation, audit logging or approval later. | Constructor rejects a null or blank `id`, `displayName` or `bankAccountRef`. `updateBankAccountRef` rejects a null or blank reference, so a merchant can never end up with no payout destination. `id` is `final`, so the compiler itself enforces that identity never changes. |
| PaymentStatus | `public enum PaymentStatus { RECEIVED, SETTLED, FAILED }` | The set of settlement states is closed and known at compile time. A `String` status would let `"recieved"` or `"SETTLED "` into the settlement run and fail only at payout; the enum makes those values unrepresentable, gives singleton constants that compare correctly with `==`, and lets the compiler check exhaustiveness in a `switch`. | The type itself is the precondition: only the three declared constants can ever be referenced, so no validation code is required. `Payment`'s compact constructor additionally rejects a null status, which is the one bad value an enum reference can still hold. |

## The single change I would make to the agent's money class

Replace the `double amount` field with a `long amountMinor` holding a whole count of minor
units, and make the field `final`. Everything else wrong with that class follows from this
one choice: the drifting totals, the lossy `toFloat()`, and the fact that no amount of
validation can recover a value that was already inexact when it was stored. Making the
field `final` at the same time removes the setters and the no-arg constructor, which
closes the mutability hole as a side effect. I would then still want input validation, a
currency check on `add` and `subtract`, and `equals`/`hashCode` before it went anywhere
near Ledger — but those are repairs I can make to a correct numeric representation,
whereas a `double` amount cannot be repaired after the fact. For the swapped-argument
defect, I would go one step further and wrap the identifiers in
`record PaymentId(String value)` and `record MerchantId(String value)`, which turns
Meera's mis-credited settlement from a runtime incident into a compile error.