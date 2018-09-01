package be.sweetmustards.seeds.matcher;

@FunctionalInterface
public interface TriFunction<I1, I2, I3, O> {
    O apply(final I1 input1, final I2 input2, final I3 input3);
}
