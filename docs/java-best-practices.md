# Java best practices for Project Nublar

Apply this checklist to all Java that you write or review in this repo. An agent that hands Java
work to a subagent passes this file on. It merges six sources (listed at the end) and resolves
their conflicts for this codebase. "EJ n" is Effective Java item n.

## Precedence

1. The four code quality standards and the other Global constraints of the plan
   (`docs/plan/engineering-backlog.md`) win over everything here.
2. The surrounding code in the same file or package wins over the external guides when they
   disagree on form (layout, naming idiom, comment density).
3. This checklist fills the gaps.
4. "Does its best": apply every rule to new and changed code. Do not reformat or rewrite untouched
   code for style points only; fix a rule break in old code only when you already touch it.

## Conflicts between the sources, resolved

| Topic | Oracle 1999 | Google | This repo |
| --- | --- | --- | --- |
| Indent | 4 spaces | 2 spaces | **4 spaces, never tabs** |
| Continuation indent | 8 spaces, or align | +4 minimum | **8 spaces, or align with the open bracket** (both in use) |
| Line length | 80 | 100 | **100** for new lines; do not rewrap old lines |
| Where to declare locals | top of block | near first use | **near first use** (EJ 57 agrees) |
| File header comment | class, version, date, copyright | licence only | **none**; git holds history |
| Flag comments | `XXX`, `FIXME` | `TODO: link - text` | **`TODO(TAG): text`** (tags in use: `BUG`, `DEAD`) |
| Logging an exception | - | log it | **`Constants.LOG`** with `{}` placeholders; never `printStackTrace()` or `System.out` |
| Logger name | - | a logger is not a constant | the existing `Constants.LOG` keeps its name |
| Parentheses | add them liberally | keep if unclear | add them where precedence is not obvious; `a == b && c == d` needs none |
| Javadoc on simple getters | doc every member | optional if trivial | **required** on every public method that is not an override (Global constraints) |

## Deliberate deviations from the sources

- Stricter than Effective Java: no finalizers or cleaners at all (EJ 8); no streams, boxing,
  varargs or `Optional` in tick and render paths without a measurement first (EJ 45, 55, 67),
  because standard 2 forbids known per-tick waste; no wildcard imports, including static
  `Collectors` imports (EJ 46 suggests them, Google bans them).
- Looser than Effective Java: `toString` is required on value types and logged types only, not
  every class (EJ 12); `Optional` record components made by `Codec.optionalFieldOf(name)` are
  allowed (EJ 55).

## Formatting

- One statement per line. One variable per declaration (`int a, b;` is out, except in a `for` header).
- Braces on every `if`, `else`, `for`, `do` and `while`, also for one-line bodies.
- K&R braces: `{` at the end of the line, `}` on its own line; `} else {`, `} catch (...) {` on one line.
- An empty catch block with no comment saying why is never allowed (see Exceptions).
- Wrapping: break at the highest syntactic level; break before an operator (also `.` and `::`);
  break after `=` and after a comma; keep a method name attached to its `(`; never break around `->`.
- Spaces: after a keyword before `(` (`if (`), none between a method name and `(`; around binary and
  ternary operators and `->`; after commas, semicolons in `for`, and a cast's `)`; none around `.` or `::`.
- One blank line between members and between logical sections of a method. No boxes of asterisks.
- No wildcard imports. Static imports in one group, then the rest; ASCII order inside each group.
- `long` literals use uppercase `L`. Numeric literals of 5 or more digits use `_` (`100_000`).
  Array brackets go on the type: `String[] args`.
- Modifier order: `public protected private abstract default static final sealed non-sealed
  transient volatile synchronized native strictfp`.
- Annotations on classes and methods: one per line above the declaration; `@Override` may share
  the line only if it is the single annotation. Type-use annotations (`@Nullable`) go right before the type.

## Naming

- Classes and interfaces: UpperCamelCase nouns (interfaces may be adjectives such as `-able`).
- Methods, lowerCamelCase: actions are verbs (`spawnEgg`); booleans `isX()`/`hasX()`; accessors
  a noun or `getX()`, matching the class; conversions `toX()`; views `asX()`; static factories
  `of`/`from`/`valueOf` (may share), `create`/`newInstance` (always new), `getInstance`.
- Fields, parameters, locals: lowerCamelCase. No prefixes or suffixes (`mName`, `_name`, `sName`).
- Constants: UPPER_SNAKE_CASE, only for `static final` fields that are deeply immutable. A mutable
  collection or a non-empty array is not a constant.
- Packages: lowercase, no underscores. Test classes: `<ClassUnderTest>Test`.
- Acronyms as words in new names (`XmlHttpRequest`, not `XMLHTTPRequest`), unless the name joins
  an existing family; keep existing names such as `DNAData` as they are.
- Descriptive whole words; no cryptic abbreviations. Short names only in small scopes (loop
  counters, one-line lambdas); name stream lambda parameters for the element. Put the unit in the
  name when it is not obvious (`delayTicks`, `radiusBlocks`).
- Type variables: one capital letter (`T`, `E`, `K`, `V`, `R`), or a class-style name plus `T`.

## Files and class layout

- One top-level class per file, named like the file. Keep files well under 2000 lines.
- Order: package, imports, class Javadoc, class.
- Inside a class: static fields, instance fields, constructors, then methods grouped by
  feature (not by visibility, not in the order they were added). Overloads stay together.

## Comments and Javadoc

- Javadoc every public type, every public method that is not an override, and every `protected`
  member and visible record component. Add a `package-info.java` note to a new package.
- The summary line is a fragment, capitalised and punctuated, and unique among overloads: a verb
  phrase for a method ("Returns the DNA yield."), a noun phrase for a class or field. Never
  "This method returns..." and never "@return ...".
- State the contract, not the implementation: preconditions, side effects, thread-safety
  (see Threads). Document type parameters (`@param <T>`). Use `{@code}` for code and for `<`, `>`, `&`.
- In a class designed for subclassing, say which overridable methods each method calls (`@implSpec`).
- Paragraphs after the first start with `<p>`. Tags in order `@param`, `@return`, `@throws`,
  `@deprecated`, never with an empty description. Document every exception a method can throw;
  a `NullPointerException` rule shared by the whole class may be stated once in the class Javadoc.
- Implementation comments explain why (trade-offs, game rules, side effects), not what the
  next line does. If the code needs many comments, simplify the code.

## Classes and objects (EJ 1-25)

- Minimise accessibility: `private` by default; top-level types package-private unless another
  package needs them; `protected` only for documented subclass hooks; widen for tests to
  package-private at most.
- A public class exposes no fields except constants; a pure data carrier is a `record`. Never
  expose an array or mutable collection through a `static final` field or getter: expose
  `List.of`/`List.copyOf`, or return a copy.
- Minimise mutability: `final` fields where possible. A constructor returns a fully valid object;
  no `init`/`reset` method, except a framework load method or a measured hot-path scratch object.
- Use only immutable objects as map keys and set elements; copy a mutable one first (`pos.immutable()`).
- Copy a mutable argument you keep (`ItemStack.copy()`, `BlockPos.immutable()`, `List.copyOf`)
  before you validate and store it. Copy mutable fields you return across an API boundary.
- Composition over inheritance: extend a class only when every instance truly is one. Minecraft
  and loader base classes are the exception; re-check every overridden vanilla method after a port.
- Never call an overridable method from a constructor. Minecraft's `Entity` and `Mob` constructors
  call `defineSynchedData` and `registerGoals`, so those overrides must not read the subclass's
  instance fields: they are not set yet.
- Design a class for subclassing (documented hooks) or make it `final`. Prefer interfaces to
  abstract classes. A class with a "kind" field and fields used only by some kinds becomes a
  `sealed` interface with one record or class per kind.
- Static nested classes over inner classes unless the instance reference is needed. A stored
  inner class or capturing lambda keeps its outer entity or level alive.
- Static factory methods where a name or caching helps. A builder when a constructor would take
  4 or more parameters or adjacent parameters of one type; never build with a no-arg constructor
  plus setters.
- A singleton is a single-element `enum`, or a private constructor plus a `static final`
  instance; prefer an injected instance when state or configuration varies.
- Utility classes: `final`, with a private constructor that throws `AssertionError`. No utility
  layer for a single caller. Never an interface that holds only constants.
- Pass collaborators in through the constructor instead of creating them inside, except where
  the loader or Minecraft registry design requires static registration.
- `equals` and `hashCode`: override them together, by the general contract, or not at all for
  identity types (entities, block entities, registry objects). Take an `Object` parameter with
  `@Override`; compare `float`/`double` with `Float.compare`/`Double.compare`, nullable fields with
  `Objects.equals`, arrays with `Arrays.equals`. `hashCode` uses exactly the fields `equals` uses;
  `Objects.hash` only off hot paths.
- Records generate these, but compare array components by reference: copy collection components
  in the compact constructor and avoid array components.
- Override `toString` on value types and anything that appears in logs (records and enums already
  have one). Never parse, persist or send `toString` output; use codecs.
- Order with `Integer.compare`/`Long.compare`/`Double.compare` or
  `Comparator.comparing(...).thenComparing(...)`, never subtraction or `<` chains.
- Never implement `Cloneable` or override `clone`; copy with a copy constructor or factory.
  `array.clone()` is fine.
- Enums instead of `int` or `String` constants. Per-constant data goes in `private final` fields;
  per-constant behaviour in a method or a lambda field, never a `switch (this)`.
- Never use `ordinal()` for a value, or to save or send an enum: keep an explicit field, or save
  the name or id through a codec. `EnumMap` instead of ordinal-indexed arrays; `EnumSet` instead of
  `int` bit flags.
- Never use finalizers or cleaners. A class that owns a resource implements `AutoCloseable`;
  close resources with try-with-resources (`Files.lines`, `Files.list`, `Files.walk` too).
- Never use Java serialization or add `implements Serializable`. Persist and sync with `Codec` /
  `StreamCodec`. Every decode path builds through the constructor that checks the invariants, and
  bounds every length read from a packet or file before it allocates.

## Methods (EJ 49-58)

- One job per method. Aim for about 10-20 lines; split when a method does more than one thing,
  not to hit a number. Reduce nesting with guard clauses (early `return`, `continue`).
- Check parameters at the API boundary and fail fast, also ones that are only stored:
  `Objects.requireNonNull`, `Objects.checkIndex`. Private helpers may use `assert`.
- 4 or fewer parameters. Bundle parameters that travel together into a `record`. Avoid adjacent
  parameters of one type that a caller can swap. Use an enum or two methods, not a boolean flag.
- Return empty collections or `Optional`, not `null`. Where `null` stays (Minecraft APIs, hot
  fields), mark it `@Nullable`.
- `Optional`: never return `null` from an `Optional` method; never `Optional` of a collection,
  map, array or stream; no `Optional` parameters, collection elements, map values or fields (codec
  record components excepted); `OptionalInt`/`OptionalLong`/`OptionalDouble` for primitives;
  `orElse`/`orElseThrow`/`map` instead of `isPresent()` plus `get()`.
- A method that returns a sequence returns an unmodifiable `List` or `Collection`, not a `Stream`,
  unless the sequence is lazy or very large.
- No two public overloads with the same parameter count and related types; if they must overlap,
  the specific one forwards to the general one. Never overload on two functional interfaces in the
  same position. Watch `List.remove(int)` versus `remove(Object)`.
- Varargs: `(T first, T... rest)` when one argument is required. A generic varargs parameter needs
  `@SafeVarargs` and the method must not store or return the array; otherwise take a `List`.
- Return expressions directly: `return condition;`, not `if (condition) return true; else return false;`.
- No assignments inside conditions or other expressions; no chained assignments (`a = b = c`).

## Language features (EJ 26-48, 57-65)

- `@Override` on every method that overrides or implements a supertype method.
- Switch on one value with many constants: a switch expression with `->` arms, no fall-through.
  On an enum or sealed type, omit `default` so a new constant fails to compile. An old `:` switch
  needs `break` or a `// fall through` comment, and a `default`.
- Lambdas over anonymous classes; keep a lambda to 1-3 lines, else move it to a named method. An
  anonymous class only for an abstract class, a multi-method interface or a body that needs itself.
- Method references (`Entity::isAlive`) only where shorter and clearer than the lambda.
- Standard `java.util.function` interfaces, primitive variants for primitives (`IntPredicate`, not
  `Predicate<Integer>`). A custom one only for a checked exception, 3 or more parameters or a real
  contract, marked `@FunctionalInterface`.
- Enhanced `for` over index loops when the index is not needed; `removeIf` to filter in place.
  `for` over `while` when the loop variable is not needed afterwards.
- Streams for clear filter/map/collect pipelines outside hot paths. Use a loop when the body must
  change locals, `return`/`break`/`continue`, or throw a checked exception. Stream functions have
  no side effects; `forEach` only reports a result; give `Collectors.toMap` a merge function when
  keys can repeat.
- Never `parallel()` or `parallelStream()` on a stream that touches Minecraft state; elsewhere only
  after a measurement shows a gain.
- Generics: no raw types (`<?>` when the type is unknown; raw only in class literals); use `<>`.
  Remove unchecked warnings; suppress one only after proving it safe, on the smallest declaration,
  never a class, with a comment. If callers must cast a value out, make the type or method generic.
- `List<T>` over `T[]` when `T` is generic or the array needs an unchecked cast; primitive arrays
  and hot-path arrays are fine. Bounded wildcards: `? extends` for producers, `? super` for
  consumers, `Comparator<? super T>`; never return a wildcard type.
- Refer to objects by interface (`List`, `Map`) where one exists, else by the least specific base
  class. `var x = new ArrayList<>()` infers `ArrayList<Object>`: state the type.
- Know the JDK and Minecraft libraries before writing a helper (see standard 2): `RandomSource`
  for gameplay randomness; fastutil primitive maps for hot paths; a `static final Pattern` instead
  of `String.matches`/`split`/`replaceAll` in repeated code.
- Primitives over boxed types; never compare boxed values with `==`; null-check a nullable boxed
  value before it meets a primitive. `int`/`long` for counts and ticks; no `float`/`double` where
  an exact value is needed. No `String` where an enum, `Identifier` or record fits.
- No reflection (`java.lang.reflect`, `Class.forName`) in game or mod code: use an interface,
  `ServiceLoader` (as `Services.load` does), an access widener or transformer, or a Mixin accessor.
  If unavoidable, keep it in one class and call the result through an interface.
- Magic numbers become named constants; tunable gameplay numbers become config values
  (Global constraints).

## Exceptions and logging (EJ 69-77)

- Exceptions only for exceptional conditions, never for control flow. Do not make callers catch
  in normal flow: add a `canX()` test or return `Optional` (or `DataResult` in codec code).
- Unchecked exceptions for programming errors. A checked exception only when the failure cannot
  be prevented and the caller can act on it; prefer `Optional` or a result type (checked exceptions
  do not pass through lambdas).
- Standard types first: `NullPointerException` (null argument), `IndexOutOfBoundsException`,
  `IllegalArgumentException` (bad value), `IllegalStateException` (bad object state),
  `UnsupportedOperationException`. Never throw `Exception`, `RuntimeException`, `Throwable` or
  `Error`; never extend `Error` or `Throwable` directly.
- Throw exceptions that fit the method's own abstraction: wrap a lower-level one and pass the
  cause (`UncheckedIOException` for `IOException`). First try to prevent it by validating.
- Declare each checked exception by name; never `throws Exception`; never list unchecked
  exceptions in `throws`, document them with `@throws`.
- Catch the narrowest type. Never catch `Exception` or `Throwable` except at a top-level
  boundary that logs and recovers (data loading, a datapack entry).
- Never swallow an exception. Log it with context through `Constants.LOG`, or rethrow wrapped with
  the cause. If ignoring it is truly right, write `catch (X _)` with a comment that says why.
- Detail messages carry the failing values (id, position, file) so a log alone can explain the
  failure; no secrets. Never parse `getMessage()`; give a custom exception accessor methods.
- Leave the object in its prior state when an operation fails (failure atomicity), or document
  the state it leaves.

## Performance (standard 2, EJ 6, 7, 67)

- Measure before and after optimising (JFR or a profiler), and revert a change with no gain.
  Fix the algorithm before micro-tuning. Design packets and save data for size and parse cost up
  front, because formats are hard to change later.
- Never leave known per-tick or per-frame waste. In tick and render paths: no streams, boxing,
  varargs calls or `Optional`; no per-call allocation that can be hoisted; cache `values()` arrays;
  no block or entity scans that a cache, event or interval can replace.
- Do not pool small objects; reuse a scratch object only in a measured hot path.
- Size collections when the size is known. Build strings in loops with `StringBuilder`.
- Remove obsolete references: evict cache and map entries on entity removal and chunk unload;
  unregister listeners; never hold a `Level`, `Entity` or client player in a static field. Use a
  `WeakHashMap` only when an entry should live exactly as long as its key. Do not null out locals.

## Threads (EJ 78-84)

- Minecraft state is confined to its game thread: touch world, entity and block entity state only
  there. A packet handler changes game state only on that thread (check which thread the loader
  calls it on) and validates on the server. Netty threads,
  worldgen workers and parallel loader setup events are where the rules below apply.
- Shared mutable data: guard every read and write with the same lock, or use `volatile` or an
  `Atomic*` type. `volatile` does not make `x++` atomic. Prefer sharing immutable data.
- Never call a callback, listener or overridable method while holding a lock; keep locked regions
  short. Iterate a snapshot of a listener list (this also prevents a
  `ConcurrentModificationException` when a listener removes itself).
- Background work goes through an `ExecutorService`, never a raw `Thread` or a hand-written queue;
  shut down every executor you create. `ConcurrentHashMap` with `computeIfAbsent`/`merge` over
  synchronized maps. Time intervals with `System.nanoTime()`. On `InterruptedException`, call
  `Thread.currentThread().interrupt()`.
- When instances can reach more than one thread, state the class's level in its Javadoc:
  immutable, thread-safe, not thread-safe, or game-thread-only. Lock on a `private final` object,
  never on `this`.
- Initialise fields normally. Lazy initialisation only to break a cycle or after measuring:
  holder class for a static field, `volatile` double-check for a shared instance field, a plain
  null check for a game-thread-only field.
- Never depend on thread timing: no busy-waiting, `Thread.yield()` or thread priorities.

## Tests

- JUnit 5 tests for logic that can run without the game (codecs, maths, data parsing). Name
  tests for the behaviour they check. A test must fail when the implementation is reverted.
- Test the complex and risky logic first; skip tests of trivial getters.
- Compare `double` values with a tolerance.

## Design limits

- YAGNI: no speculative abstraction, feature or extension point. Refactor toward an abstraction
  when duplication or untestable logic makes the simple version fail, not before.
- Clear over clever. No circular dependencies between packages; common code never references
  loader or client-only classes.
- Keep dependencies current and free of known vulnerabilities; add none without a reason.

## Sources

- JetBrains, "Java Best Practices" (2024): https://blog.jetbrains.com/idea/2024/02/java-best-practices/
- Oracle, "Code Conventions for the Java Programming Language", all 11 sections (archived,
  last revised 1999): https://www.oracle.com/java/technologies/javase/codeconventions-introduction.html
- JAVAPRO, "Best Practices for Writing Clean Code in Java" (2025):
  https://javapro.io/2025/11/25/best-practices-for-writing-clean-code-in-java/
- in28minutes, java-best-practices (README; the linked PDFs were not read):
  https://github.com/in28minutes/java-best-practices
- Google Java Style Guide: https://google.github.io/styleguide/javaguide.html
- Joshua Bloch, Effective Java, 3rd edition (2018), items 1-90, read in full from the user's copy
  on 2026-10-29 and checked line by line against this list.
