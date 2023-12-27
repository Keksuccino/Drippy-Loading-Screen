package de.keksuccino.drippyloadingscreen.platform;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.drippyloadingscreen.platform.services.IPlatformHelper;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "forge";
    }

    @Override
    public String getPlatformDisplayName() {
        return "Forge";
    }

    @Override
    public String getLoaderVersion() {
        return this.getModVersion("forge");
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        try {
            Optional<? extends ModContainer> o = ModList.get().getModContainerById(modId);
            if (o.isPresent()) {
                ModContainer c = o.get();
                return c.getModInfo().getVersion().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }

    @Override
    public List<String> getLoadedModIds() {
        List<String> l = new ArrayList<>();
        for (IModInfo info : ModList.get().getMods()) {
            l.add(info.getModId());
        }
        return l;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isOnClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public InputConstants.Key getKeyMappingKey(KeyMapping keyMapping) {
        return keyMapping.getKey();
    }

}