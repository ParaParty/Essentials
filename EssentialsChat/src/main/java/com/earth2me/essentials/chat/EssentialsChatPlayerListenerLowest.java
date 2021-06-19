package com.earth2me.essentials.chat;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import net.ess3.api.IEssentials;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class EssentialsChatPlayerListenerLowest extends EssentialsChatPlayer {
    EssentialsChatPlayerListenerLowest(final Server server, final IEssentials ess, final Map<AsyncPlayerChatEvent, ChatStore> chatStorage) {
        super(server, ess, chatStorage);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @Override
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (isAborted(event)) {
            return;
        }

        final User user = ess.getUser(event.getPlayer());

        if (user == null) {
            event.setCancelled(true);
            return;
        }

        final ChatStore chatStore = new ChatStore(ess, user, getChatType(user, event.getMessage()));
        setChatStore(event, chatStore);

        // This listener should apply the general chat formatting only...then return control back the event handler
        event.setMessage(FormatUtil.formatMessage(user, "essentials.chat", event.getMessage()));

        if (ChatColor.stripColor(event.getMessage()).length() == 0) {
            event.setCancelled(true);
            return;
        }

        final String group = user.getGroup();
        final String world = user.getWorld().getName();
        final String username = user.getName();
        final String nickname = user.getFormattedNickname();

        final Player player = user.getBase();
        final String prefix = FormatUtil.replaceFormat(ess.getPermissionsHandler().getPrefix(player));
        final String suffix = FormatUtil.replaceFormat(ess.getPermissionsHandler().getSuffix(player));
        final Team team = player.getScoreboard().getPlayerTeam(player);

        String format = ess.getSettings().getChatFormat(group);
        format = format.replace("%1$s", alignColon(ess, user));
        format = format.replace("{0}", group);
        format = format.replace("{1}", ess.getSettings().getWorldAlias(world));
        format = format.replace("{2}", world.substring(0, 1).toUpperCase(Locale.ENGLISH));
        format = format.replace("{3}", team == null ? "" : team.getPrefix());
        format = format.replace("{4}", team == null ? "" : team.getSuffix());
        format = format.replace("{5}", team == null ? "" : team.getDisplayName());
        format = format.replace("{6}", prefix);
        format = format.replace("{7}", suffix);
        format = format.replace("{8}", username);
        format = format.replace("{9}", nickname == null ? username : nickname);
        synchronized (format) {
            event.setFormat(format);
        }
    }

    private CharSequence alignColon(IEssentials ess, User user) {
        final String str = user.getDisplayName();
        final int displayLength = getDisplayLength(str);
        final StringBuilder displayFormat = new StringBuilder();
        final int padding = 16 - displayLength;

        for (int i = 0; i < padding; i++) {
            displayFormat.append(' ');
        }
        displayFormat.append("%1$s");

        return displayFormat.toString();
    }

    static final HashSet<Character> formatAndColorCode = new HashSet<Character>() {
        {
            add('0');
            add('1');
            add('2');
            add('3');
            add('4');
            add('5');
            add('6');
            add('7');
            add('8');
            add('9');
            add('a');
            add('b');
            add('c');
            add('d');
            add('e');
            add('f');
            add('l');
            add('m');
            add('n');
            add('o');
            add('r');
        }
    };

    private int getDisplayLength(String msg) {
        int ret = 0;

        int i = 0;
        while (i < msg.length()) {
            if (msg.charAt(i) == '§') {
                if (i + 1 < msg.length()) {
                    if (formatAndColorCode.contains(msg.charAt(i + 1))) {
                        i++;
                    } else {
                        ret++;
                    }
                } else {
                    ret++;
                }
            } else {
                ret++;
            }
            i++;
        }

        return ret;
    }
}
