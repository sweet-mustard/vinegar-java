# Mustard Seeds (Java)

Mustard seeds are small but highly reusable libraries.
Seeds typically don't require any other libraries, but play nice with popular frameworks and libraries.

## Pattern Matcher

The [Pattern Matcher Seed](pattern-matcher/README.md) is inspired by [JDK enhancement proposal 305](http://openjdk.java.net/jeps/305).
It brings [Pattern Matching](https://en.wikipedia.org/wiki/Pattern_matching) to Java in the form of a DSL.
Here's an example of the Pattern Matcher Seed in action:

```
String result = new PatternMatcher<Shape, String>()
    .when(is(Circle.class)).then(c ->
            "Circle (radius = " + c.radius + ", area = " + c.getArea() + ")")
    .when(is(Rectangle.class)).then(r ->
            "Rectangle (width = " + r.width + ", height = " + r.height + ", area = " + r.getArea() + ")")
    .otherwise(s -> "Unknown shape (area = " + s.getArea() + ")")
    .apply(new Rectangle(2.5, 4));
```

See also [Pattern Matching for Java](http://cr.openjdk.java.net/~briangoetz/amber/pattern-match.html)