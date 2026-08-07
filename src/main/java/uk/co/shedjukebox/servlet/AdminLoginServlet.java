package uk.co.shedjukebox.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import uk.co.shedjukebox.auth.Pbkdf2;
import uk.co.shedjukebox.db.Db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminLoginServlet.class.getName());

    /** Known-good PBKDF2 hash for password "admin" (matches Migrations seed). */
    private static final String ADMIN_DEFAULT_HASH =
            "100000$0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20$c423fa655ffd3d321c959acd23d2403c00a023f0e590027a04a0f3c7fc3dad3c";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");
        if (user == null || pass == null || user.isBlank() || pass.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/admin/login.html?error=1");
            return;
        }
        user = user.trim();

        final String username = user;
        final String password = pass;

        Long adminId = Db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, password_hash FROM juke_admin WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long id = rs.getLong("id");
                        String hash = rs.getString("password_hash");
                        if (hash == null) hash = "";

                        // Normal path
                        if (Pbkdf2.verify(password, hash)) {
                            return id;
                        }

                        // Bootstrap / repair: old placeholder seed or empty hash + admin/admin
                        boolean oldPlaceholder = hash.startsWith("100000$a1b2c3d4") || hash.isBlank();
                        if (oldPlaceholder && "admin".equals(username) && "admin".equals(password)) {
                            try (PreparedStatement upd = c.prepareStatement(
                                    "UPDATE juke_admin SET password_hash = ? WHERE id = ?")) {
                                upd.setString(1, ADMIN_DEFAULT_HASH);
                                upd.setLong(2, id);
                                upd.executeUpdate();
                            }
                            LOG.info("Repaired admin password hash for id=" + id);
                            return id;
                        }
                    } else if ("admin".equals(username) && "admin".equals(password)) {
                        // No admin row at all — create one (migration may have failed earlier)
                        try (PreparedStatement ins = c.prepareStatement(
                                """
                                INSERT INTO juke_admin (username, password_hash, salt, venue_token)
                                VALUES ('admin', ?, '0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20',
                                        md5(random()::text || clock_timestamp()::text))
                                RETURNING id
                                """)) {
                            ins.setString(1, ADMIN_DEFAULT_HASH);
                            try (ResultSet irs = ins.executeQuery()) {
                                if (irs.next()) {
                                    LOG.info("Created missing admin account");
                                    return irs.getLong(1);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        });

        if (adminId == null) {
            LOG.warning("Admin login failed for user=" + username);
            resp.sendRedirect(req.getContextPath() + "/admin/login.html?error=1");
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("adminId", adminId);
        session.setAttribute("adminUser", username);
        resp.sendRedirect(req.getContextPath() + "/admin/");
    }
}
