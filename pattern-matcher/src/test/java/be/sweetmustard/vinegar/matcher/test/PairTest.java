package be.sweetmustard.vinegar.matcher.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import be.sweetmustard.vinegar.matcher.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PairTest {

  @ParameterizedTest(name = "Pair({0}, {1}) equals Pair({2}, {3}) = {4}")
  @CsvSource({
      "foo, bar, foo, baz, false",
      "foo, bar, foo, bar, true"
  })
  void equals(String value1a, String value1b, String value2a, String value2b, boolean equals) {
    Pair<String, String> pair1 = new Pair<>(value1a, value1b);
    Pair<String, String> pair2 = new Pair<>(value2a, value2b);
    assertEquals(pair1.equals(pair2), equals);
  }

  @ParameterizedTest(name = "Pair({0}, {1}).hashCode equals Pair({2}, {3}).hashCode = {4}")
  @CsvSource({
      "foo, bar, foo, baz, false",
      "foo, bar, foo, bar, true"
  })
  void hashCode(String value1a, String value1b, String value2a, String value2b, boolean equals) {
    Pair<String, String> pair1 = new Pair<>(value1a, value1b);
    Pair<String, String> pair2 = new Pair<>(value2a, value2b);
    assertEquals(pair1.hashCode() == pair2.hashCode(), equals);
  }
}
