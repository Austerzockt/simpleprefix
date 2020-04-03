import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Logger;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.INametag;
import com.nametagedit.plugin.api.events.NametagFirstLoadedEvent;
import com.nametagedit.plugin.hooks.HookLuckPerms;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.QueryOptions;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
public class Main extends JavaPlugin implements Listener {
    public static Permission perms = null;
    public static Chat chat = null;
    public static LuckPerms luckPerms;
    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();
        perms = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage("Plugin successfully started");
    }
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("Plugin successfully stopped");
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e)  {
        String group = perms.getPrimaryGroup(e.getPlayer());
        NametagEdit.getApi().setPrefix(e.getPlayer(), chat.getGroupPrefix(e.getPlayer().getWorld(), group));
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        boolean set= false;
        for (String group:
                perms.getPlayerGroups(e.getPlayer())) {
            for (Node x : luckPerms.getGroupManager().getGroup(group).getNodes()) {
                boolean boolx = x.getType() == NodeType.META;
                if (boolx) {
                    MetaNode node = (MetaNode) x;
                    String nodekey = node.getMetaKey();
                    if (nodekey.startsWith("mc")) {
                        String chatcolor = node.getMetaValue();
                        e.setFormat(ChatColor.translateAlternateColorCodes('&', NametagEdit.getApi().getFakeTeam(e.getPlayer()).getPrefix() + e.getPlayer().getName() + "&f|" + chatcolor + e.getMessage()));
                        set = true;
                    }
                }
            }
            if (!set)
            e.setFormat(ChatColor.translateAlternateColorCodes('&', NametagEdit.getApi().getFakeTeam(e.getPlayer()).getPrefix() + e.getPlayer().getName() + "&f|" + e.getMessage()));

        }
    }
    @EventHandler
    public void onFirstNameTagEdit(NametagFirstLoadedEvent e ) {
        OptionalInt id = luckPerms.getGroupManager().getGroup(perms.getPlayerGroups(e.getPlayer())[0]).getWeight();
        if (id.isPresent()) {
            FakeTeam  team = new FakeTeam(e.getNametag().getPrefix(), e.getNametag().getSuffix(), id.getAsInt() , true);
            team.addMember(e.getPlayer().getName());
        }
    }
}