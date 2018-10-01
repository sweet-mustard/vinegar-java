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
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.hamcrest.Matcher;

/**
 * A condition to pass to {@link PatternMatcher#when(MappingCondition)}. The condition can
 * optionally do some mapping for convenience, eg. check for a subtype of the input type and cast to
 * it or extract a value of the input. In that case, the parameter <code>I1</code> will differ from
 * the original input type <code>I</code>.
 *
 * @param <I> the original input type
 * @param <I1> the mapped input type (often the same as <code>I</code>)
 */
public abstract class MappingCondition<I, I1> {

  /**
   * Maps the input in case this condition performs type checking or extracts a value.
   */
  public abstract MaybeMatch<I1> mapIfMatches(final I input);

  /**
   * Creates a condition that checks whether the input is of type <code>I1</code>. If so, the
   * condition will cast the input to the type <code>I1</code>
   */
  public static <I, I1 extends I> MappingCondition<I, I1> is(final Class<I1> type) {
    return new TypeMappingCondition<>(type);
  }

  /**
   * Create a condition that checks whether the input satisfies the specified predicate. This
   * condition does not perform any mapping and will return the input as is.
   */
  public static <I> MappingCondition<I, I> predicate(final Predicate<? super I> predicate) {
    return new PredicateMappingCondition<>(predicate);
  }

  /**
   * Create a condition that checks whether the input satisfies the specified Hamcrest {@link
   * Matcher}. This condition does not perform any mapping and will return the input as is.
   */
  public static <I> MappingCondition<I, I> matcher(final Matcher<? super I> matcher) {
    return predicate(matcher::matches);
  }

  /**
   * Create a condition that checks whether the input is equal to the specified value using {@link
   * Object#equals(Object)}. This condition does not perform any mapping and will return the input
   * as is.
   */
  public static <I> MappingCondition<I, I> eq(final I value) {
    return predicate(i -> Objects.equals(i, value));
  }

  /**
   * Creates a condition that will match any value. This condition does not perform any mapping and
   * will return the input as is.
   */
  public static <I> MappingCondition<I, I> any() {
    return predicate(i -> true);
  }

  /**
   * Creates a condition that matches strings with the specified regex. This condition maps the
   * input to a {@link MatchResult}.
   */
  public static MappingCondition<String, MatchResult> regex(final String regex) {
    return new RegexMappingCondition(regex);
  }

  /**
   * Creates a condition that matches strings with the specified regex. This condition maps the
   * input to the first matching group.
   */
  public static MappingCondition<String, String> regex1(final String regex) {
    return new Regex1MappingCondition(regex);
  }

  /**
   * Creates a condition that matches strings with the specified regex. This condition maps the
   * input to  a {@link Pair} of the first two matching groups.
   */
  public static MappingCondition<String, Pair<String, String>> regex2(final String regex) {
    return new Regex2MappingCondition(regex);
  }

  /**
   * Creates a condition that matches a {@link Pair}. It checks both values of the pair with the
   * specified conditions. This condition does not perform any mapping and will return the input as
   * is.
   *
   * @param condition1 the condition to apply to the first value of the pair
   * @param condition2 the condition to apply to the second value of the pair
   */
  public static <A, B> MappingCondition<Pair<A, B>, Pair<A, B>> pair(
      MappingCondition<A, A> condition1, MappingCondition<B, B> condition2) {
    return new PairMappingCondition<>(condition1, condition2);
  }

  /**
   * Creates a condition that matches a {@link Pair}. It checks both values of the pair with the
   * specified conditions. This condition does not perform any mapping and will return the input as
   * is.
   *
   * @param condition1 the condition to apply to the first value of the pair
   * @param condition2 the condition to apply to the second value of the pair
   */
  public static <A, B> MappingCondition<Pair<A, B>, Pair<A, B>> pair(
      Predicate<A> condition1, Predicate<B> condition2) {
    return new PairMappingCondition<>(predicate(condition1), predicate(condition2));
  }

  static final class TypeMappingCondition<I, I1 extends I> extends MappingCondition<I, I1> {

    private final Class<I1> type;

    private TypeMappingCondition(final Class<I1> type) {
      this.type = type;
    }

    @Override
    public MaybeMatch<I1> mapIfMatches(final I input) {
      return MaybeMatch
          .create(input != null && type.isAssignableFrom(input.getClass()), () -> type.cast(input));
    }
  }

  static final class PredicateMappingCondition<I> extends MappingCondition<I, I> {

    private final Predicate<? super I> predicate;

    private PredicateMappingCondition(final Predicate<? super I> predicate) {
      this.predicate = predicate;
    }

    @Override
    public MaybeMatch<I> mapIfMatches(final I input) {
      return MaybeMatch.create(predicate.test(input), () -> input);
    }
  }

  static final class RegexMappingCondition extends MappingCondition<String, MatchResult> {

    private final Pattern pattern;

    RegexMappingCondition(final String regex) {
      pattern = Pattern.compile(regex);
    }

    @Override
    public MaybeMatch<MatchResult> mapIfMatches(final String input) {
      java.util.regex.Matcher matcher = pattern.matcher(input);
      return MaybeMatch.create(matcher.matches(), () -> matcher);
    }
  }

  static final class Regex1MappingCondition extends MappingCondition<String, String> {

    private final RegexMappingCondition delegateCondition;

    Regex1MappingCondition(final String regex) {
      this.delegateCondition = new RegexMappingCondition(regex);
    }

    @Override
    public MaybeMatch<String> mapIfMatches(final String input) {
      return delegateCondition.mapIfMatches(input)
          .map(r -> r.group(1));
    }
  }

  static final class Regex2MappingCondition extends MappingCondition<String, Pair<String, String>> {

    private final RegexMappingCondition delegateCondition;

    Regex2MappingCondition(final String regex) {
      this.delegateCondition = new RegexMappingCondition(regex);
    }

    @Override
    public MaybeMatch<Pair<String, String>> mapIfMatches(final String input) {
      return delegateCondition.mapIfMatches(input)
          .map(r -> new Pair<>(r.group(1), r.group(2)));
    }
  }

  static final class PairMappingCondition<A, B> extends MappingCondition<Pair<A, B>, Pair<A, B>> {

    private final MappingCondition<A, A> condition1;
    private final MappingCondition<B, B> condition2;

    PairMappingCondition(final MappingCondition<A, A> condition1,
        final MappingCondition<B, B> condition2) {
      this.condition1 = condition1;
      this.condition2 = condition2;
    }

    @Override
    public MaybeMatch<Pair<A, B>> mapIfMatches(final Pair<A, B> input) {
      MaybeMatch<A> maybeA = condition1.mapIfMatches(input.getA());
      MaybeMatch<B> maybeB = condition2.mapIfMatches(input.getB());
      return MaybeMatch.create(maybeA.matches() && maybeB.matches(),
          () -> new Pair<>(maybeA.getValue(), maybeB.getValue()));
    }
  }

}
