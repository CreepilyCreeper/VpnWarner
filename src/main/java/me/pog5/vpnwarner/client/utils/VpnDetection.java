package me.pog5.vpnwarner.client.utils;

import me.pog5.vpnwarner.client.VpnwarnerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for VPN detection.
 */
public final class VpnDetection {
    private static final Set<String> VPN_NAMES = new HashSet<>(Arrays.asList(
            "openvpn", "nordvpn", "expressvpn", "tunnelbear", "windscribe", "protonvpn", "cyberghost", "surfshark",
            "vyprvpn", "ipvanish", "hidemyass", "privateinternetaccess", "purevpn", "strongvpn", "hotspotshield",
            "privatevpn", "torguard", "avastsecureline", "avgsecurevpn", "avirasecureline", "bitdefenderpremiumvpn",
            "bullguardvpn", "ciscovpn", "fsecurefreedom", "kasperskysecureconnection", "mcafeesafeconnect",
            "nortonsecurevpn", "pandasecurity", "totalavpn", "webrootwifi", "zenmate", "hideallip", "exitlag",
            "wireguard", "cloudflarewarp", "mullvad", "ivpn", "mozillavpn", "azirevpn", "airvpn", "privadovpn",
            "speedify", "atlasvpn", "surfeasy", "wevpn", "malwarebytesprivacyvpn", "ghostpath", "vpnunlimited",
            "vpnbook", "safenetvpn", "xvpn", "urbanvpn", "supervpn", "rocketvpn", "internetprivacynow",
            "cryptostorm", "perfectprivacy", "shellfire", "hola", "betternet", "safetynet", "vpnsecure", "upvpn",
            "vpnht", "unlocator", "ibvpn", "strongswan", "onetunnel", "fastestvpn", "tapvpn", "vpnmonster",
            "freevpn", "hideman", "vpnify", "vpn360", "vpnhub", "browsec", "dashvpn"
    ));
    private static final Pattern VPN_REGEX_PATTERN = Pattern.compile(String.join("|", VPN_NAMES), Pattern.CASE_INSENSITIVE);
    private static final Set<String> WHITELISTED_VPN_NAMES = new HashSet<>(Arrays.asList(
            "exitlagpmservice"
    ));

    private VpnDetection() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static boolean isVpnEnabled() {
        return checkRunningProcessesOrServices() || isOpenVPNConnected();
    }

    private static boolean checkRunningProcessesOrServices() {
        try {
            String osName = System.getProperty("os.name").toLowerCase(Locale.US);
            List<List<String>> commands = new ArrayList<>();
    
            if (osName.contains("win")) {
                commands.add(Arrays.asList("tasklist")); // List running processes
                commands.add(Arrays.asList("sc", "query")); // List services
            } else {
                commands.add(Arrays.asList("ps", "-e")); // List running processes
                commands.add(Arrays.asList("systemctl", "list-units", "--type=service", "--all")); // List services
            }
    
            for (List<String> command : commands) {
                if (isVpnProcessRunning(command)) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isVpnProcessRunning(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        boolean found = false;
        StringBuilder foundNames = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = VPN_REGEX_PATTERN.matcher(line);
                while (matcher.find()) {
                    String detectedVpn = matcher.group();
                    String lowerDetected = detectedVpn.toLowerCase(Locale.ROOT);
                    if (WHITELISTED_VPN_NAMES.contains(lowerDetected)) {
                        continue;
                    }
                    if (foundNames.length() > 0) foundNames.append(", ");
                    foundNames.append(detectedVpn).append(" (Process)");
                    found = true;
                }
            }
        } finally {
            if (!finished) {
                process.destroyForcibly();
            }
        }
        if (found) {
            if (!VpnwarnerClient.detectedVpn.isEmpty() && !VpnwarnerClient.detectedVpn.endsWith(", ")) {
                VpnwarnerClient.detectedVpn = VpnwarnerClient.detectedVpn + ", ";
            }
            VpnwarnerClient.detectedVpn = VpnwarnerClient.detectedVpn.concat(foundNames.toString()).concat(", ");
        }
        return found;
    }

    private static boolean isOpenVPNConnected() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            boolean found = false;
    
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = networkInterfaces.nextElement();
                String name = netInterface.getName();
                String displayName = netInterface.getDisplayName();
                if (!netInterface.isUp())
                    continue;
    
    
                // Check for typical VPN interface names
                if (name != null && (name.toLowerCase(Locale.US).contains("tun") || name.toLowerCase(Locale.US).contains("tap") || name.toLowerCase(Locale.US).contains("vpn"))) {
                    VpnwarnerClient.detectedVpn = VpnwarnerClient.detectedVpn.concat(name + " (OpenVPN), ");
                    found = true;
                }
    
                if (displayName != null && (displayName.toLowerCase(Locale.US).contains("tun") || displayName.toLowerCase(Locale.US).contains("tap") || displayName.toLowerCase(Locale.US).contains("vpn"))) {
                    VpnwarnerClient.detectedVpn = VpnwarnerClient.detectedVpn.concat(displayName + " (OpenVPN), ");
                    found = true;
                }
            }
    
            return found;
    
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }
}