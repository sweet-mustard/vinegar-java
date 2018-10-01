package be.sweetmustard.vinegar.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TripletTest {

  @ParameterizedTest(name = "Triplet({0}, {1}, {2}) equals Triplet({3}, {4}, {5}) = {6}")
  @CsvSource({
      "foo, bar, baz, foo, baz, baz, false",
      "foo, bar, baz, foo, bar, baz, true"
  })
  void equals(String value1a, String value1b, String value1c, String value2a, String value2b,
      String value2c, boolean equals) {
    Triplet<String, String, String> triplet1 = new Triplet<>(value1a, value1b, value1c);
    Triplet<String, String, String> triplet2 = new Triplet<>(value2a, value2b, value2c);
    assertEquals(triplet1.equals(triplet2), equals);
  }

  @ParameterizedTest(name = "Triplet({0}, {1}, {2}).hashCode equals Triplet({3}, {4}, {5}).hashCode = {6}")
  @CsvSource({
      "foo, bar, baz, foo, baz, baz, false",
      "foo, bar, baz, foo, bar, baz, true"
  })
  void hashCode(String value1a, String value1b, String value1c, String value2a, String value2b,
      String value2c, boolean equals) {
    Triplet<String, String, String> triplet1 = new Triplet<>(value1a, value1b, value1c);
    Triplet<String, String, String> triplet2 = new Triplet<>(value2a, value2b, value2c);
    assertEquals(triplet1.hashCode() == triplet2.hashCode(), equals);
  }
}
