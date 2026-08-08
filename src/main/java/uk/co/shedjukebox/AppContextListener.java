package uk.co.shedjukebox;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import uk.co.shedjukebox.db.Migrations;
import uk.co.shedjukebox.scan.LibraryScanner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Migrations.run();
            LOG.info("ShedJukeBox2 migrations complete");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Migration failed", e);
        }
        Thread boot = new Thread(LibraryScanner::scan, "juke-boot-scan");
        boot.setDaemon(true);
        boot.start();
        int interval = Config.SCAN_INTERVAL_SECONDS;
        if (interval > 0) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "juke-scan-scheduler");
                t.setDaemon(true);
                return t;
            });
            scheduler.scheduleAtFixedRate(LibraryScanner::scan, interval, interval, TimeUnit.SECONDS);
        }
        LOG.info("ShedJukeBox2 ready");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
    }
}
