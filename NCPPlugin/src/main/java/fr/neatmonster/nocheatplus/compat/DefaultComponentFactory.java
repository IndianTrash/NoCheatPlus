package fr.neatmonster.nocheatplus.compat;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.checks.inventory.Gutenberg;
import fr.neatmonster.nocheatplus.checks.net.protocollib.ProtocolLibComponent;
import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * Default factory for add-in components which might only be available under certain circumstances.
 * 
 * @author mc_dev
 */
public class DefaultComponentFactory {

    /**
     * This will be called from within the plugin in onEnable, after registration of all core listeners and components. After each components addition processQueuedSubComponentHolders() will be called to allow registries for further optional components.
     * @param plugin 
     * @return
     */
    public Collection<Object> getAvailableComponentsOnEnable(NoCheatPlus plugin){
        final List<Object> available = new LinkedList<Object>();

        // Add components (try-catch).
        // TODO: catch ClassNotFound, incompatibleXY rather !?

        // Check: inventory.fastconsume.
        try{
            // TODO: Static test methods !?
            FastConsume.testAvailability();
            available.add(new FastConsume());
            if (ConfigManager.isTrueForAnyConfig(ConfPaths.INVENTORY_FASTCONSUME_CHECK)) {
                NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(FastConsume.class.getSimpleName()));
            }
        }
        catch (Throwable t){
            StaticLog.logInfo("Inventory checks: FastConsume is not available.");
        }

        // Check: inventory.gutenberg.
        try {
            Gutenberg.testAvailability();
            available.add(new Gutenberg());
            if (ConfigManager.isTrueForAnyConfig(ConfPaths.INVENTORY_GUTENBERG_CHECK)) {
                NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(Gutenberg.class.getSimpleName()));
            }
        } catch (Throwable t) {
            StaticLog.logInfo("Inventory checks: Gutenberg is not available.");
        }

        // ProtocolLib dependencies.
        Plugin pluginProtocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        boolean protocolLibAvailable = false;
        if (pluginProtocolLib != null) {
            String _pV = pluginProtocolLib.getDescription().getVersion().toLowerCase();
            String pV = GenericVersion.collectVersion(_pV, 0);
            if (pV == null) {
                pV = GenericVersion.parseVersionDelimiters(_pV, "", "-snapshot");
            }
            if (pV == null) {
                
            }
            else {
                try {
                    boolean vP1 = GenericVersion.compareVersions("3.6.4", pV) == 0;
                    boolean vP2 = GenericVersion.compareVersions("3.6.5", pV) == 0;
                    boolean vP3 = GenericVersion.compareVersions("3.7", pV) == 0;
                    if (
                            ServerVersion.isMinecraftVersionBetween("1.9", true, "1.10", false)
                            && vP3
                            || ServerVersion.isMinecraftVersionBetween("1.8", true, "1.9", false) 
                            &&
                            (vP1 || vP2) 
                            || ServerVersion.isMinecraftVersionBetween("1.2.5", true, "1.9", false)
                            && vP1
                            ) {
                        available.add(new ProtocolLibComponent(plugin));
                        protocolLibAvailable = true;
                    }
                } catch (Throwable t){
                    StaticLog.logWarning("Failed to set up packet level hooks.");
                    if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS)) {
                        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.INIT, t);
                    }
                }
            }
        }
        if (!protocolLibAvailable) {
            if (pluginProtocolLib != null) {
                StaticLog.logWarning("NoCheatPlus supports ProtocolLib 3.6.4 on Minecraft 1.7.10 and earlier, ProtocolLib 3.6.4 or 3.6.5 on Minecraft 1.8, ProtocolLib 3.7 on Minecraft 1.9 [EXPERIMENTAL].");
            }
            StaticLog.logInfo("Packet level access: ProtocolLib is not available.");
        }

        return available;
    }
}
