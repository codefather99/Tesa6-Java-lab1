LAB: sjv-l0-1 — Modelling the Ledger settlement domain in Java

Toolchain:
javac 25.0.1
java 25.0.1 2025-10-21 LTS
Java(TM) SE Runtime Environment (build 25.0.1+8-LTS-27)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.1+8-LTS-27, mixed mode, sharing)

Built: PaymentStatus (enum), Payment (record + compact constructor guards),
Merchant (class, mutable bankAccountRef), SettlementCalculator (fee/net + main).

Run output (SettlementCalculator.main):
Payment Made: 128450
Fee at 310 basis points: 3981
The net amount: 124469
(confirmed against spec: fee 3981, net 124469)

Precondition checks (each guard fired once):
blank id          -> id must not be null or blank
null merchantId   -> merchantId must not be null
zero amount       -> amountMinor must be greater than zero, was: 0
bad currency      -> currency must be a 3 letter code, was: GB
negative amount   -> amountMinor must not be negative, was: -1
rate out of range -> rateBasisPoints must be between 0 and 10000, was: 10001

Documentation research (dev.java/learn):
- "Using Records to Model Immutable Data" — records generate a private final field
  per component, a canonical constructor, and toString/equals/hashCode.
  
- "Creating Primitive Type Variables in Your Programs" — long is a 64-bit two's
  complement integer; float/double are explicitly warned off for currency.

AI-agent audit (agent-money.java, unedited output from prompt "Generate a Java
class representing a money amount for a payments system"):
1. Uses double/float?          YES — fails (double field, float toFloat())
2. Validates its inputs?       NO  — fails (no null/sign/currency checks)
3. Immutable, final, no setters? NO — fails (mutable fields, two public setters)
4. Carries a currency?         YES — passes (weak: unvalidated free string)
5. Overrides equals/hashCode?  NO  — fails (only toString overridden)
Score: 1 pass / 4 fails.

Evidence of the double-drift defect (1000 settlements of the MR-4471 payment):
double total : 39819.50000000008
long total   : 3981000 minor units (39810.0)
0.1 + 0.2    = 0.30000000000000004

Single change I'd make to the agent's class: replace `double amount` with a final
`long amountMinor`. Everything else (validation, currency check, equals/hashCode)
is repairable afterwards; the double corrupts the value before any of that helps.
