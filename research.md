# research.md

Lab: sjv-l0-1 — Modelling the Ledger settlement domain in Java
Author: Henry Emefo

---

## 1. Toolchain confirmation

Output of `javac --version`:

```
javac 25.0.1
```

Output of `java --version`:

```
java 25.0.1 2025-10-21 LTS
Java(TM) SE Runtime Environment (build 25.0.1+8-LTS-27)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.1+8-LTS-27, mixed mode, sharing)
```

Compiled and run with no build tool:

```
javac -d out src/*.java
java -cp out SettlementCalculator
```

---

## 2. The three printed run lines

`SettlementCalculator.main` builds payment `PAY-90312` for merchant `MR-4471`,
amount `128450` minor units, currency `GBP`, status `RECEIVED`, and prints:

```
Payment[id=PAY-90312, merchantId=MR-4471, amountMinor=128450, currency=GBP, status=RECEIVED]
fee: 3981
net: 124469
```

**Expected values confirmed.** Fee prints as `3981` and net prints as `124469`.

Arithmetic check, done entirely in `long`:

- 128450 × 310 = 39,819,500
- 39,819,500 ÷ 10000 = 3981 (integer division truncates the remainder 9,500, i.e. the
  exact fee of 3981.95 minor units is rounded **down**, so Ledger never over-charges)
- 128450 − 3981 = 124469

The first line is the record's compiler-generated `toString()`; nothing was hand-written
for it.

---

## 3. Precondition evidence

Each guard was exercised once. Actual thrown messages:

```
blank id          -> id must not be null or blank
null merchantId   -> merchantId must not be null
zero amount       -> amountMinor must be greater than zero, was: 0
bad currency      -> currency must be a 3 letter code, was: GB
negative amount   -> amountMinor must not be negative, was: -1
rate out of range -> rateBasisPoints must be between 0 and 10000, was: 10001
```

Every message names the offending field, so a production failure identifies the bad
input without a debugger.

Note on Meera's second defect — the settlement credited to the wrong merchant because
two `String` arguments were swapped. The compact constructor **cannot** catch that: `id`
and `merchantId` are both `String`, so `new Payment("MR-4471", "PAY-90312", ...)`
compiles and passes every guard. Only a type change fixes it (see findings.md).

---

## 4. Primary documentation research

Source: the official Java learning documentation at https://dev.java/learn/.

**Page title: "Using Records to Model Immutable Data"** (https://dev.java/learn/records/)

On what a record generates for you:

> "the compiler creates a private final field with the same name as this component"

The same page lists the rest of what is generated from the single declaration line: one
accessor method per component, the canonical constructor, and overrides of `toString()`,
`equals()` and `hashCode()`. This is why `Payment` needs no hand-written accessors and why
its printed line above is correct without any code of mine.

**Page title: "Creating Primitive Type Variables in Your Programs"**
(https://dev.java/learn/language-basics/primitive-types/)

On the range and behaviour of a primitive numeric type:

> "The long data type is a 64-bit two's complement integer."

The page gives the signed range as −2^63 to 2^63−1. That ceiling is roughly 9.22 × 10^18
minor units — about £92 quadrillion in pence — so `long` cannot realistically overflow on
a single Ledger settlement, while giving exact whole-unit arithmetic. The same page warns
that the floating point types should never be used for precise values such as currency,
and points to `java.math.BigDecimal` where a decimal type is genuinely needed.

---

## 5. Why this matters for the two reported defects

| Defect | Cause | What this lab's model does about it |
| --- | --- | --- |
| Rounding gap on MR-4471 | Fee arithmetic in binary floating point | `amountMinor` is `long`; all fee maths is integer maths, with one documented rounding rule (truncate down) |
| Settlement credited to the wrong merchant | Two `String` arguments swapped at a call site | Not yet fixed — guards cannot distinguish two `String`s. Needs `record PaymentId(String value)` and `record MerchantId(String value)` so the compiler rejects the swap |