/*
 * MIT licence
 *
 * Copyright (c) 2018 Sweet Mustard
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package be.sweetmustard.vinegar.matcher;

import static be.sweetmustard.vinegar.matcher.MappingCondition.any;
import static be.sweetmustard.vinegar.matcher.MappingCondition.eq;
import static be.sweetmustard.vinegar.matcher.MappingCondition.is;
import static be.sweetmustard.vinegar.matcher.MappingCondition.pair;
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex;
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex1;
import static be.sweetmustard.vinegar.matcher.MappingCondition.regex2;
import static be.sweetmustard.vinegar.matcher.PatternMatcher.extract;
import static java.lang.Double.parseDouble;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PatternMatcherTest {

  @Test
  void matchTypeShouldConvertToType() {
    String result = new PatternMatcher<Shape, String>()
        .when(is(Circle.class)).then(c ->
            "Circle (radius = " + c.radius + ", area = " + c.getArea() + ")")
        .when(is(Rectangle.class)).then(r ->
            "Rectangle (width = " + r.width + ", height = " + r.height + ", area = " + r.getArea()
                + ")")
        .otherwise(s -> "Unknown shape (area = " + s.getArea() + ")")
        .apply(new Rectangle(2.5, 4));

    assertEquals("Rectangle (width = 2.5, height = 4.0, area = 10.0)", result);
  }

  @Test
  void matchEqShouldTestForEquality() {
    Optional<String> result = new PatternMatcher<String, String>()
        .when("E").then("Earth")
        .when("W").then("Water")
        .when("F").then("Fire")
        .when("A").then("Air")
        .apply("F");

    assertEquals("Fire", result.orElse(null));
  }

  @Test
  void thenDoShouldConsumeInput() {
    List<Integer> values = new ArrayList<>();
    new PatternMatcher<Integer, Void>()
        .when(i -> i <= 5).thenDo(v -> values.add(v * v))
        .when(i -> i > 5).thenDo(values::add)
        .otherwiseDo(v -> values.add(v + 1))
        .apply(4);

    assertEquals(Collections.singletonList(16), values);
  }

  @Test
  void extractShouldExtractInput() {
    String result = new PatternMatcher<Shape, String>()
        .when(is(Circle.class)).then(extract(r -> "Circle (radius = " + r + ")"))
        .when(is(Rectangle.class))
        .then(extract((w, h) -> "Rectangle (width = " + w + ", height = " + h + ")"))
        .otherwise(s -> "Unknown shape (area = " + s.getArea() + ")")
        .apply(new Rectangle(2.5, 4));

    assertEquals("Rectangle (width = 2.5, height = 4.0)", result);
  }

  @Test
  void regexShouldEvaluateRegex() {
    Optional<Shape> result = new PatternMatcher<String, Shape>()
        .when(regex("Circle\\(([\\d.]+)\\)")).then(m -> new Circle(parseDouble(m.group(1))))
        .when(regex("Rectangle\\(([\\d.]+), ([\\d.]+)\\)"))
        .then(m -> new Rectangle(parseDouble(m.group(1)), parseDouble(m.group(2))))
        .apply("Rectangle(3.0, 5.5)");

    assertEquals(new Rectangle(3.0, 5.5), result.orElse(null));
  }

  @Test
  void regex1ShouldReturnGroup1() {
    Optional<Shape> result = new PatternMatcher<String, Shape>()
        .when(regex1("Circle\\(([\\d.]+)\\)")).then(r -> new Circle(parseDouble(r)))
        .when(regex("Rectangle\\(([\\d.]+), ([\\d.]+)\\)"))
        .then(m -> new Rectangle(parseDouble(m.group(1)), parseDouble(m.group(2))))
        .apply("Rectangle(3.0, 5.5)");

    assertEquals(new Rectangle(3.0, 5.5), result.orElse(null));
  }

  @Test
  void regex2ShouldReturnGroups1And2() {
    Optional<Shape> result = new PatternMatcher<String, Shape>()
        .when(regex("Circle\\(([\\d.]+)\\)")).then(m -> new Circle(parseDouble(m.group(1))))
        .when2(regex2("Rectangle\\(([\\d.]+), ([\\d.]+)\\)"))
        .then((w, h) -> new Rectangle(parseDouble(w), parseDouble(h)))
        .apply("Rectangle(3.0, 5.5)");

    assertEquals(new Rectangle(3.0, 5.5), result.orElse(null));
  }

  @Test
  void hamcrestMatcherShouldMatch() {
    String result = new PatternMatcher<List<String>, String>()
        .match(hasItem("baz")).then("It's a Baz!")
        .match(hasItem("foo")).then("It's a Foo!")
        .match(hasItem("bar")).then("It's a Bar!")
        .otherwise("It's nothing!")
        .apply(Arrays.asList("bar", "foo"));

    assertEquals("It's a Foo!", result);
  }

  @Test
  void pairShouldMatchAPairOfValues() {
    String result = new PatternMatcher<Pair<Integer, Integer>, String>()
        .when(pair(eq(0), eq(0))).then("FizzBuzz")
        .when(pair(eq(0), any())).then("Fizz")
        .when(pair(any(), eq(0))).then("Buzz")
        .otherwise(p -> "")
        .apply(new Pair<>(10 % 3, 10 % 5));
    assertEquals("Buzz", result);
  }

  interface Shape<T> extends Extractable<T> {

    double getArea();
  }

  static class Circle implements Shape<Double> {

    final double radius;

    Circle(final double radius) {
      this.radius = radius;
    }


    @Override
    public double getArea() {
      return radius * 2 * Math.PI;
    }

    @Override
    public Double extract() {
      return radius;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final Circle circle = (Circle) o;
      return Double.compare(circle.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(radius);
    }
  }

  static class Rectangle implements Shape<Pair<Double, Double>> {

    final double width;
    final double height;

    Rectangle(final double width, final double height) {
      this.width = width;
      this.height = height;
    }

    @Override
    public double getArea() {
      return width * height;
    }

    @Override
    public Pair<Double, Double> extract() {
      return new Pair<>(width, height);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final Rectangle rectangle = (Rectangle) o;
      return Double.compare(rectangle.width, width) == 0 &&
          Double.compare(rectangle.height, height) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(width, height);
    }
  }
}
