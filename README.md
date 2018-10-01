# Vinegar (Java)

Vinegar is a set of small but highly reusable libraries.
Vinegar libraries typically don't require any other libraries, but play nice with popular frameworks and libraries.

## Getting Started

Add a Maven dependency on vinegar-java in your pom.xml:

```xml
<dependency>
  <groupId>be.sweetmustard.vinegar</groupId>
  <artifactId>vinegar-all</artifactId>
  <version>0.1.0</version>
</dependency>
```

Or if you only need one library, add a dependency on that specific library:

```xml
<dependency>
  <groupId>be.sweetmustard.vinegar</groupId>
  <artifactId>vinegar-pattern-matcher</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Features

### Pattern Matcher

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

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/sweet-mustard/vinegar-java/tags). 

## Authors

* **Michael Devloo** - *Pattern Matcher* - [Sweet Mustard](https://github.com/sweet-mustard)
* **Stijn Van Bael** - *Pattern Matcher* - [Sweet Mustard](https://github.com/sweet-mustard)

See also the list of [contributors](https://github.com/sweet-mustard/vinegar-java/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENCE.txt) file for details