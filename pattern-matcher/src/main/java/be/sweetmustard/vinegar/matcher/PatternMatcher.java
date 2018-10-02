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
 *
 * @param <I> the type this pattern matcher can match.
 * @param <O> the output type of the values this pattern matcher produces. Use {@link Void} if you
 * don't wish to produce any values.
 */
public final class PatternMatcher<I, O> implements Function<I, Optional<O>> {

  private final List<Case<? super I, ?, O>> cases = new ArrayList<>();

  public PatternMatcher() {
  }

  private PatternMatcher(
      PatternMatcher<? super I, O> previousMatcher,
      Case<? super I, ?, O> newCase) {
    cases.addAll(previousMatcher.cases);
    cases.add(newCase);
  }

  /**
   * Creates a mapper {@link Function} that extracts a single value from the input and applies the
   * specified <code>mapper</code> on it.
   *
   * @param extractedValueMapper the mapper <code>Function</code> to apply to the extracted value
   * @param <I> the input type to extract a value from, must implement {@link Extractable}
   * @param <E> the type of the extracted value
   * @param <O> the output type of the mapper function
   * @return A mapper {@link Function} to use in a {@link CaseBuilder#then(Function)} or {@link
   * #otherwise(Function)} clause.
   * @see Extractable
   */
  public static <I extends Extractable<E>, E, O> Function<I, O> extract(
      final Function<? super E, ? extends O> extractedValueMapper) {
    return input -> extractedValueMapper.apply(input.extract());
  }

  /**
   * Creates a mapper {@link Function} that extracts a {@link Pair} of values from the input and
   * applies the specified <code>mapper</code> on it.
   *
   * @param extractedValuesMapper the mapper {@link BiFunction} to apply to the extracted values
   * @param <I> the input type to extract a value from, must implement {@link Extractable}
   * @param <E1> the type of the first extracted value
   * @param <E2> the type of the second extracted value
   * @param <O> the output type of the mapper function
   * @return A mapper {@link Function} to use in a {@link CaseBuilder#then(Function)} or {@link
   * #otherwise(Function)} clause.
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
   * Creates a mapper {@link Function} that extracts a {@link Triplet} of values from the input and
   * applies the specified <code>mapper</code> on it.
   *
   * @param extractedValuesMapper the mapper {@link TriFunction} to apply to the extracted values
   * @param <I> the input type to extract a value from, must implement {@link Extractable}
   * @param <E1> the type of the first extracted value
   * @param <E2> the type of the second extracted value
   * @param <E3> the type of the third extracted value
   * @param <O> the output type of the mapper function
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
   * @param condition the condition to satisfy
   * @param <I1> The type of the first extracted value.
   * @param <I2> The type of the second extracted value.
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   * @see MappingCondition
   */
  public <I1, I2> Case2Builder<I, I1, I2, O> whenPair(
      MappingCondition<? super I, Pair<I1, I2>> condition) {
    return new Case2Builder<>(this, condition);
  }

  /**
   * Matches any value that satisfies the specified <code>condition</code>. The condition can
   * optionally check for a subtype of the input type or extract a value of the input. In that case,
   * the parameter <code>I1</code> will differ from the original input type <code>I</code>.
   *
   * @param condition the condition to satisfy
   * @param <I1> The type of the input type or the extracted value type, in case the condition
   * performs type checking or value extraction.
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   * @see MappingCondition
   */
  public <I1> CaseBuilder<I, I1, O> when(MappingCondition<? super I, I1> condition) {
    return new CaseBuilder<>(this, condition);
  }

  /**
   * Matches any value that equals the specified <code>value</code>.
   *
   * @param value the value to match
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> when(I value) {
    return new CaseBuilder<>(this, eq(value));
  }

  /**
   * Matches any value that satisfies the specified <code>predicate</code>.
   *
   * @param predicate the predicate to match
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> when(Predicate<? super I> predicate) {
    return new CaseBuilder<>(this, MappingCondition.predicate(predicate));
  }

  /**
   * Matches any value that satisfies the specified Hamcrest <code>matcher</code>.
   *
   * @param matcher the matcher to match
   * @return A <code>CaseBuilder</code> for chaining the result of this case
   */
  public CaseBuilder<I, I, O> match(Matcher<? super I> matcher) {
    return new CaseBuilder<>(this, MappingCondition.matcher(matcher));
  }

  /**
   * Adds an input <code>mapper</code> that will be called if none of the previous cases match. No
   * further cases can be added to a <code>PatternMatcher</code> after calling this method. Also,
   * the <code>PatternMatcher</code> will return <code>O</code> instead of
   * <code>Optional&lt;O&gt;</code> when applied to an input <code>I</code>.
   *
   * @param mapper a <code>Function</code> that transforms the input into the output
   * @return a <code>ClosedMatcher</code> that can be called using the {@link
   * ClosedMatcher#apply(Object)} method
   */
  public ClosedMatcher<I, O> otherwise(Function<? super I, ? extends O> mapper) {
    return new ClosedMatcher<>(cases, mapper);
  }

  /**
   * Adds an input <code>consumer</code> that will be called if none of the previous cases match. No
   * further cases can be added to a {@link PatternMatcher} after calling this method.
   *
   * @param consumer a {@link Consumer} that accepts the input
   * @return a {@link ClosedMatcher} that can be called using the {@link
   * ClosedMatcher#apply(Object)} method
   */
  public ClosedMatcher<I, O> otherwiseDo(Consumer<? super I> consumer) {
    return new ClosedMatcher<>(cases, input -> {
      consumer.accept(input);
      return null;
    });
  }

  /**
   * Adds an output <code>value</code> that will be returned if none of the previous cases match.
   * Also, the {@link PatternMatcher} will return <code>O</code> instead of {@link
   * Optional}<code>&lt;O&gt;</code> when applied to an input <code>I</code>.
   *
   * @param value the value to return
   * @return a {@link ClosedMatcher} that can be called using the {@link
   * ClosedMatcher#apply(Object)} method
   */
  public ClosedMatcher<I, O> otherwise(O value) {
    return new ClosedMatcher<>(cases, ignored -> value);
  }

  /**
   * Applies the {@link PatternMatcher} to the specified <code>input</code>. The {@link
   * CaseBuilder#then(Function)} method of the first case that matches will be invoked.
   *
   * @return An {@link Optional} containing the result returned by the case that matched, or {@link
   * Optional#empty()} if none of the cases match.
   */
  @Override
  public Optional<O> apply(I input) {
    return cases.stream()
        .map(c -> c.mapIfMatches(input))
        .filter(MaybeMatch::matches)
        .map(MaybeMatch::getValue)
        .findFirst();
  }

  /**
   * A intermediate builder for building a new case for a {@link PatternMatcher}. Calling {@link
   * #then(Function)}, {@link #then(Object)} or {@link #thenDo(Consumer)} will build the case and
   * add it to the {@link PatternMatcher}.
   */
  public static final class CaseBuilder<I, I1, O> {

    private final PatternMatcher<? super I, O> patternMatcher;
    private final MappingCondition<? super I, I1> condition;

    private CaseBuilder(
        PatternMatcher<? super I, O> patternMatcher,
        MappingCondition<? super I, I1> condition) {
      this.patternMatcher = patternMatcher;
      this.condition = condition;
    }

    /**
     * Adds a <code>mapper</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param mapper a {@link Function} that transforms the input into the output
     * @return a new {@link PatternMatcher} with this case added.
     */
    public PatternMatcher<I, O> then(Function<? super I1, ? extends O> mapper) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, mapper));
    }

    /**
     * Adds a <code>value</code> to this case that will be returned when the input matches the
     * condition of this case.
     *
     * @param value the value to return from this case
     * @return a new {@link PatternMatcher} with this case added.
     */
    public PatternMatcher<I, O> then(O value) {
      return new PatternMatcher<>(patternMatcher,
          new PatternMatcher.Case<>(condition, ignored -> value));
    }

    /**
     * Adds a <code>consumer</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param consumer the consumer to call from this case
     * @return a new {@link PatternMatcher} with this case added.
     */
    public PatternMatcher<I, O> thenDo(Consumer<? super I1> consumer) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, input -> {
        consumer.accept(input);
        return null;
      }));
    }
  }

  public static final class Case2Builder<I, I1, I2, O> {

    private final PatternMatcher<? super I, O> patternMatcher;
    private final MappingCondition<? super I, Pair<I1, I2>> condition;

    private Case2Builder(
        PatternMatcher<? super I, O> patternMatcher,
        MappingCondition<? super I, Pair<I1, I2>> condition) {
      this.patternMatcher = patternMatcher;
      this.condition = condition;
    }

    /**
     * Adds a <code>mapper</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param mapper a {@link Function} that transforms the input into the output
     * @return a new {@link PatternMatcher} with this case added.
     */
    public PatternMatcher<I, O> then(BiFunction<? super I1, ? super I2, ? extends O> mapper) {
      return new PatternMatcher<>(patternMatcher,
          new PatternMatcher.Case<>(condition, i -> mapper.apply(i.getA(), i.getB())));
    }

    /**
     * Adds a <code>consumer</code> to this case that will be called when the input matches the
     * condition of this case.
     *
     * @param consumer the {@link BiConsumer} to call from this case
     * @return a new {@link PatternMatcher} with this case added.
     */
    public PatternMatcher<I, O> thenDo(BiConsumer<? super I1, ? super I2> consumer) {
      return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, i -> {
        consumer.accept(i.getA(), i.getB());
        return null;
      }));
    }
  }

  private static final class Case<I, I1, O> {

    private final MappingCondition<? super I, I1> condition;
    private final Function<? super I1, ? extends O> mapper;

    Case(
        MappingCondition<? super I, I1> condition,
        Function<? super I1, ? extends O> mapper) {
      this.condition = condition;
      this.mapper = mapper;
    }

    MaybeMatch<O> mapIfMatches(I input) {
      return condition.mapIfMatches(input).map(mapper::apply);
    }
  }

  public static class ClosedMatcher<I, O> implements Function<I, O> {

    private final List<Case<? super I, ?, O>> cases = new ArrayList<>();
    private final Function<? super I, ? extends O> lastMapper;

    ClosedMatcher(
        List<Case<? super I, ?, O>> cases,
        Function<? super I, ? extends O> lastMapper) {
      this.lastMapper = lastMapper;
      this.cases.addAll(cases);
    }

    /**
     * Applies the {@link ClosedMatcher} to the specified <code>input</code>. The {@link
     * CaseBuilder#then(Function)} method of the first case that matches will be invoked.
     *
     * @param input the input to apply this matcher on
     * @return The result returned by the first case that matched
     */
    @Override
    public O apply(I input) {
      Optional<MaybeMatch<O>> matchingCase = cases.stream()
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
