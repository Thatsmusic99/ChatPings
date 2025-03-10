package de.niestrat.chatpings.listeners;

import de.niestrat.chatpings.commands.Toggle;
import de.niestrat.chatpings.config.Config;
import de.niestrat.chatpings.hooks.CooldownManager;
import de.niestrat.chatpings.hooks.HookManager;
import de.niestrat.chatpings.hooks.PlaceholderAPIManager;
import de.niestrat.chatpings.hooks.PopUpManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingListener implements Listener {



    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    // Functions used to be separated in three different classes, now been compiled into one.
    public void ping(AsyncPlayerChatEvent e) {

        Player sender = e.getPlayer();

        // For Everyone and Someone pings
        String formatMessage = e.getFormat();
        String regularMessage = e.getMessage();


        // For Player Pings
        String playerRegularMessage = createPing(regularMessage, sender, formatMessage);

        // Ping Types
        String everyone = Config.getString("ping.Prefix") + Config.config.getString("ping.everyoneFormat");
        String someone = Config.getString("ping.Prefix") + Config.config.getString("ping.someoneFormat");

        // Player Ping
        if (playerRegularMessage != null) {
            e.setMessage(playerRegularMessage);
        } else {
            e.setCancelled(true);
            return;
        }
        

        // EVERYONE PING
        // This line of code is to stop the API from shitting itself about NullPointerExceptions

        if (formatMessage.contains(everyone) ||regularMessage.contains(everyone)) {
            if (!sender.hasPermission("chatpings.admin")) { return; }

            // formatMessage = formatMessage.replace(everyone, Config.getColor("everyonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.everyoneFormat") + messageColorCode(formatMessage, formatMessage, formatMessage.indexOf(Config.getString("ping.Prefix"))));
            regularMessage = regularMessage.replace(everyone, Config.getColor("everyonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.everyoneFormat") + messageColorCode(regularMessage, formatMessage, regularMessage.indexOf(Config.getString("ping.Prefix"))));

            if (CooldownManager.checkForCooldown(sender)) {
                e.setCancelled(true);
                return;
            }
            e.setMessage(regularMessage);
            // e.setFormat(formatMessage);

            for (Player everyPlayer : Bukkit.getOnlinePlayers()) {
                everyPlayer.playSound(everyPlayer.getLocation(), Sound.valueOf(Config.getString("everyonePing.sound")), Config.getFloat("everyonePing.volume"), Config.getFloat("everyonePing.pitch"));
                PopUpManager.popUp(everyPlayer, sender);
            }
            CooldownManager.addPlayerToCooldown(sender.getUniqueId(), Config.config.getInt("pingcooldown.duration.everyone"));
        }

        // SOMEONE PING
        if (formatMessage.contains(someone) || regularMessage.contains(someone)) {
            if (!sender.hasPermission("chatpings.someone")) { return; }

            // Get the amount of players and get a random player using the randomizer
            int playerCount = Bukkit.getOnlinePlayers().size();
            Random rand = new Random();
            Player target = new ArrayList<>(Bukkit.getOnlinePlayers()).get(rand.nextInt(playerCount));

            if (playerCount == 1 && !Config.config.getBoolean("someonePing.pingYourself")) { return; }

            while (target.getName().equals(sender.getName())) {
                if (!Config.config.getBoolean("someonePing.pingYourself")) {
                    target = new ArrayList<>(Bukkit.getOnlinePlayers()).get(rand.nextInt(playerCount));
                } else {
                    break;
                }
            }

            if (Config.config.getBoolean("someonePing.includePicked")) {
                if (CooldownManager.checkForCooldown(sender)) {
                    e.setCancelled(true);
                    return;
                }
                // formatMessage = formatMessage.replace(someone, Config.getColor("someonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.someoneFormat") + "(" + target.getName() + ")" + messageColorCode(formatMessage, formatMessage, regularMessage.indexOf(Config.getString("ping.Prefix"))));
                regularMessage = regularMessage.replace(someone, Config.getColor("someonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.someoneFormat") + "(" + target.getName() + ")" + messageColorCode(regularMessage, formatMessage, regularMessage.indexOf(Config.getString("ping.Prefix"))));
                PopUpManager.popUp(target, sender);

            } else {
                if (CooldownManager.checkForCooldown(sender)) {
                    e.setCancelled(true);
                    return;
                }
                // formatMessage = formatMessage.replace(someone, Config.getColor("someonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.someoneFormat") + messageColorCode(formatMessage, formatMessage, regularMessage.indexOf(Config.getString("ping.Prefix"))));
                regularMessage = regularMessage.replace(someone, Config.getColor("someonePing") + Config.getString("ping.Prefix") + Config.config.getString("ping.someoneFormat") + messageColorCode(regularMessage, formatMessage, regularMessage.indexOf(Config.getString("ping.Prefix"))));
                PopUpManager.popUp(target, sender);
            }



            // e.setFormat(formatMessage);
            e.setMessage(regularMessage);

            target.playSound(target.getLocation(), Sound.valueOf(Config.getString("someonePing.sound")), Config.getFloat("someonePing.volume"), Config.getFloat("someonePing.pitch"));
            CooldownManager.addPlayerToCooldown(sender.getUniqueId(), Config.config.getInt("pingcooldown.duration.someone"));
        }
    }

    private Player getPlayer(String nickname) {
        String rawNickname = nickname.toLowerCase();
        Player player = HookManager.getNicknames().get(rawNickname);

        if (player != null) {
            return player;
        } else {
            player = PlaceholderAPIManager.getNicknames().get(rawNickname);
            // If PlaceholderAPI's Player is not null then it shall return the player, otherwise it returns Bukkit's player.
            return player != null ? player : Bukkit.getPlayer(rawNickname);

        }
    }

    // Private String for the player pings
    private String createPing(String message, Player sender, String format) {
        if (!sender.hasPermission("chatpings.player")) { return message; }

        String pattern = "(" + Pattern.quote(Config.getString("ping.Prefix")) + ")" + "([A-Za-z0-9_]+)";
        Pattern playerNamePattern = Pattern.compile(pattern);
        Matcher playerMatch = playerNamePattern.matcher(message);

        while (playerMatch.find()) {
            String playerName = playerMatch.group(2);
            Player player = getPlayer(playerName);
            String ping = playerMatch.group(0);
            if (player != null) {
                if (player.isOnline() && sender.canSee(player)) {
                    if (Toggle.mutePing.contains(player.getUniqueId())) {
                        if (sender.hasPermission("chatpings.bypass")) {
                            if (CooldownManager.checkForCooldown(sender)) {
                                return null;
                            }
                            message = message.replace(ping, Config.getColor("playerPing") + Config.getString("ping.Prefix") + playerName + messageColorCode(message, format, playerMatch.start()));
                            player.playSound(player.getLocation(), Sound.valueOf(Config.getString("playerPing.sound")), Config.getFloat("playerPing.volume"), Config.getFloat("playerPing.pitch"));
                            PopUpManager.popUp(player, sender);

                            CooldownManager.addPlayerToCooldown(sender.getUniqueId(), Config.config.getInt("pingcooldown.duration.player"));
                        }

                    } else {
                        if (CooldownManager.checkForCooldown(sender)) {
                            return null;
                        }
                        message = message.replace(ping, Config.getColor("playerPing")+ Config.getString("ping.Prefix") + playerName + messageColorCode(message, format, playerMatch.start()));
                        player.playSound(player.getLocation(), Sound.valueOf(Config.getString("playerPing.sound")), Config.getFloat("playerPing.volume"), Config.getFloat("playerPing.pitch"));
                        PopUpManager.popUp(player, sender);

                        CooldownManager.addPlayerToCooldown(sender.getUniqueId(), Config.config.getInt("pingcooldown.duration.player"));
                    }

                }
            }
        } return message;
    }

    private String messageColorCode(String msg, String format, int index) {
        final char SECTION = 167;
        if (msg == null) {return SECTION + "r";}

        // For message
        Pattern pattern = Pattern.compile("((" + SECTION + "[0-9a-fk-ox])+)");
        Matcher matcher = pattern.matcher(msg);

        // For format
        Matcher formatMatcher = pattern.matcher(format);

        int startingIndex = 0;
        String colorCode = SECTION + "r";


        while (matcher.find(startingIndex)) {
            if (matcher.start() >= index) { break; } // Break the loop when matcher is bigger or equal to index

            colorCode = matcher.group(1); // Returns first group of color code pattern
            startingIndex = matcher.start() + colorCode.length();
        }

        int endingIndex = format.indexOf("%s");
        if (endingIndex == -1) endingIndex = format.indexOf("%2$s");

        if (colorCode.equals(SECTION + "r")) {
            startingIndex = 0;
            while (formatMatcher.find(startingIndex)) {
                if (formatMatcher.start() >= endingIndex) {break;}

                startingIndex = formatMatcher.start() + 1;
                colorCode = formatMatcher.group(1);
            }
        }

        return SECTION + "r" + colorCode;
    }
}



