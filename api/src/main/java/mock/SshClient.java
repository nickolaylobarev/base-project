package mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;

/**
 * A client for managing SSH tunnels, handling the creation and termination of SSH processes.
 * It starts an SSH tunnel, retrieves its URL, and ensures proper cleanup upon closing
 */

@Slf4j
public class SshClient implements AutoCloseable {
    private final SshServers sshServer;
    private final Process sshProcess;
    private final String url;

    public SshClient(int port) throws IOException {
        this.sshServer = SshServers.getRandomServer();
        this.sshProcess = new ProcessBuilder(sshServer.getSshCommandWithSpecifiedPort(port)).start();
        this.url = createSshUrl();
    }

    public String getSshServerUrl() {
        if (url != null) {
            return url;
        } else {
            throw new NullPointerException("URL was not initialized");
        }
    }

    @Override
    public void close() {
        log.info("Stopping SSH tunnel");
        sshProcess.destroyForcibly();
        try {
            sshProcess.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("SSH tunnel has been stopped.");
    }

    private String createSshUrl() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sshProcess.getInputStream()))) {
            String consoleLine;
            do {
                consoleLine = reader.readLine();
                if (consoleLine == null) {
                    throw new IllegalStateException("SSH process output is empty.");
                }
            } while (!sshServer.getRegex().matcher(consoleLine).find());

            Matcher matcher = sshServer.getRegex().matcher(consoleLine);
            if (matcher.find()) {
                String sshUrl = matcher.group(1);
                log.info("SSH tunnel {} is started with URL {}", sshServer.name(), sshUrl);
                return sshUrl;
            }
        } catch (IOException e) {
            log.warn("Impossible to determine SSH tunnel URL for {}", sshServer.name());
        }
        return null;
    }
}
