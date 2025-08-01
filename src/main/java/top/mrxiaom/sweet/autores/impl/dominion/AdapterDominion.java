package top.mrxiaom.sweet.autores.impl.dominion;

import cn.lunadeer.dominion.Dominion;
import cn.lunadeer.dominion.DominionInterface;
import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.dominion.configuration.Configuration;
import cn.lunadeer.dominion.configuration.Limitation;
import cn.lunadeer.dominion.events.dominion.DominionCreateEvent;
import cn.lunadeer.dominion.utils.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.autores.SweetAutoResidence;
import top.mrxiaom.sweet.autores.api.IResidenceAdapter;
import top.mrxiaom.sweet.autores.api.Selection;
import top.mrxiaom.sweet.autores.func.AbstractPluginHolder;

import static cn.lunadeer.dominion.misc.Others.autoPoints;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class AdapterDominion extends AbstractPluginHolder implements IResidenceAdapter {
    DominionAPI dominionAPI;
    Dominion dominion;
    public AdapterDominion(SweetAutoResidence plugin) {
        super(plugin);
        dominion = Dominion.instance;
        dominionAPI = DominionInterface.getInstance();
    }

    @Override
    public @NotNull String getName() {
        return "Dominion " + dominion.getDescription().getVersion();
    }

    @Override
    public @Nullable Selection genAutoSelection(Player player, int xSize, int ySize, int zSize) {
        World world = player.getWorld();
        Location[] points = autoPoints(player);
        CuboidDTO cuboidDTO = new CuboidDTO(points[0], points[1]);
        // TODO: 与其它领地出现区域冲突时返回 null
        return new Selection(cuboidDTO, cuboidDTO.x1(), cuboidDTO.y1(), cuboidDTO.z1(), cuboidDTO.x2(), cuboidDTO.y2(), cuboidDTO.z2());
    }

    @Override
    public boolean isResidenceExists(String resName) {
        try {
            dominionAPI.getDominion(resName);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void createResidence(Player player, String resName, Selection area) {
        CuboidDTO cuboidDTO = (CuboidDTO) area.tag;
        DominionCreateEvent event = new DominionCreateEvent(
                player,
                resName,
                player.getUniqueId(),
                player.getWorld(), cuboidDTO,
                null
        );
        event.setSkipEconomy(true);
        event.call();
    }

    @Override
    public int getResidenceCount(Player player) {
        // TODO: 或许应该获取玩家在当前世界的领地数量才对？或者或者应该修改接口适应 Dominion 多世界分别限制的特性
        return dominionAPI.getPlayerOwnDominionDTOs(player.getUniqueId()).size();
    }

    @Override
    public int getResidenceMaxCount(Player player) {
        Limitation limitation = Configuration.getPlayerLimitation(player);
        Limitation.WorldLimitationSetting settings = limitation.getWorldSettings(player.getWorld());
        return settings.amount;
    }

    @Override
    public void showSelection(Player player, Selection area) {
        CuboidDTO cuboidDTO = (CuboidDTO) area.tag;
        ParticleUtil.showBorder(player, player.getWorld(), cuboidDTO);
    }
}
