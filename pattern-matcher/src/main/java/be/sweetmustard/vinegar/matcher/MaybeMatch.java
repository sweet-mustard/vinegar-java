package be.sweetmustard.vinegar.matcher;

import java.util.function.Function;
import java.util.function.Supplier;

public interface MaybeMatch<T> {
    NoMatch<?> NO_MATCH = new NoMatch<>();

    boolean matches();

    T getValue();

    <U> MaybeMatch<U> map(Function<? super T, ? extends U> mapper);

    static <T> MaybeMatch<T> create(boolean match, Supplier<T> value) {
        return match ? match(value.get()) : noMatch();
    }

    static <T> Match<T> match(T value) {
        return new Match<>(value);
    }

    @SuppressWarnings("unchecked")
    static <T> NoMatch<T> noMatch() {
        return (NoMatch<T>) NO_MATCH;
    }

    final class Match<T> implements MaybeMatch<T> {

        private final T value;

        Match(final T value) {
            this.value = value;
        }

        @Override
        public boolean matches() {
            return true;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public <U> MaybeMatch<U> map(final Function<? super T, ? extends U> mapper) {
            return new Match<>(mapper.apply(value));
        }
    }

    final class NoMatch<T> implements MaybeMatch<T> {
        NoMatch() {
        }

        @Override
        public boolean matches() {
            return false;
        }

        @Override
        public T getValue() {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> MaybeMatch<U> map(final Function<? super T, ? extends U> mapper) {
            return (MaybeMatch<U>) NO_MATCH;
        }
    }
}
