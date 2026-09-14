package engine;

import engine.core.EngineConfig;
import engine.core.EngineKernel;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        EngineConfig config;
        Path cfg = Path.of("config", "engine.properties");
        try {
            config = EngineConfig.load(cfg);
        }
        catch (Exception e) {
            System.err.println("Unreadable config (" + e.getMessage() + ") -> defaults");
            config = EngineConfig.defaults();
        }
        System.out.println(config);
        EngineKernel kernel = new EngineKernel(config);
        kernel.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kernel::stop, "shutdown"));
    }
}
