package be.sweetmustard.vinegar.matcher;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A value that is a possible match of a {@link MappingCondition}.
 *
 * @param <T> The type of the object container by this match.
 */
public interface MaybeMatch<T> {

  /**
   * Returns whether or not this value matches the condition.
   */
  boolean matches();

  /**
   * Returns the matching value, if the condition matches, otherwise null.
   */
  T getValue();

  /**
   * Maps this value to another value, if it matches.
   *
   * @param mapper the mapper function to apply to the value.
   * @param <U> the type to map to
   * @return a new <code>MaybeMatch</code> containing the mapped value.
   */
  <U> MaybeMatch<U> map(Function<? super T, ? extends U> mapper);

  /**
   * Creates a new <code>MaybeMatch</code>
   *
   * @param match whether or not it is a match.
   * @param value A supplier that supplies the value, in case it is a match
   */
  static <T> MaybeMatch<T> create(boolean match, Supplier<T> value) {
    return match ? match(value.get()) : noMatch();
  }

  /**
   * Creates a new <code>Match</code> with the specified value.
   */
  static <T> MaybeMatch<T> match(T value) {
    return new Match<>(value);
  }

  /**
   * Returns a <code>NoMatch</code>.
   */
  @SuppressWarnings("unchecked")
  static <T> MaybeMatch<T> noMatch() {
    return (MaybeMatch<T>) NoMatch.INSTANCE;
  }

  final class Match<T> implements MaybeMatch<T> {

    private final T value;

    Match(T value) {
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
    public <U> MaybeMatch<U> map(Function<? super T, ? extends U> mapper) {
      return new Match<>(mapper.apply(value));
    }
  }

  enum NoMatch implements MaybeMatch<Object> {
    INSTANCE;

    @Override
    public boolean matches() {
      return false;
    }

    @Override
    public Object getValue() {
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> MaybeMatch<U> map(Function<? super Object, ? extends U> mapper) {
      return (MaybeMatch<U>) this;
    }
  }
}
