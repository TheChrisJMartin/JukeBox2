package uk.co.shedjukebox.media;

import uk.co.shedjukebox.Config;
import uk.co.shedjukebox.db.Db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Resolve a track id to a safe Path under JUKE_MUSIC_ROOT. */
public final class TrackFiles {

    public record TrackFile(long id, String relPath, Path absolute) {}

    public static TrackFile resolve(long trackId) {
        String rel = Db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT path FROM juke_track WHERE id = ? AND missing = FALSE")) {
                ps.setLong(1, trackId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("path");
                    return null;
                }
            }
        });
        if (rel == null) return null;
        Path root = Config.MUSIC_ROOT.toAbsolutePath().normalize();
        Path file = root.resolve(rel).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) return null;
        return new TrackFile(trackId, rel, file);
    }

    private TrackFiles() {}
}
