import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class StealthEffect extends PeriodicExpirableEffect
{
    SlowEffect slow;
    int slowamp;
    
    public StealthEffect(Skill skill, long duration, float dist, int am)
    {
        super(skill, "Invisible", 400, duration);
        types.add(EffectType.BENEFICIAL);
        types.add(EffectType.INVIS);
        this.distance = dist;
        this.slowamp=am;
    }
    
    @Override
    public void applyToHero(Hero hero)
    {
        super.applyToHero(hero);
        slow=new com.herocraftonline.heroes.characters.effects.common.SlowEffect(this.getSkill(), this.getDuration(), slowamp, true, null, null, hero);
        hero.addEffect(slow);
        hero.addPermission("nocheatplus.checks.moving.sneaking");
        hero.addPermission("nocheatplus.checks.moving.survivalfly.sneaking");
        Player player = hero.getPlayer();
        Player arr$[] = plugin.getServer().getOnlinePlayers();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Player onlinePlayer = arr$[i$];
            if(!onlinePlayer.equals(player) && !onlinePlayer.hasPermission("heroes.admin.seeinvis")) {
                onlinePlayer.hidePlayer(player);
            }
        }

        hiddenPlayers.add(player.getName());
        //Messaging.send(player, applyText, new Object[0]);
        player.sendMessage("§7You blend into the shadows!");
        hero.getPlayer().setSneaking(true);
    }

    @Override
    public void removeFromHero(Hero hero)
    {
        super.removeFromHero(hero);
        hero.removeEffect(slow);
        hero.removePermission("nocheatplus.checks.moving.sneaking");
        hero.removePermission("nocheatplus.checks.moving.survivalfly.sneaking");
        Player player = hero.getPlayer();
        Player arr$[] = plugin.getServer().getOnlinePlayers();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Player onlinePlayer = arr$[i$];
            if(!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(player);
            }
        }

        hiddenPlayers.remove(player.getName());
        //Messaging.send(player, expireText, new Object[0]);
        player.sendMessage("§7You reappear!");
        hero.getPlayer().setSneaking(false);
    }
    
    
    @Override
    public void tickHero(Hero hero)
    {
        float distancesq = distance*distance;
        Player player = hero.getPlayer();
        Player arr$[] = plugin.getServer().getOnlinePlayers();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Player onlinePlayer = arr$[i$];
            if(!onlinePlayer.equals(player)) {
                if(onlinePlayer.getWorld().equals(player.getWorld())) {
                    if(onlinePlayer.getLocation().distanceSquared(player.getLocation()) < distancesq) {
                        onlinePlayer.showPlayer(player);
                    }
                    else {
                        if(!onlinePlayer.hasPermission("heroes.admin.seeinvis")) {
                            onlinePlayer.hidePlayer(player);
                        }
                    }
                }
            }
        }
        
        hero.getPlayer().setSneaking(false);
        hero.getPlayer().setSneaking(true);
    }

    @Override
    public void tickMonster(Monster monster1) {
        
    }

    private final float distance;
    public static final Set hiddenPlayers = new HashSet();

}