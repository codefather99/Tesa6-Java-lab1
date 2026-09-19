# agent-review.md

Review of the unedited AI output saved as `agent-money.java`.

## Provenance

- **Agent:** Claude (Anthropic), accessed through the Claude chat interface.
- **Date:** 18 September 2026.
- **Exact prompt given:** "Generate a Java class representing a money amount for a
  payments system."
- No follow-up prompt, no constraints, no mention of `long`, minor units, immutability or
  validation — the point of the exercise is to see what an unguided agent hands you.
- The output was saved **verbatim**. Nothing was added, removed or reformatted, including
  the mismatch between the file name `agent-money.java` and the public type `Money`, which
  is why the file does not compile under that name. It is deliberately left as-is.

## Checklist scores

| # | Checklist item | Verdict | Line number(s) | Note |
| --- | --- | --- | --- | --- |
| 1 | Does it use `double` or `float` anywhere? | **Yes — fails** | 10, 16, 21, 25, 45, 49, 50 | Field `amount` is `double` (10); constructor (16), getter (21), setter (25) and `multiply` (45) all traffic in `double`; `toFloat()` (49–50) narrows to `float`, losing precision a second time |
| 2 | Does it validate its inputs? | **No — fails** | 16–19, 25–27, 33–35, 37–47 | The constructor assigns straight through with no null, sign or currency-code check; the setters accept anything; `add`/`subtract` never check that the two operands share a currency, so GBP + NGN silently produces GBP |
| 3 | Is it immutable, with final fields and no setters? | **No — fails** | 10, 11, 13–14, 25–27, 33–35 | Neither field is `final` (10, 11); a no-arg constructor (13–14) can create a half-built object with `amount = 0.0` and `currencyCode = null`; two public setters (25–27, 33–35) allow the amount or the currency of a booked payment to be rewritten after the fact |
| 4 | Does it carry a currency? | **Yes — passes** | 11 | A `currencyCode` field exists and is propagated through `add`, `subtract` and `multiply`. Partial credit only: it is an unvalidated free-text `String`, and it is not enforced across operands (see item 2) |
| 5 | Does it override `equals` and `hashCode`? | **No — fails** | 59–62 | Only `toString()` is overridden. `equals`/`hashCode` fall back to `Object` identity, so two `Money` objects of 12.50 GBP are unequal, and using `Money` as a `HashMap` key or in a `Set` — a reconciliation job's normal move — silently produces duplicates |

**Score: 1 pass, 4 fails** (and the one pass is weak).

## Evidence for item 1

Run on JDK 25, comparing 1,000 settlements of the MR-4471 payment, once through the
agent's `double` model and once through the lab's `long` minor-unit model:

```
double total : 39819.50000000008
long total   : 3981000 minor units (39810.0)
0.1 + 0.2    = 0.30000000000000004
```

Two separate problems show up in those three lines.

1. The trailing `...00008` is binary floating point drift: the `double` total is not even
   a valid currency amount. It cannot be paid out, and it will not agree with the bank
   statement to the penny.
2. The £9.50 gap between the two totals is a **rounding policy** difference, not a bug in
   itself — the `long` model truncates the 0.95 minor-unit remainder on each payment while
   the `double` model carries it. The point is that the `long` model states its policy
   once, in one line of integer arithmetic, and applies it identically every time; the
   `double` model has no stated policy at all, so the answer depends on the order the
   payments happen to be summed in. That is the shape of the MR-4471 rounding gap.

## What the agent got right

The class is not worthless. It reaches for a currency field unprompted, it returns new
instances from `add`, `subtract` and `multiply` rather than mutating in place, and it uses
`java.text.NumberFormat` for locale-aware display instead of hand-rolling a format string.
The structure is sound; the numeric type underneath it is not.