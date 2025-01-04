package mock;

import java.util.Random;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enum representing different SSH servers with associated commands and URL patterns.
 * It provides functionality for generating SSH commands with specified ports and selecting a random server
 */

@Getter
@RequiredArgsConstructor
public enum SshServers {
    LOCALTUNNEL_ME(
            "lt -p PortPlaceholder -h https://localtunnel.me",
            Pattern.compile("your url is: (https://[\\w\\-\\.]+)")
    ),
    LOCALHOST_RUN(
            "ssh -R 80:localhost:PortPlaceholder nokey@localhost.run -o StrictHostKeyChecking=no",
            Pattern.compile("tunneled with tls termination, (https://[\\w\\-\\.]+.lhr.life)")
    ),
    PINGGY(
            "ssh -p 443 -R0:localhost:PortPlaceholder a.pinggy.io -o StrictHostKeyChecking=no",
            Pattern.compile("(https://[\\w\\-\\.]+.free.pinggy.link)")
    ),
    SERVEO(
            "ssh -R 80:localhost:PortPlaceholder serveo.net -o StrictHostKeyChecking=no",
            Pattern.compile("Forwarding HTTP traffic from (https://[\\w\\-\\.]+.serveo.net)")
    );

    private final String sshCommand;
    private final Pattern regex;

    public String[] getSshCommandWithSpecifiedPort(int port) {
        return sshCommand.replace("PortPlaceholder", String.valueOf(port)).split(" ");
    }

    public static SshServers getRandomServer() {
        SshServers[] servers = SshServers.values();
        return servers[new Random().nextInt(servers.length)];
    }
}
