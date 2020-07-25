public interface Computable<A, V> {

	V compute(final A arg) throws InterruptedException;

}
