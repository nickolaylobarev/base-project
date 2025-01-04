package properties;

import utils.PropertiesManagerUtil;

public class PrivateProperties {
    public static final String JUNIT_PARALLELISM = PropertiesManagerUtil.getPrivateProperty(
            "junit.parallelism", "4");
    public static final String START_WIREMOCK_IN_DOCKER = PropertiesManagerUtil.getPrivateProperty(
            "startWireMockInDocker", "false");
    public static final String USERNAME = PropertiesManagerUtil.getPrivatePropertyOrNull("username");
    public static final String PASSWORD = PropertiesManagerUtil.getPrivatePropertyOrNull("password");
}
