# Vinegar (Java)

Vinegar is a set of small but highly reusable libraries.
Vinegar libraries typically don't require any other libraries, but play nice with popular frameworks and libraries.

## Pattern Matcher

The [Pattern Matcher Vinegar](pattern-matcher/README.md) is inspired by [JDK enhancement proposal 305](http://openjdk.java.net/jeps/305).
It brings [Pattern Matching](https://en.wikipedia.org/wiki/Pattern_matching) to Java in the form of a DSL.
Here's an example of the Pattern Matcher Vinegar in action:

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

See also [Pattern Matching for Java](http://cr.openjdk.java.net/~briangoetz/amber/pattern-match.html)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENCE.txt) file for details