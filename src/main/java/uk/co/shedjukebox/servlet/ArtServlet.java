package uk.co.shedjukebox.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.co.shedjukebox.media.ArtCache;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/art/*")
public class ArtServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.sendError(404);
            return;
        }
        String key = pathInfo.substring(1);
        Path file = ArtCache.resolve(key);
        if (file == null) {
            resp.sendError(404);
            return;
        }
        String name = file.getFileName().toString().toLowerCase();
        resp.setContentType(name.endsWith(".png") ? "image/png" : "image/jpeg");
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setContentLengthLong(Files.size(file));
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file, out);
        }
    }
}
