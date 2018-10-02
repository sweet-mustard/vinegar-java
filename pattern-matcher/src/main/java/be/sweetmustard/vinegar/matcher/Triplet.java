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

import java.util.Objects;

/**
 * A triplet are three values that go together. Triplets can be extracted from the input of a {@link
 * PatternMatcher}.
 *
 * @see PatternMatcher#extract(TriFunction)
 */
public final class Triplet<A, B, C> {

  private final A a;
  private final B b;
  private final C c;

  public Triplet(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  /**
   * Returns the first value of the triplet.
   *
   * @return the first value.
   */
  public A getA() {
    return a;
  }

  /**
   * Returns the second value of the triplet.
   *
   * @return the second value.
   */
  public B getB() {
    return b;
  }

  /**
   * Returns the third value of the triplet.
   *
   * @return the thrird value.
   */
  public C getC() {
    return c;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
    return Objects.equals(a, triplet.a) &&
        Objects.equals(b, triplet.b) &&
        Objects.equals(c, triplet.c);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, c);
  }

  @Override
  public String toString() {
    return "Triplet{" +
        "a=" + a +
        ", b=" + b +
        ", c=" + c +
        '}';
  }
}
