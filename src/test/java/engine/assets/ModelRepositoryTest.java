package engine.assets;

import engine.assets.ModelRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelRepositoryTest {
    private static final Path MODELS = Path.of("assets", "models");

    ModelRepositoryTest() {
    }

    @Test
    void acquireCachesAndCountsRefs() throws Exception {
        ModelRepository repo = new ModelRepository(MODELS);
        List a = repo.acquire("cube");
        List b = repo.acquire("cube");
        Assertions.assertSame((Object)a, (Object)b, (String)"2nd acquire = same instance (cache)");
        Assertions.assertEquals((int)2, (int)repo.refCount("cube"));
        repo.release("cube");
        Assertions.assertEquals((int)1, (int)repo.refCount("cube"));
        Assertions.assertTrue((boolean)repo.isCached("cube"));
        repo.release("cube");
        Assertions.assertEquals((int)0, (int)repo.refCount("cube"));
        Assertions.assertFalse((boolean)repo.isCached("cube"), (String)"unloaded at 0 refs");
        List c = repo.acquire("cube");
        Assertions.assertNotSame((Object)a, (Object)c, (String)"reloaded after unload");
        repo.release("cube");
    }

    @Test
    void missingModelThrows() {
        ModelRepository repo = new ModelRepository(MODELS);
        Assertions.assertThrows(Exception.class, () -> repo.acquire("n_existe_pas_123"));
        Assertions.assertFalse((boolean)repo.isCached("n_existe_pas_123"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Test
    void threadSafeConcurrentAcquire() throws Exception {
        ModelRepository repo = new ModelRepository(MODELS);
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            ArrayList<Future<List>> futures = new ArrayList<Future<List>>();
            for (int i = 0; i < threads; ++i) {
                futures.add(pool.submit(() -> repo.acquire("pyramid")));
            }
            for (Future future : futures) {
                Assertions.assertNotNull(future.get(10L, TimeUnit.SECONDS));
            }
            Assertions.assertEquals((int)threads, (int)repo.refCount("pyramid"));
        }
        finally {
            pool.shutdownNow();
        }
        for (int i = 0; i < threads; ++i) {
            repo.release("pyramid");
        }
        Assertions.assertFalse((boolean)repo.isCached("pyramid"));
    }

    @Test
    void availableModelsListsObj() throws Exception {
        ModelRepository repo = new ModelRepository(MODELS);
        List ids = repo.availableModels();
        Assertions.assertTrue((boolean)ids.contains("cube"));
        Assertions.assertTrue((boolean)ids.contains("pyramid"));
        Assertions.assertTrue((boolean)ids.contains("plane"));
    }
}
