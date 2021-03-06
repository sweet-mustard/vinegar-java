package be.sweetmustard.vinegar.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TriFunctionTest {

  @Test
  void andThenShouldChainNextFunction() {
    TriFunction<String, String, String, Integer> concatenateAndCalculateLength =
        ((TriFunction<String, String, String, String>) (a, b, c) -> a + b + c)
            .andThen(String::length);

    int result = concatenateAndCalculateLength.apply("foo", "bar", "foobar");
    assertEquals(12, result);
  }

}
