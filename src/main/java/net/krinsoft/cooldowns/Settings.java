package net.krinsoft.cooldowns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import net.krinsoft.cooldowns.util.CoolLogger;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author krinsdeath
 */

final class Settings {
    private Cooldowns plugin;
    private CoolLogger log;

    private final File dataFolder;

    public Settings(Cooldowns instance) {
        plugin = instance;
        dataFolder = plugin.getDataFolder();
        log = plugin.getLogger();
        load();
    }

    public void load() {
        Configuration conf = null;
        File tmp = makeDefault(new File(dataFolder, "config.yml"));
        if (tmp.exists()) {
            conf = new Configuration(tmp);
            conf.load();
            plugin.config = conf;
        }
        for (String loc : plugin.config.getStringList("plugin.available_locales", Arrays.asList("en_US"))) {
            tmp = makeDefault(new File(dataFolder + File.separator + "language", loc + ".yml"));
            if (tmp.exists()) {
                plugin.addLocale(loc, tmp);
            }
        }
        tmp = new File(dataFolder, "users.yml");
        conf = new Configuration(tmp);
        conf.load();
        plugin.users = conf;
    }

    public File makeDefault(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            InputStream in = Cooldowns.class.getResourceAsStream("/defaults/" + file.getName());
            FileOutputStream out = null;
            if (in != null) {
                try {
                    out = new FileOutputStream(file);
                    byte[] buffer = new byte[128];
                    int len = 0;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    log.info("Successfully created " + file.getName());
                } catch (IOException e) {
                    log.warn("Error: " + e);
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        log.warn("Error: " + e);
                    }
                }
            }
        }
        return file;
    }
}
