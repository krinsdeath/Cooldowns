package net.krinsoft.cooldowns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author krinsdeath
 */

final class Settings {
    private Cooldowns plugin;

    private final File dataFolder;

    public Settings(Cooldowns aThis) {
        plugin = aThis;
        dataFolder = plugin.getDataFolder();
        load();
    }

    public void load() {
        Configuration conf = null;
        File tmp = makeDefault(new File(dataFolder, "config.yml"));
        if (tmp.exists()) {
            conf = new Configuration(tmp);
            conf.load();
            Cooldowns.config = conf;
        }
        for (String loc : Cooldowns.config.getStringList("plugin.available_locales", Arrays.asList("en_US"))) {
            tmp = makeDefault(new File(dataFolder + File.separator + "language", loc + ".yml"));
            if (tmp.exists()) {
                Cooldowns.addLocale(loc, tmp);
            }
        }
        tmp = new File(dataFolder, "users.yml");
        conf = new Configuration(tmp);
        conf.load();
        Cooldowns.users = conf;
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
                    Cooldowns.getLogger().info("Successfully created " + file.getName());
                } catch (IOException e) {
                    Cooldowns.getLogger().warn("Error: " + e);
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        Cooldowns.getLogger().warn("Error: " + e);
                    }
                }
            }
        }
        return file;
    }
}
