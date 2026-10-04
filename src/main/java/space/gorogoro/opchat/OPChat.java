package space.gorogoro.opchat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * OPChat
 * @license    GPL-3.0
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">Gorogoro Server.</a>
 */
public class OPChat extends JavaPlugin implements Listener {

  // /opchatoff で OP チャットを OFF にしている人
  private final Set<UUID> offPlayers = new HashSet<>();
  // OFF の間に、ログイン時の案内を表示済みの人(ON に戻すと消す)
  private final Set<UUID> notifiedPlayers = new HashSet<>();

  // config.yml の書き込み専用スレッド
  private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "OPChat-Save"));
  // 書き込み待ちの config.yml の内容(null なら書き込み待ちなし)
  private final AtomicReference<String> pendingConfigYaml = new AtomicReference<>();

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable() {
    try {
      loadUuidSet("off-players", offPlayers);
      loadUuidSet("notified-players", notifiedPlayers);
      getServer().getPluginManager().registerEvents(this, this);
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
      if(!sender.isOp()) {
        return true;
      }

      if(command.getName().equals("opchatoff")) {
        toggleOpChat(sender);
        return true;
      }

      if(args.length <= 0){
        return true;
      }

      if(command.getName().equals("o")) {
        // OFF の間は送信できない
        if(sender instanceof Player && offPlayers.contains(((Player) sender).getUniqueId())) {
          sender.sendMessage(ChatColor.RED + "OPチャットがOFFのため送信できません。/opchatoff で ON に戻せます。");
          return true;
        }

        for(Player p: getServer().getOnlinePlayers()) {
          if(!p.isOp()) {
            continue;
          }

          if(offPlayers.contains(p.getUniqueId())) {
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
      } else if(command.getName().equals("url")) {
        if(sender.isOp()) {
          for(Player p: getServer().getOnlinePlayers()) {
            p.sendMessage(
              sender.getName()
              + ChatColor.GREEN
              + ": "
              + ChatColor.RESET
              +  String.join(" ", args)
            );
          }
        }
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
      // 書き込み待ちがすべて終わるまで待つ
      saveExecutor.shutdown();
      try {
        if(!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
          getLogger().severe("config.yml の書き込みが時間内に終わりませんでした。");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * OFF の OP が OFF にしてから初めてログインしたときに、1 回だけ案内する
   */
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    try {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      // OP でない間は案内しない(OFF の設定は残すので、OP に戻ったときに 1 回案内する)
      if(!player.isOp() || !offPlayers.contains(uuid) || notifiedPlayers.contains(uuid)) {
        return;
      }
      player.sendMessage(ChatColor.YELLOW + "OPチャットがOFFです。" + ChatColor.GRAY + "(/opchatoff で ON に戻せます)");
      notifiedPlayers.add(uuid);
      saveUuidSet("notified-players", notifiedPlayers);
      requestSaveConfig();
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * /opchatoff で OP チャットの ON / OFF を切り替える
   */
  private void toggleOpChat(CommandSender sender) {
    if(!(sender instanceof Player)) {
      sender.sendMessage("このコマンドはプレイヤーのみ実行できます。");
      return;
    }
    UUID uuid = ((Player) sender).getUniqueId();
    if(offPlayers.remove(uuid)) {
      notifiedPlayers.remove(uuid);
      sender.sendMessage(ChatColor.AQUA + "OPチャットを ON にしました。");
    } else {
      offPlayers.add(uuid);
      sender.sendMessage(ChatColor.AQUA + "OPチャットを OFF にしました。" + ChatColor.GRAY + "(もう一度 /opchatoff で ON に戻せます)");
    }
    saveUuidSet("off-players", offPlayers);
    saveUuidSet("notified-players", notifiedPlayers);
    requestSaveConfig();
  }

  /**
   * config.yml から UUID のリストを読み込む
   */
  private void loadUuidSet(String path, Set<UUID> set) {
    set.clear();
    for(String s: getConfig().getStringList(path)) {
      try {
        set.add(UUID.fromString(s));
      } catch (IllegalArgumentException ignored) {
      }
    }
  }

  /**
   * UUID のリストを config.yml の内容(メモリ上)へ反映する
   */
  private void saveUuidSet(String path, Set<UUID> set) {
    List<String> list = set.stream().map(UUID::toString).sorted().collect(Collectors.toList());
    getConfig().set(path, list);
  }

  /**
   * 現在の config.yml の内容を専用スレッドで書き込む
   * getConfig() へのアクセスはメインスレッドで行い、ファイルの書き込みだけを専用スレッドに任せる
   * 書き込み待ちが残っている間に呼ばれた場合は、最新の内容で 1 回にまとめて書き込む
   */
  private void requestSaveConfig() {
    String yaml = getConfig().saveToString();
    if(pendingConfigYaml.getAndSet(yaml) != null) {
      return;
    }
    File dataFolder = getDataFolder();
    saveExecutor.execute(() -> {
      String latestYaml = pendingConfigYaml.getAndSet(null);
      try {
        dataFolder.mkdirs();
        Files.writeString(new File(dataFolder, "config.yml").toPath(), latestYaml, StandardCharsets.UTF_8);
      } catch (IOException e) {
        getLogger().severe("config.yml の書き込みに失敗しました: " + e.getMessage());
      }
    });
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
