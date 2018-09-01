package be.sweetmustards.seeds.matcher;

import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface Condition<I, I2> {
    boolean test(final I input);

    I2 map(final I input);

    static <I, I2 extends I> Condition<I, I2> is(final Class<I2> type) {
        return new TypeCondition<>(type);
    }

    static <I> Condition<I, I> predicate(final Predicate<? super I> predicate) {
        return new PredicateCondition<>(predicate);
    }

    static <I> Condition<I, I> matcher(final Matcher<? super I> matcher) {
        return predicate(matcher::matches);
    }

    static <I> Condition<I, I> eq(final I value) {
        return predicate(i -> Objects.equals(i, value));
    }

    static <I> Condition<I, I> any() {
        return predicate(i -> true);
    }

    static Condition<String, MatchResult> regex(final String regex) {
        return new RegexCondition(regex);
    }

    static Condition<String, String> regex1(final String regex) {
        return new Regex1Condition(regex);
    }

    static Condition<String, Pair<String, String>> regex2(final String regex) {
        return new Regex2Condition(regex);
    }

    static <A, B> Condition<Pair<A, B>, Pair<A, B>> pair(Condition<A, A> condition1, Condition<B, B> condition2) {
        return new PairCondition<>(condition1, condition2);
    }

    final class TypeCondition<I, I2 extends I> implements Condition<I, I2> {
        private final Class<I2> type;

        private TypeCondition(final Class<I2> type) {
            this.type = type;
        }

        @Override
        public final boolean test(final I input) {
            return input != null && type.isAssignableFrom(input.getClass());
        }

        @Override
        public final I2 map(final I input) {
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
            return condition1.test(input.a) && condition2.test(input.b);
        }

        @Override
        public Pair<A, B> map(final Pair<A, B> input) {
            return input;
        }
    }

}
