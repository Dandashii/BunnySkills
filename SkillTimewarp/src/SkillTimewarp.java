import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillTimewarp extends ActiveSkill {

    public SkillTimewarp(Heroes plugin) {
        super(plugin, "Timewarp");
        setDescription("After a few seconds you will be sent back to that point in time restoring health, mana, buffs, debuffs and position");
        setUsage("/skill Timewarp");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Timewarp" });
        
        setTypes(SkillType.ILLUSION);
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 200, false) -
                (SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", radius + "").replace("$2", distance + "");
        
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
        node.set(SkillSetting.DURATION.node(), 6000);
        node.set(SkillSetting.COOLDOWN.node(), 30000);
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        int duration = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false));
        duration = duration > 0 ? duration : 0;
        
        //if(hero.hasEffect("Timewarp")) {
        //    hero.removeEffect(new Effect(this, "Timewarp"));
        //}
        if(!hero.hasEffect("Timewarp")) {
            hero.addEffect(new ExpirableEffect(this, "Timewarp", duration+1000));
            
            final Player pla = hero.getPlayer();
            
            final int health = hero.getHealth();
            final int mana = hero.getMana();
            final Location loc = hero.getPlayer().getLocation();
            
            final Map<String, Long> cds = hero.getCooldowns();
            
            final Iterator<String> keyset = hero.getCooldowns().keySet().iterator();
            final Iterator<Long> longset = hero.getCooldowns().values().iterator();
            
            final Iterator<Effect> effs = hero.getEffects().iterator();
            
            /*
            final long prevtime = System.currentTimeMillis();
            final long cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false);
            final int fduration = duration;
            */
            Location loca = hero.getPlayer().getLocation();
            
            
            hero.getPlayer().getWorld().playEffect(new Location(hero.getPlayer().getWorld(),loca.getX()+1,loca.getY(),loca.getZ()), org.bukkit.Effect.POTION_BREAK, 23);
            hero.getPlayer().getWorld().playEffect(new Location(hero.getPlayer().getWorld(),loca.getX()-1,loca.getY(),loca.getZ()), org.bukkit.Effect.POTION_BREAK, 23);
            hero.getPlayer().getWorld().playEffect(new Location(hero.getPlayer().getWorld(),loca.getX(),loca.getY(),loca.getZ()-1), org.bukkit.Effect.POTION_BREAK, 23);
            hero.getPlayer().getWorld().playEffect(new Location(hero.getPlayer().getWorld(),loca.getX(),loca.getY(),loca.getZ()+1), org.bukkit.Effect.POTION_BREAK, 23);
            
            
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Hero fhero = plugin.getCharacterManager().getHero(pla);
                    if(fhero.hasEffect("Timewarp")) {
                        fhero.removeEffect(fhero.getEffect("Timewarp"));
                        
                        fhero.getPlayer().setHealth(health);
                        fhero.setMana(mana);
                        
                        fhero.clearEffects();
                        //fhero.clearCooldowns();
                        
                        /*while(keyset.hasNext()) {
                            String cdn = keyset.next();
                            long cdv = longset.next()-prevtime+System.currentTimeMillis();
                            fhero.setCooldown(cdn,cdv);
                            //System.out.println(cdn+": "+cdv);
                        }
                        fhero.setCooldown("Timewarp",System.currentTimeMillis()+cooldown-fduration);
                        */
                        
                        while(effs.hasNext()) {
                            Effect eff = effs.next();
                            fhero.addEffect(eff);
                        }
                        
                        Location locat = fhero.getPlayer().getLocation();
                        fhero.getPlayer().getWorld().playEffect(new Location(fhero.getPlayer().getWorld(),locat.getX()+1,locat.getY(),locat.getZ()), org.bukkit.Effect.POTION_BREAK, 23);
                        fhero.getPlayer().getWorld().playEffect(new Location(fhero.getPlayer().getWorld(),locat.getX()-1,locat.getY(),locat.getZ()), org.bukkit.Effect.POTION_BREAK, 23);
                        fhero.getPlayer().getWorld().playEffect(new Location(fhero.getPlayer().getWorld(),locat.getX(),locat.getY(),locat.getZ()-1), org.bukkit.Effect.POTION_BREAK, 23);
                        fhero.getPlayer().getWorld().playEffect(new Location(fhero.getPlayer().getWorld(),locat.getX(),locat.getY(),locat.getZ()+1), org.bukkit.Effect.POTION_BREAK, 23);
                        
                        fhero.getPlayer().setFallDistance(0);
                        
                        fhero.getPlayer().teleport(loc);
                        
                        //fhero.syncHealth();
                    }
                }
            }, Math.round(duration/50.0));
            
        }
        
        
        return SkillResult.NORMAL;
    }
}
