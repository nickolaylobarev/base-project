package utils;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;
import static properties.PrivateProperties.JUNIT_PARALLELISM;

public class CustomParallelExecutionConfigUtil implements ParallelExecutionConfiguration,
        ParallelExecutionConfigurationStrategy {

    private static final int PARALLELISM = Integer.parseInt(JUNIT_PARALLELISM);

    @Override
    public int getParallelism() {
        return PARALLELISM;
    }

    @Override
    public int getMinimumRunnable() {
        return 0;
    }

    @Override
    public int getMaxPoolSize() {
        return PARALLELISM;
    }

    @Override
    public int getCorePoolSize() {
        return PARALLELISM;
    }

    @Override
    public int getKeepAliveSeconds() {
        return 60;
    }

    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        return this;
    }
}
