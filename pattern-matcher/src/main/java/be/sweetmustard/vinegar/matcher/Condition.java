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

import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * A condition to pass to {@link PatternMatcher#when(Condition)}. The condition can optionally check for a subtype
 * of the input type or extract a value of the input. In that case, the parameter <code>I1</code> will differ from
 * the original input type <code>I</code>.
 *
 * @param <I>  the original input type
 * @param <I1> the mapped input type (often the same as <code>I</code>)
 */
public interface Condition<I, I1> {
    /**
     * Returns whether the specified input matches this condition.
     */
    boolean test(final I input);

    /**
     * Maps the input in case this condition performs type checking or extracts a value.
     */
    I1 map(final I input);

    /**
     * Creates a condition that checks whether the input is of type <code>I1</code>. If so, the condition will cast
     * the input to the type <code>I1</code>
     */
    static <I, I1 extends I> Condition<I, I1> is(final Class<I1> type) {
        return new TypeCondition<>(type);
    }

    /**
     * Create a condition that checks whether the input satisfies the specified predicate. This condition does not
     * perform any mapping and will return the input as is.
     */
    static <I> Condition<I, I> predicate(final Predicate<? super I> predicate) {
        return new PredicateCondition<>(predicate);
    }

    /**
     * Create a condition that checks whether the input satisfies the specified Hamcrest {@link Matcher}. This condition does not
     * perform any mapping and will return the input as is.
     */
    static <I> Condition<I, I> matcher(final Matcher<? super I> matcher) {
        return predicate(matcher::matches);
    }

    /**
     * Create a condition that checks whether the input is equal to the specified value using {@link Object#equals(Object)}.
     * This condition does not perform any mapping and will return the input as is.
     */
    static <I> Condition<I, I> eq(final I value) {
        return predicate(i -> Objects.equals(i, value));
    }

    /**
     * Creates a condition that will match any value. This condition does not perform any mapping and will return the input as is.
     */
    static <I> Condition<I, I> any() {
        return predicate(i -> true);
    }

    /**
     * Creates a condition that matches strings with the specified regex. This condition maps the input to a {@link MatchResult}.
     */
    static Condition<String, MatchResult> regex(final String regex) {
        return new RegexCondition(regex);
    }

    /**
     * Creates a condition that matches strings with the specified regex. This condition maps the input to the first matching group.
     */
    static Condition<String, String> regex1(final String regex) {
        return new Regex1Condition(regex);
    }

    /**
     * Creates a condition that matches strings with the specified regex. This condition maps the input to  a {@link Pair} of
     * the first two matching groups.
     */
    static Condition<String, Pair<String, String>> regex2(final String regex) {
        return new Regex2Condition(regex);
    }

    /**
     * Creates a condition that matches a {@link Pair}. It checks both values of the pair with the specified conditions.
     * This condition does not perform any mapping and will return the input as is.
     *
     * @param condition1 the condition to apply to the first value of the pair
     * @param condition2 the condition to apply to the second value of the pair
     */
    static <A, B> Condition<Pair<A, B>, Pair<A, B>> pair(Condition<A, A> condition1, Condition<B, B> condition2) {
        return new PairCondition<>(condition1, condition2);
    }

    final class TypeCondition<I, I1 extends I> implements Condition<I, I1> {
        private final Class<I1> type;

        private TypeCondition(final Class<I1> type) {
            this.type = type;
        }

        @Override
        public final boolean test(final I input) {
            return input != null && type.isAssignableFrom(input.getClass());
        }

        @Override
        public final I1 map(final I input) {
            return type.cast(input);
        }
    }

    final class PredicateCondition<I> implements Condition<I, I> {
        private final Predicate<? super I> predicate;

        private PredicateCondition(final Predicate<? super I> predicate) {
            this.predicate = predicate;
        }

        @Override
        public final boolean test(final I input) {
            return predicate.test(input);
        }

        @Override
        public final I map(final I input) {
            return input;
        }
    }

    final class RegexCondition implements Condition<String, MatchResult> {
        private final Pattern pattern;
        private final ThreadLocal<Map<String, MatchResult>> result = ThreadLocal.withInitial(HashMap::new);

        RegexCondition(final String regex) {
            pattern = Pattern.compile(regex);
        }

        @Override
        public boolean test(final String input) {
            java.util.regex.Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();
            result.get().put(input, matcher.toMatchResult());
            return matches;
        }

        @Override
        public MatchResult map(final String input) {
            return result.get().remove(input);
        }
    }

    final class Regex1Condition implements Condition<String, String> {
        private final RegexCondition delegateCondition;

        Regex1Condition(final String regex) {
            this.delegateCondition = new RegexCondition(regex);
        }

        @Override
        public boolean test(final String input) {
            return delegateCondition.test(input);
        }

        @Override
        public String map(final String input) {
            return delegateCondition.map(input).group(1);
        }
    }

    final class Regex2Condition implements Condition<String, Pair<String, String>> {
        private final RegexCondition delegateCondition;

        Regex2Condition(final String regex) {
            this.delegateCondition = new RegexCondition(regex);
        }

        @Override
        public boolean test(final String input) {
            return delegateCondition.test(input);
        }

        @Override
        public Pair<String, String> map(final String input) {
            MatchResult result = delegateCondition.map(input);
            return new Pair<>(result.group(1), result.group(2));
        }
    }

    final class PairCondition<A, B> implements Condition<Pair<A, B>, Pair<A, B>> {
        private final Condition<A, A> condition1;
        private final Condition<B, B> condition2;

        PairCondition(final Condition<A, A> condition1, final Condition<B, B> condition2) {
            this.condition1 = condition1;
            this.condition2 = condition2;
        }

        @Override
        public boolean test(final Pair<A, B> input) {
            return condition1.test(input.getA()) && condition2.test(input.getB());
        }

        @Override
        public Pair<A, B> map(final Pair<A, B> input) {
            return input;
        }
    }

}
