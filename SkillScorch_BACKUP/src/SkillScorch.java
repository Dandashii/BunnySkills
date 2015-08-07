import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SkillScorch extends TargettedSkill {
    
    static Skill scorch;
    
    public SkillScorch(Heroes plugin) {
        super(plugin, "Scorch");
        scorch=this;
        asd=plugin;
        setDescription("Burn your target alive. This stacks 3 times to increase combustion damage. Your target will ignite on the 3rd stack.");
        setUsage("/skill Scorch");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Scorch" });
        
        setTypes(SkillType.DAMAGING, SkillType.DEBUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", distance + "");
        
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 30);
        node.set(SkillSetting.DAMAGE.node(), 1);
        node.set(SkillSetting.DURATION.node(), 1000);
        node.set(SkillSetting.MANA.node(),5);
        node.set(SkillSetting.COOLDOWN.node(),100);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if(target == null ) return SkillResult.INVALID_TARGET_NO_MSG;
        
        if (!damageCheck(hero.getPlayer(), target)) {
            //Messaging.send(hero.getPlayer(), "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        
        
        if(damageCheck(hero.getPlayer(), target)) {
            Player dmger = hero.getPlayer();

            addSpellTarget(target, hero);
            
            int damage = SkillConfigManager.getUseSetting(hero, scorch, SkillSetting.DAMAGE, 1, false);

            long duration = SkillConfigManager.getUseSetting(hero, scorch, SkillSetting.DURATION, 1000, false);

            if(target.getFireTicks()>0)
                damage+=1;
            
            if (target instanceof Player && !(target.equals(dmger))) {
                Player p = (Player) target;
                Hero tHero = plugin.getCharacterManager().getHero((Player)target);
                boolean knockback=false;
                    if(tHero.getHealth()>damage) {
                        //tHero.setHealth(tHero.getHealth()-damage);
                        if(tHero.hasEffect("Scorch1")) {
                            tHero.addEffect(new ExpirableEffect(scorch, "Scorch2", duration+10000));
                            tHero.removeEffect(tHero.getEffect("Scorch1"));
                        }
                        else if(tHero.hasEffect("Scorch2")) {
                            tHero.removeEffect(tHero.getEffect("Scorch2"));
                            tHero.addEffect(new ExpirableEffect(scorch, "Scorch3", duration+10000));
                            hero.getPlayer().sendMessage("Your target has reached maximum scorch stacks and is ready to combust!");
                            target.setFireTicks((int)(duration/50.0));
                        }
                        else if(tHero.hasEffect("Scorch3")) {
                            //tHero.removeEffect(tHero.getEffect("Scorch3"));
                            //tHero.addEffect(new ExpirableEffect(this, "Scorch1", duration+10000));
                            damage+=1;
                            knockback=true;
                        }
                        else {
                            tHero.addEffect(new ExpirableEffect(scorch, "Scorch1", duration+10000));
                        }
                        
                        damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.MAGIC,knockback);
                    }
                    else {
                        tHero.getPlayer().setFireTicks(0);
                        damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                    }

            } else {

                if(target.getHealth()>(damage)){
                    target.setHealth(target.getHealth()-damage);
                    target.setFireTicks((int)(duration/50.0));
                    damageEntity(target, dmger, damage, EntityDamageEvent.DamageCause.MAGIC,false);
                }
                else {
                    target.setFireTicks(0);
                    damageEntity(target, dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
                }
            }
        fireEffect(hero.getPlayer().getLocation(),target);
        }
        else return SkillResult.INVALID_TARGET_NO_MSG;
        
        return SkillResult.NORMAL;
    }
    
    static Heroes asd;
    
    public void fireEffect(Location loc, LivingEntity tar) {
        if(loc==null || tar==null)
            return;
        if(loc.distanceSquared(tar.getLocation())<3.5)
            return;
        if(!loc.getWorld().equals(tar.getWorld()))
            return;
        if(tar==null || tar.isDead())
            return;
        if(tar instanceof Player) {
            if(!((Player)tar).isOnline()) {
                return;
            }
        }
        
        Vector thrv = tar.getLocation().subtract(loc).toVector().normalize();
        loc=loc.add(thrv);
        loc.getWorld().playEffect(loc,Effect.MOBSPAWNER_FLAMES,50);
        
        final Location floc = loc;
        final LivingEntity ftar = tar;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(asd, new Runnable(){                
            @Override public void run(){ fireEffect(floc,ftar); }},(long)(1));
    }

}