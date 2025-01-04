package allure;

import io.qameta.allure.Allure;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.awaitility.Awaitility;

public class AllureAttachmentUtil {

    public static void addZipAttachment(String reportIdentifier) throws IOException {
        String reportPath = ".jmeter-reports/" + reportIdentifier;

        Awaitility.await().untilAsserted(() -> {
            if (!Files.exists(Paths.get(reportPath))) {
                throw new AssertionError("Report path does not exist: " + reportPath);
            }
        });

        zipFolder(reportPath, reportPath + ".zip");

        try (InputStream inputStream = new FileInputStream(reportPath + ".zip")) {
            Allure.addAttachment("LoadTestsReport", "application/zip", inputStream, "zip");
        }
    }

    private static void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        File zipFile = new File(zipFilePath);

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            zipFiles(sourceDir, sourceDir, zos);
        }
    }

    private static void zipFiles(File rootDir, File sourceFile, ZipOutputStream zos) throws IOException {
        if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    zipFiles(rootDir, file, zos);
                }
            }
        } else {
            String relativePath = rootDir.toURI().relativize(sourceFile.toURI()).getPath();
            zos.putNextEntry(new ZipEntry(relativePath));
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
            }
            zos.closeEntry();
        }
    }
}
