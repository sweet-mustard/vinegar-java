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

package be.sweetmustards.seeds.matcher;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static be.sweetmustards.seeds.matcher.Condition.matcher;
import static be.sweetmustards.seeds.matcher.Condition.predicate;

public final class PatternMatcher<I, O> implements Function<I, Optional<O>> {
    private final List<Case<? super I, ?, O>> cases = new ArrayList<>();

    public PatternMatcher() {
    }

    private PatternMatcher(final PatternMatcher<? super I, O> previousMatcher, final Case<? super I, ?, O> newCase) {
        cases.addAll(previousMatcher.cases);
        cases.add(newCase);
    }

    private static <I, O> Function<I, O> value(final O value) {
        return ignored -> value;
    }

    public static <I, O> Function<I, O> consumer(final Consumer<? super I> consumer) {
        return input -> {
            consumer.accept(input);
            return null;
        };
    }

    public static <I extends Extractable<D>, D, O> Function<I, O> extract(final Function<? super D, ? extends O> mapper) {
        return input -> mapper.apply(input.extract());
    }

    public static <I extends Extractable<Pair<E1, E2>>, E1, E2, O> Function<I, O> extract(
            final BiFunction<? super E1, ? super E2, ? extends O> mapper) {
        return input -> {
            Pair<E1, E2> pair = input.extract();
            return mapper.apply(pair.a, pair.b);
        };
    }

    public static <I extends Extractable<Triplet<E1, E2, E3>>, E1, E2, E3, O> Function<I, O> extract(
            final TriFunction<? super E1, ? super E2, ? super E3, ? extends O> mapper) {
        return input -> {
            Triplet<E1, E2, E3> triplet = input.extract();
            return mapper.apply(triplet.a, triplet.b, triplet.c);
        };
    }

    public final <I1, I2> Case2Builder<I, I1, I2, O> when2(final Condition<? super I, Pair<I1, I2>> condition) {
        return new Case2Builder<>(this, condition);
    }

    /**
     * Matches any value that satisfies the specified <code>condition</code>. The condition can optionally check for a subtype
     * of the input type or extract a value of the input. In those cases, the parameter <code>I2</code> will differ from
     * the original input type <code>I</code>.
     *
     * @param <I2> The subtype of the input type or the extracted value type, in case the condition performs
     *             type checking or value extraction.
     * @return A <code>CaseBuilder</code> for chaining the result of this case
     */
    public final <I2> CaseBuilder<I, I2, O> when(final Condition<? super I, I2> condition) {
        return new CaseBuilder<>(this, condition);
    }

    /**
     * Matches any value that satisfies the specified <code>predicate</code>.
     *
     * @return A <code>CaseBuilder</code> for chaining the result of this case
     */
    public final CaseBuilder<I, I, O> when(final Predicate<? super I> predicate) {
        return new CaseBuilder<>(this, predicate(predicate));
    }

    /**
     * Matches any value that satisfies the specified Hamcrest <code>matcher</code>.
     *
     * @return A <code>CaseBuilder</code> for chaining the result of this case
     */
    public final CaseBuilder<I, I, O> when(final Matcher<? super I> matcher) {
        return new CaseBuilder<>(this, matcher(matcher));
    }

    /**
     * Adds an input <code>mapper</code> that will be called if none of the previous cases match. No further cases
     * can be added to a <code>PatternMatcher</code> after calling this method. Also, the <code>PatternMatcher</code>
     * will return <code>O</code> instead of <code>Optional&lt;O></code> when applied to an input <code>I</code>.
     *
     * @param mapper a <code>Function</code> that transforms the input into the output
     * @return a <code>ClosedMatcher</code> that can be called using the {@link ClosedMatcher#apply(I)} method
     */
    public final ClosedMatcher<I, O> otherwise(final Function<? super I, ? extends O> mapper) {
        return new ClosedMatcher<>(cases, mapper);
    }

    /**
     * Adds an output <code>value</code> that will be returned if none of the previous cases match. Also,
     * the <code>PatternMatcher</code> will return <code>O</code> instead of <code>Optional&lt;O></code> when applied
     * to an input <code>I</code>.
     *
     * @return a <code>ClosedMatcher</code> that can be called using the {@link ClosedMatcher#apply(I)} method
     */
    public final ClosedMatcher<I, O> otherwise(final O value) {
        return new ClosedMatcher<>(cases, value(value));
    }

    /**
     * Applies the <code>PatternMatcher</code> to the specified <code>input</code>. The <code>then()</code> method of
     * the first case that matches will be invoked.
     *
     * @return An <code>Optional</code> containing the result returned by the case that matched, or
     * <code>Optional.empty()</code> if none of the cases match.
     */
    @Override
    public final Optional<O> apply(final I input) {
        return cases.stream()
                .filter(c -> c.matches(input))
                .map(c -> c.map(input))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static final class CaseBuilder<I, I2, O> {
        private final PatternMatcher<? super I, O> patternMatcher;
        private final Condition<? super I, I2> condition;

        private CaseBuilder(final PatternMatcher<? super I, O> patternMatcher, final Condition<? super I, I2> condition) {
            this.patternMatcher = patternMatcher;
            this.condition = condition;
        }

        /**
         * Adds a <code>mapper</code> to this case that will be called when the input matches the condition of this case.
         *
         * @param mapper a <code>Function</code> that transforms the input into the output
         * @return a new <code>PatternMatcher</code> with this case added.
         */
        public final PatternMatcher<I, O> then(final Function<? super I2, ? extends O> mapper) {
            return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, mapper));
        }

        /**
         * Adds a <code>value</code> to this case that will be returned when the input matches the condition of this case.
         *
         * @return a new <code>PatternMatcher</code> with this case added.
         */
        public final PatternMatcher<I, O> then(final O value) {
            return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, value(value)));
        }
    }

    public static final class Case2Builder<I, I1, I2, O> {
        private final PatternMatcher<? super I, O> patternMatcher;
        private final Condition<? super I, Pair<I1, I2>> condition;

        private Case2Builder(final PatternMatcher<? super I, O> patternMatcher, final Condition<? super I, Pair<I1, I2>> condition) {
            this.patternMatcher = patternMatcher;
            this.condition = condition;
        }

        /**
         * Adds a <code>mapper</code> to this case that will be called when the input matches the condition of this case.
         *
         * @param mapper a <code>Function</code> that transforms the input into the output
         * @return a new <code>PatternMatcher</code> with this case added.
         */
        public final PatternMatcher<I, O> then(final BiFunction<? super I1, ? super I2, ? extends O> mapper) {
            return new PatternMatcher<>(patternMatcher, new PatternMatcher.Case<>(condition, i -> mapper.apply(i.a, i.b)));
        }
    }

    private static final class Case<I, I2, O> {
        private final Condition<? super I, I2> condition;
        private Function<? super I2, ? extends O> mapper;

        Case(final Condition<? super I, I2> condition, final Function<? super I2, ? extends O> mapper) {
            this.condition = condition;
            this.mapper = mapper;
        }

        final boolean matches(final I input) {
            return condition.test(input);
        }

        final O map(final I input) {
            return mapper.apply(condition.map(input));
        }
    }

    public static class ClosedMatcher<I, O> implements Function<I, O> {
        private final List<Case<? super I, ?, O>> cases = new ArrayList<>();
        private final Function<? super I, ? extends O> lastMapper;

        ClosedMatcher(final List<Case<? super I, ?, O>> cases, final Function<? super I, ? extends O> lastMapper) {
            this.lastMapper = lastMapper;
            this.cases.addAll(cases);
        }

        /**
         * Applies the <code>ClosedMatcher</code> to the specified <code>input</code>. The <code>then()</code> method of
         * the first case that matches will be invoked.
         *
         * @return The result returned by the first case that matched
         */
        @Override
        public final O apply(final I input) {
            return cases.stream()
                    .filter(c -> c.matches(input))
                    .map(c -> c.map(input))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> lastMapper.apply(input));
        }
    }
}
