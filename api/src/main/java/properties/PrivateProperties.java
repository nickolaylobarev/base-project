package properties;

import utils.PropertiesManagerUtil;

public class PrivateProperties {
    public static final String junitParallelism = PropertiesManagerUtil.getPrivateProperty("junit.parallelism", "4");
    public static final String username = PropertiesManagerUtil.getPrivatePropertyOrNull("username");
    public static final String password = PropertiesManagerUtil.getPrivatePropertyOrNull("password");
}
