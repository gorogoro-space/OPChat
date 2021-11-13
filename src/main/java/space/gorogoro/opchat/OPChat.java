package space.gorogoro.opchat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * OPChat
 * @license    LGPv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">Gorogoro Server.</a>
 */
public class OPChat extends JavaPlugin {

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable() {
    try {
      getLogger().info("The Plugin Has Been Enabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * JavaPlugin method onCommand.
   */
  public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
    // Return true:Success false:Show the usage set in plugin.yml
    try{
      if(!command.getName().equals("o")) {
        return true;
      }

      if(!sender.isOp()) {
        return true;
      }

      if(args.length <= 0){
        return true;
      }

      for(Player p: getServer().getOnlinePlayers()) {
        if(!p.isOp()) {
          continue;
        }

        p.sendMessage(
          ChatColor.AQUA
          + "[OP] "
          + ChatColor.RESET
          + sender.getName()
          + ChatColor.GREEN
          + ": "
          + ChatColor.RESET
          +  String.join(" ", args)
        );
      }
    }catch(Exception e){
      logStackTrace(e);
    }
    return true;
  }

  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable() {
    try {
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  private void logStackTrace(Exception e){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      getLogger().warning(sw.toString());
  }
}
