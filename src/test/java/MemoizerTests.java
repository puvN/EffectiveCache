import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemoizerTests {

	private final Computable<BigInteger, BigInteger[]> c = this::work;

	private final Memoizer<BigInteger, BigInteger[]> cache = new Memoizer<>(this.c);

	@Test
	public void storeNewKeyInCache() throws InterruptedException {
		BigInteger arg = new BigInteger("3");
		cache.compute(arg);
		BigInteger arg2 = new BigInteger("200");
		cache.compute(arg2);
		BigInteger arg3 = new BigInteger("20");
		cache.compute(arg3);

		BigInteger[] v = cache.compute(arg);
		assertEquals(1, v.length);
		assertEquals(new BigInteger("27"), v[0]);
	}

	@Test
	public void basicConcurrencyMemoizerTest() throws InterruptedException, ExecutionException {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		BigInteger testNumber = new BigInteger("2");

		List<Callable<BigInteger[]>> tasks = new ArrayList<>();
		for (int i = 0; i < 100 ; i++) {
			tasks.add(() -> cache.compute(testNumber));
		}
		executorService.invokeAll(tasks);
		assertEquals(1L, cache.size());
		assertEquals(new BigInteger("4"), cache.getByArg(testNumber)[0]);
		assertEquals(new BigInteger("4"), cache.compute(testNumber)[0]);
	}

	private BigInteger[] work(BigInteger arg) {
		arg = arg.pow(arg.intValue());
		BigInteger[] result = new BigInteger[1];
		result[0] = arg;
		return result;
	}

}
