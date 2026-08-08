package uk.co.shedjukebox;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * All runtime configuration via environment variables (setenv.sh / Tomcat).
 */
public final class Config {

    public static final String DB_URL = env("JUKE_DB_URL", "jdbc:postgresql://localhost:5432/jukebox");
    public static final String DB_USER = env("JUKE_DB_USER", "jukebox");
    public static final String DB_PASS = env("JUKE_DB_PASS", "jukebox");
    public static final Path MUSIC_ROOT = Paths.get(env("JUKE_MUSIC_ROOT", "/mnt/music"));
    public static final int SCAN_INTERVAL_SECONDS = Integer.parseInt(env("JUKE_SCAN_INTERVAL", "3600"));
    public static final int BACKLOG_LIMIT = Integer.parseInt(env("JUKE_BACKLOG_LIMIT", "5"));
    /** Higher queue limit for named hosts (comma-separated, case-insensitive). */
    public static final int VIP_BACKLOG_LIMIT = Integer.parseInt(env("JUKE_VIP_BACKLOG_LIMIT", "50"));
    public static final String VIP_NAMES = env("JUKE_VIP_NAMES", "Katie,Chris");
    public static final int COOLDOWN_MINUTES = Integer.parseInt(env("JUKE_COOLDOWN_MINUTES", "30"));
    /** Crossfade duration on playout (seconds). */
    public static final int CROSSFADE_SECONDS = Integer.parseInt(env("JUKE_CROSSFADE_SECONDS", "5"));
    public static final Path ART_CACHE_DIR = Paths.get(env("JUKE_ART_CACHE_DIR", "/var/cache/shedjukebox/art"));
    public static final String BASE_URL = env("JUKE_BASE_URL", "http://localhost:8080/shedjukebox2");
    public static final String SMB_URL = env("JUKE_SMB_URL", "");
    public static final String SMB_USER = env("JUKE_SMB_USER", "");
    public static final String SMB_PASS = env("JUKE_SMB_PASS", "");

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    public static boolean isVipGuest(String name) {
        if (name == null || name.isBlank()) return false;
        String n = name.trim();
        for (String part : VIP_NAMES.split(",")) {
            if (part.trim().equalsIgnoreCase(n)) return true;
        }
        return false;
    }

    public static int backlogLimitFor(String guestName) {
        return isVipGuest(guestName) ? VIP_BACKLOG_LIMIT : BACKLOG_LIMIT;
    }

    private Config() {}
}
