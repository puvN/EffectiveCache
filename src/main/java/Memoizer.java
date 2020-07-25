import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Memoizer<A, V> implements Computable<A, V> {

	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();

	private final Computable<A, V> c;

	public Memoizer(Computable<A, V> c) {
		this.c = c;
	}

	@Override
	public V compute(final A arg) throws InterruptedException {
		while (true) {
			Future<V> f = cache.get(arg);
			if (f == null) {
				Callable<V> vCallable = () -> c.compute(arg);
				FutureTask<V> vFutureTask = new FutureTask<>(vCallable);
				f = cache.putIfAbsent(arg, vFutureTask);
				if (f == null) {
					f = vFutureTask;
					vFutureTask.run();
				}
			}
			try {
				return f.get();
			} catch (CancellationException e) {
				cache.remove(arg, f);
			} catch (ExecutionException e) {
				throw new InterruptedException(e.getMessage());
			}
		}
	}

	//Only for tests
	public V getByArg(A arg) throws ExecutionException, InterruptedException {
		return cache.get(arg).get();
	}

	//Only for tests
	public long size() {
		return cache.entrySet().size();
	}
}
