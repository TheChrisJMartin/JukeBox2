package uk.co.shedjukebox.media;

import uk.co.shedjukebox.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Disk cache for album art under JUKE_ART_CACHE_DIR, keyed by content hash.
 */
public final class ArtCache {

    private static final Logger LOG = Logger.getLogger(ArtCache.class.getName());

    public static String store(byte[] data, String mimeHint) throws IOException {
        if (data == null || data.length == 0) return null;
        String hash = sha256(data);
        String ext = (mimeHint != null && mimeHint.contains("png")) ? "png" : "jpg";
        Path dir = Config.ART_CACHE_DIR;
        Files.createDirectories(dir);
        Path target = dir.resolve(hash + "." + ext);
        if (!Files.exists(target)) {
            Files.write(target, data);
        }
        return hash + "." + ext;
    }

    public static Path resolve(String artKey) {
        if (artKey == null || artKey.isBlank()) return null;
        if (artKey.contains("..") || artKey.contains("/") || artKey.contains("\\")) return null;
        Path p = Config.ART_CACHE_DIR.resolve(artKey).normalize();
        if (!p.startsWith(Config.ART_CACHE_DIR.toAbsolutePath().normalize())
                && !p.startsWith(Config.ART_CACHE_DIR.normalize())) {
            if (!p.getFileName().toString().equals(artKey)) return null;
        }
        return Files.isRegularFile(p) ? p : null;
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data)).substring(0, 32);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "hash failed", e);
            return Integer.toHexString(data.hashCode());
        }
    }

    private ArtCache() {}
}
