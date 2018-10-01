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

import static be.sweetmustard.vinegar.matcher.MappingCondition.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.hamcrest.Matcher;

/**
 * The Pattern Matcher Vinegar is inspired by JDK enhancement proposal 305. It brings Pattern
 * Matching to Java in the form of a DSL. See README.md for more information how to use.
 */
public final class PatternMatcher<I, O> implements Function<I, Optional<O>> {

  private final List<Case<? super I, ?, O>> cases = new ArrayList<>();

  public PatternMatcher() {
  }

  private PatternMatcher(final PatternMatcher<? super I, O> previousMatcher,
      final Case<? super I, ?, O> newCase) {
    cases.addAll(previousMatcher.cases);
    cases.add(newCase);
  }

  /**
   * Creates a mapper <code>Function</code> that extracts a single value from the input and applies
   * the specified
   * <code>mapper</code> on it.
   *
   * @param extractedValueMapper the mapper <code>Function</code> to apply to the extracted value
   * @return A mapper <code>Function</code> to use in a <code>then()</code> or
   * <code>otherwise()</code> clause.
   * @see Extractable
   */
  public static <I extends Extractable<D>, D, O> Function<I, O> extract(
      final Function<? super D, ? extends O> extractedValueMapper) {
    return input -> extractedValueMapper.apply(input.extract());
  }

  /**
   * Creates a mapper <code>Function</code> that extracts a <code>Pair</code> of values from the
   * input and applies the specified
   * <code>mapper</code> on it.
   *
   * @param extractedValuesMapper the mapper <code>BiFunction</code> to apply to the extracted
   * values
   * @return A mapper <code>Function</code> to use in a <code>then()</code> or
   * <code>otherwise()</code> clause.
   * @see Extractable
   */
  public static <I extends Extractable<Pair<E1, E2>>, E1, E2, O> Function<I, O> extract(
      final BiFunction<? super E1, ? super E2, ? extends O> extractedValuesMapper) {
    return input -> {
      final Pair<E1, E2> pair = input.extract();
      return extractedValuesMapper.apply(pair.getA(), pair.getB());
    };
  }

  /**
   * Creates a mapper <code>Function</code> that extracts a <code>Triplet</code> of values from the
   * input and applies the specified
   * <code>mapper</code> on it.
   *
   * @param extractedValuesMapper the mapper <code>TriFunction</code> to apply to the extracted
   * values
   * @return A mapper <code>Function</code> to use in a {@link CaseBuilder#then(Function)} or {@link
   * #otherwise(Function)} clause.
   * @see Extractable
   */
  public static <I extends Extractable<Triplet<E1, E2, E3>>, E1, E2, E3, O> Function<I, O> extract(
      final TriFunction<? super E1, ? super E2, ? super E3, ? extends O> extractedValuesMapper) {
    return input -> {
      final Triplet<E1, E2, E3> triplet = input.extract();
      return extractedValuesMapper.apply(triplet.getA(), triplet.getB(), triplet.getC());
    };
  }

  /**
   * Matches any value that satisfies the specified <code>condition</code> that extracts a pair of
   * values.
   *
   * @param <I1> The type of the first extracted value.
   * @param <I2> The type of the second extracted value.
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   * @see MappingCondition
   */
  public <I1, I2> Case2Builder<I, I1, I2, O> when2(
      final MappingCondition<? super I, Pair<I1, I2>> condition) {
    return new Case2Builder<>(this, condition);
  }

  /**
   * Matches any value that satisfies the specified <code>condition</code>. The condition can
   * optionally check for a subtype of the input type or extract a value of the input. In that case,
   * the parameter <code>I1</code> will differ from the original input type <code>I</code>.
   *
   * @param <I1> The type of the input type or the extracted value type, in case the condition
   * performs type checking or value extraction.
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   * @see MappingCondition
   */
  public <I1> CaseBuilder<I, I1, O> when(final MappingCondition<? super I, I1> condition) {
    return new CaseBuilder<>(this, condition);
  }

  /**
   * Matches any value that equals the specified <code>value</code>.
   *
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> when(final I value) {
    return new CaseBuilder<>(this, eq(value));
  }

  /**
   * Matches any value that satisfies the specified <code>predicate</code>.
   *
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> when(final Predicate<? super I> predicate) {
    return new CaseBuilder<>(this, MappingCondition.predicate(predicate));
  }

  /**
   * Matches any value that satisfies the specified Hamcrest <code>matcher</code>.
   *
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> match(final Matcher<? super I> matcher) {
    return new CaseBuilder<>(this, MappingCondition.matcher(matcher));
  }

  /**
   * Adds an input <code>mapper</code> that will be called if none of the previous cases match. No
   * further cases can be added to a <code>PatternMatcher</code> after calling this method. Also,
   * the <code>PatternMatcher</code> will return <code>O</code> instead of
   * <code>Optional&lt;O></code> when applied to an input <code>I</code>.
   *
   * @param mapper a <code>Function</code> that transforms the input into the output
   * @return a <code>ClosedMatcher</code> that can be called using the {@link
   * ClosedMatcher#apply(I)} method
   */
  public ClosedMatcher<I, O> otherwise(final Function<? super I, ? extends O> mapper) {
    return new ClosedMatcher<>(cases, mapper);
  }

  /**
   * Adds an input <code>consumer</code> that will be called if none of the previous cases match. No
   * further cases can be added to a <code>PatternMatcher</code> after calling this method.
   *
   * @param consumer a <code>Consumer</code> that accepts the input
   * @return a <code>ClosedMatcher</code> that can be called using the {@link
   * ClosedMatcher#apply(I)} method
   */
  public ClosedMatcher<I, O> otherwiseDo(final Consumer<? super I> consumer) {
    return new ClosedMatcher<>(cases, input -> {
      consumer.accept(input);
      return null;
    });
  }

  /**
   * Adds an output <code>value</code> that will be returned if none of the previous cases match.
   * Also, the <code>PatternMatcher</code> will return <code>O</code> instead of
   * <code>Optional&lt;O></code> when applied to an input <code>I</code>.
   *
   * @return a <code>ClosedMatcher</code> that can be called using the {@link
   * ClosedMatcher#apply(I)} method
   */
  public ClosedMatcher<I, O> otherwise(final O value) {
    return new ClosedMatcher<>(cases, ignored -> value);
  }

  /**
   * Applies the <code>PatternMatcher</code> to the specified <code>input</code>. The
   * <code>then()</code> method of the first case that matches will be invoked.
   *
   * @return An <code>Optional</code> containing the result returned by the case that matched, or
   * <code>Optional.empty()</code> if none of the cases match.
   */
  @Override
  public Optional<O> apply(final I input) {
    return cases.stream()
        .map(c -> c.mapIfMatches(input))
        .filter(MaybeMatch::matches)
        .map(MaybeMatch::getValue)
        .findFirst();
  }

  /**
   * A intermediate builder for building a new case for a <code>PatternMatcher</code>. Calling
   * {@link #then(Function)}, {@link #then(O)} or {@link #thenDo(Consumer)} will build the case and
   * add it to the <code>PatternMatcher</code>.
   */
  public static final class CaseBuilder<I, I1, O> {

    private final PatternMatcher<? super I, O> patternMatcher;
    private final MappingCondition<? super I, I1> condition;

    private CaseBuilder(final PatternMatcher<? super I, O> patternMatcher,
        final MappingCondition<? super I, I1> condition) {
      this.patternMatcher = patternMatcher;
      this.condition = condition;
    }

    /**
     * Adds a <code>mapper</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param mapper a <code>Function</code> that transforms the input into the output
     * @return a new <code>PatternMatcher</code> with this case added.
     */
    public PatternMatcher<I, O> then(final Function<? super I1, ? extends O> mapper) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, mapper));
    }

    /**
     * Adds a <code>value</code> to this case that will be returned when the input matches the
     * condition of this case.
     *
     * @return a new <code>PatternMatcher</code> with this case added.
     */
    public PatternMatcher<I, O> then(final O value) {
      return new PatternMatcher<>(patternMatcher,
          new PatternMatcher.Case<>(condition, ignored -> value));
    }

    /**
     * Adds a <code>consumer</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @return a new <code>PatternMatcher</code> with this case added.
     */
    public PatternMatcher<I, O> thenDo(final Consumer<? super I1> consumer) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, input -> {
        consumer.accept(input);
        return null;
      }));
    }
  }

  public static final class Case2Builder<I, I1, I2, O> {

    private final PatternMatcher<? super I, O> patternMatcher;
    private final MappingCondition<? super I, Pair<I1, I2>> condition;

    private Case2Builder(final PatternMatcher<? super I, O> patternMatcher,
        final MappingCondition<? super I, Pair<I1, I2>> condition) {
      this.patternMatcher = patternMatcher;
      this.condition = condition;
    }

    /**
     * Adds a <code>mapper</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param mapper a <code>Function</code> that transforms the input into the output
     * @return a new <code>PatternMatcher</code> with this case added.
     */
    public PatternMatcher<I, O> then(
        final BiFunction<? super I1, ? super I2, ? extends O> mapper) {
      return new PatternMatcher<>(patternMatcher,
          new PatternMatcher.Case<>(condition, i -> mapper.apply(i.getA(), i.getB())));
    }

    /**
     * Adds a <code>consumer</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @return a new <code>PatternMatcher</code> with this case added.
     */
    public PatternMatcher<I, O> thenDo(final BiConsumer<? super I1, ? super I2> consumer) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, i -> {
        consumer.accept(i.getA(), i.getB());
        return null;
      }));
    }
  }

  private static final class Case<I, I1, O> {

    private final MappingCondition<? super I, I1> condition;
    private Function<? super I1, ? extends O> mapper;

    Case(final MappingCondition<? super I, I1> condition,
        final Function<? super I1, ? extends O> mapper) {
      this.condition = condition;
      this.mapper = mapper;
    }

    MaybeMatch<O> mapIfMatches(final I input) {
      return condition.mapIfMatches(input).map(v -> mapper.apply(v));
    }
  }

  public static class ClosedMatcher<I, O> implements Function<I, O> {

    private final List<Case<? super I, ?, O>> cases = new ArrayList<>();
    private final Function<? super I, ? extends O> lastMapper;

    ClosedMatcher(final List<Case<? super I, ?, O>> cases,
        final Function<? super I, ? extends O> lastMapper) {
      this.lastMapper = lastMapper;
      this.cases.addAll(cases);
    }

    /**
     * Applies the <code>ClosedMatcher</code> to the specified <code>input</code>. The
     * <code>then()</code> method of the first case that matches will be invoked.
     *
     * @return The result returned by the first case that matched
     */
    @Override
    public O apply(final I input) {
      final Optional<MaybeMatch<O>> matchingCase = cases.stream()
          .map(c -> c.mapIfMatches(input))
          .filter(MaybeMatch::matches)
          .findFirst();
      if (!matchingCase.isPresent()) {
        return lastMapper.apply(input);
      }
      return matchingCase.orElse(MaybeMatch.noMatch()).getValue();
    }
  }
}
