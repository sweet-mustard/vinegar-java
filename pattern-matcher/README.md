# Pattern Matcher

The Pattern Matcher Vinegar is inspired by [JDK enhancement proposal 305](http://openjdk.java.net/jeps/305).
It brings [Pattern Matching](https://en.wikipedia.org/wiki/Pattern_matching) to Java in the form of a DSL.

A pattern matcher typically matches an input object for one or more patterns.
It can either return a result from that, or perform an action.

Pattern matchers are immutable and can safely be reused and shared across threads.
Since there is a cost to creating pattern matchers, especially with regular expression matching,
it is advised to create your pattern matcher as a constant and reuse it as much as possible.

## Features

### Equality and predicate matching

The pattern matcher's `when()` method accepts exact values and `Predicate`s. 
Furthermore it supports Hamcrest `Matcher`s or pattern matcher specific `Condition`s.
Hamcrest is not required, it is an optional dependency.

```
Function<Integer, Optional<String>> matcher = new PatternMatcher<Integer, String>()
    .when(i -> i < 10).then("Less then 10")
    .when(10).then("Exactly 10")
    .when(i -> i > 10).then("Greater than 10");
    
Optional<String> result = matcher.apply(10); // Returns: Optional[Exactly 10]
```

### Otherwise

The pattern matcher's `apply()` method will return `Optional`, unless `otherwise()` is present.

```
Function<Integer, String> matcher = new PatternMatcher<Integer, String>()
    .when(i -> i < 10).then("Less then 10")
    .when(10).then("Exactly 10")
    .otherwise("Greater than 10");
    
String result = matcher.apply(11); // Returns: Greater than 10
```

### Type matching

When providing a type `Condition` using `is(Class)`, the input will automatically be converted to the specified type.

```
import static be.sweetmustard.vinegar.matcher.MappingCondition.is;

Function<Shape, String> matcher = new PatternMatcher<Shape, String>()
    .when(is(Circle.class))
        .then(c -> "Circle with radius " + c.getRadius())
    .when(is(Rectangle.class))
        .then(r -> "Rectangle with width " + r.getWidth() + " and height " + r.getHeight())
    .otherwise("Unknown shape");

String result = matcher.apply(new Rectangle(2, 5)); // Returns: Rectangle with width 2 and height 5
```

### Actions

Use `thenDo()` and `otherwiseDo()` to perform an action instead of returning a value.

```
new PatternMatcher<Integer, Void>()
    .when(i -> i < 10).thenDo(i -> System.out.println("Less then 10: " + i))
    .when(10).thenDo(i -> System.out.println("Exactly 10"))
    .otherwiseDo(i -> System.out.println("Greater than 10: " + i))
    .apply(10); // Prints: Exactly 10
```

### Regular expression matching

The condition `regex1()` returns the first matching group of a regular expression.
`regex2()` returns the first two matching groups. Combine with `regex2()` with `when2()`
`regex()` returns a `MatchResult` with all matching groups of a regular expression.

```
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex;
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex1;
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex2;

Function<String, Optional<Object>> matcher = new PatternMatcher<String, Object>()
    .when(regex1("^(\\d+)$")).then(Integer::parseInt)
    .when2(regex2("^([A-Z]{3}) (\\d+)$")).then((c, v) -> new Money(c, Integer.parseInt(v)))
    .when(regex("^(\\d{4})-(\\d{2})-(\\d{2})$")).then(m -> LocalDate.of(
        Integer.parseInt(m.group(1)), 
        Integer.parseInt(m.group(2)),
        Integer.parseInt(m.group(3))));
        
Optional<Object> result = matcher.apply("2018-09-14"); // Returns: Optional of LocalDate(September 14, 2018)
```