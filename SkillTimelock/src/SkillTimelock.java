import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class SkillTimelock extends ActiveSkill implements Listener {
    
    public SkillTimelock(Heroes plugin) {
        super(plugin, "Timelock");
        setDescription("You slow time for all players around you for $1s");
        setUsage("/skill Timelock");
        setArgumentRange(0, 0);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        setIdentifiers(new String[]{"skill Timelock"});
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false));
                
        String description = getDescription().replace("$1", (int)(duration/1000) + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 8000);
        node.set(SkillSetting.RADIUS.node(),5);
        node.set("speed-amplifier",3);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);
        final long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 8000, false);
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 5, false);
        int amp = SkillConfigManager.getUseSetting(hero, this, "speed-amplifier", 3, false);
        //hero.addEffect(new com.herocraftonline.heroes.characters.effects.ExpirableEffect(voidshift, "voidshift", duration));
        final Hero fHero = hero;
        
        for(Player p: getNearbyPlayers(hero.getPlayer().getLocation(),radius)) {
            if(damageCheck(fHero.getPlayer(),p)) {
                p.setVelocity(new Vector(0,0.8,0));
                Hero h = plugin.getCharacterManager().getHero(p);
                //h.addEffect(new ImpStunEffect(timelock,duration));
                h.addEffect(new SlowEffect(this,duration,amp,true,"","",hero));
            }
            /*
            final Hero h = plugin.getCharacterManager().getHero(p);
            
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if(damageCheck(fHero.getPlayer(),h.getPlayer())) {
                            h.addEffect(new ImpStunEffect(timelock,duration));
                        }
                    }
                }, (13));*/
        }
        return SkillResult.NORMAL;
    }
    
    /*
    public class ImpStunEffect extends StunEffect {
        public ImpStunEffect(Skill skill,Long duration) {
            super(skill, duration);
            
            int tickDuration = (int) (duration / 1000) * 20;
            addMobEffect(2, tickDuration, 4, false);
            addMobEffect(8, tickDuration, -4, false);
        }
    }*/
    
    public static List<Player> getNearbyPlayers(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<Player> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if(e.getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof Player) {
                                    radiusEntities.add((Player)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
}