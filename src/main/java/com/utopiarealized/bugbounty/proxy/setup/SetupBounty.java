package com.utopiarealized.bugbounty.proxy.setup;

import com.utopiarealized.bugbounty.proxy.CachedDnsResolver;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SetupBounty {

    public static final String DIRECTORY = "/Users/m/bug-bounty";

    private static final String CERTIFICATE_DIR = "certificates";

    private final String DNS_FILE = "dnsentries.db";
    private final String HOSTS_FILE = "hosts";

    private final String DEFAULT_HOSTS = "127.0.0.1\tlocalhost\n" +
            "255.255.255.255\tbroadcasthost\n" +
            "::1\tlocalhost\n";

    private final String CERT_SCRIPT_RESOURCE = "/create-certificate.sh";
    private final String KEYSTORE_SCRIPT_RESOURCE = "/create-keystore.sh";

    private final String SETUP_SCRIPT = "setup.sh";

    private final String keystoreCommand = "." + KEYSTORE_SCRIPT_RESOURCE + " #1 #2\n";
    private final String scriptCommand = "." + CERT_SCRIPT_RESOURCE + " #1 #2 #3\n";

    private String rootDirectory;
    private String bountyContext;
    private String certificateDirectory;
    private boolean isNew = false;

    final String baseDir;


    public static String getBaseDirectory(final String bounty) {
        return DIRECTORY + "/" + bounty;
    }

//This is messy
    public static String getKeystoreLocation(final String bounty) {
        return DIRECTORY + "/" + bounty + "/" + CERTIFICATE_DIR + "/" + bounty +".jks";
    }
    public SetupBounty(final String bountyContext) {
        this.rootDirectory = DIRECTORY;
        this.bountyContext = bountyContext;

        baseDir = getBaseDirectory(bountyContext);
        File directory = new File(baseDir);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new RuntimeException("Failed to create directory " + baseDir);
            }
            isNew = true;

            directory = new File(baseDir, CERTIFICATE_DIR);
            directory.mkdir();
        }
        certificateDirectory = baseDir + "/" + CERTIFICATE_DIR;

    }

    public String getBaseDir(){
        return baseDir;
    }

    public boolean isNew() {
        return isNew;
    }

    private void writeBaseScripts(final String directory) {
        try {
            File writeFile = new File(directory, CERT_SCRIPT_RESOURCE);
            String writeString = IOUtils.toString(this.getClass().getResourceAsStream(CERT_SCRIPT_RESOURCE), StandardCharsets.UTF_8);
            FileOutputStream fos = new FileOutputStream(writeFile);
            IOUtils.write(writeString.getBytes(), fos);
            fos.close();
            writeFile.setExecutable(true);

            writeFile = new File(directory, KEYSTORE_SCRIPT_RESOURCE);
            writeString = IOUtils.toString(this.getClass().getResourceAsStream(KEYSTORE_SCRIPT_RESOURCE), StandardCharsets.UTF_8);
            fos = new FileOutputStream(writeFile);
            IOUtils.write(writeString.getBytes(), fos);
            fos.close();
            writeFile.setExecutable(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed writing base scripts", e);
        }
    }

    /*
    Steps
    1.) Ask bounty name
    2.) If DNE
        - Ask for list of hosts
        - Create directory
        - Create dnsentries.db
        - Create hosts
        - Create script to create certificates/jks
     */

    public Map<String, String> getDNSEntries() {
        try {
            File file = new File(baseDir, DNS_FILE);
            if (file.exists()) {
                final Map<String, String> returnMe = new HashMap<>();
                final String fileContent = IOUtils.toString(new FileInputStream(file));
                final String[] entries = fileContent.split("\n");
                for (final String entry : entries) {
                    final String[] hostAndIp = entry.split("=");
                    final String host = hostAndIp[0].trim();
                    final String ip = hostAndIp[1].trim();
                    returnMe.put(host, ip);
                }
                return returnMe;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Could not getDNSEntries from " + DNS_FILE, e);
        }
    }

    public void writeDNSFile(final String commaDelimitedEntries) {
        try {
            final StringBuilder stringBuilder = new StringBuilder();
            final String[] entries = commaDelimitedEntries.split(",");
            for (final String entry : entries) {
                final String host = entry.trim();
                final String ip = CachedDnsResolver.resolveUncached(host).getHostAddress();
                stringBuilder.append(host)
                        .append(" = ")
                        .append(ip)
                        .append("\n");
            }
            // Remove the ;\n
            stringBuilder.setLength(stringBuilder.length() - 1);
            File file = new File(baseDir, DNS_FILE);
            FileOutputStream fileOutputStreamStream = new FileOutputStream(file);
            IOUtils.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), fileOutputStreamStream);
        } catch (Exception uke) {
            throw new RuntimeException("Could not write DNS File with " + commaDelimitedEntries, uke);
        }
    }

    public void writeHostFile() {
        try {
            final Map<String, String> entries = getDNSEntries();
            final StringBuilder stringBuilder = new StringBuilder(DEFAULT_HOSTS);
            for (final Map.Entry<String, String> entryObject : entries.entrySet()) {
                stringBuilder.append("127.0.0.1\t")
                        .append(entryObject.getKey())
                        .append("\n");
            }
            File file = new File(baseDir, HOSTS_FILE);
            FileOutputStream fileOutputStreamStream = new FileOutputStream(file);
            IOUtils.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), fileOutputStreamStream);
        } catch (Exception e) {
            throw new RuntimeException("Error writing hosts file ", e);
        }
    }

    public void writeFiles(final String hosts) {
        this.writeDNSFile(hosts);
        this.writeHostFile();
        this.writeBaseScripts(baseDir);
        this.writeSetupScript(baseDir);
/*
# $1 is the target directory for the certificate
# $2 is the name of the bounty
# $3 is the host
 */

    }

    private void writeSetupScript(final String baseDir) {
        try {
            final StringBuilder scriptContents = new StringBuilder(
                    keystoreCommand.replaceAll("#1", certificateDirectory).replaceAll("#2", bountyContext)
            );
            final Map<String, String> hostEntries = getDNSEntries();

            for (final Map.Entry<String, String> entryObject : hostEntries.entrySet()) {
                scriptContents.append(scriptCommand.replaceAll("#1", certificateDirectory)
                        .replaceAll("#2", bountyContext)
                        .replaceAll("#3", entryObject.getKey()));
            }

            File setupScript = new File(baseDir, SETUP_SCRIPT);
            FileOutputStream fos = new FileOutputStream(setupScript);

            IOUtils.write(scriptContents.toString().getBytes(StandardCharsets.UTF_8), fos);
            fos.close();
            setupScript.setExecutable(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write setup script, e");
        }
    }
}
