import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.*;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SkillScorch extends ActiveSkill {
    
    
    public SkillScorch(Heroes plugin) {
        super(plugin, "Scorch");
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
        node.set("fullstackbonus-damage",1);
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        targets.clear();
        
        Location nxt=hero.getPlayer().getLocation();
        //wave(hero,nxt,0);
        wave(hero,nxt.clone().add(0, 1, 0),0);
        
        
        return SkillResult.NORMAL;
    }
    
    public void wave(Hero hero, Location nxt, int c) {
        Vector thrv = hero.getPlayer().getLocation().getDirection();
        nxt=nxt.add(thrv);
        nxt.getWorld().playEffect(nxt,Effect.MOBSPAWNER_FLAMES,50);
        c++;
        
        
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 30, false);
        if(c>distance) {
            return;
        }
        
        boolean hit=false;
        for(LivingEntity target:getNearbyLivingEntities(nxt,2)) {
            if(!targets.contains(target.getEntityId())) {
                if(scorchTarget(hero, target)) {
                    hit=true;
                }
            }
        }
        if(hit) {
            return;
        }
        
        if(nxt.getBlock().getType().isSolid()) {
            return;
        }
        
        final Location fnxt = nxt;
        final Hero fhero = hero;
        final int fc = c;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(asd, new Runnable(){                
            @Override public void run(){ wave(fhero,fnxt,fc); }},(long)(1));
        
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()) {
                        if(e.getLocation().getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof LivingEntity) {
                                    radiusEntities.add((LivingEntity)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
    static Heroes asd;
    List<Integer> targets = new ArrayList<>();
    
    public boolean scorchTarget(Hero hero, LivingEntity target) {
        boolean hit = false;
        if(damageCheck(hero.getPlayer(), target)) {
            Player dmger = hero.getPlayer();

            // auto attack, not spell
            //addSpellTarget(target, hero);
            
            int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
            int d2 = SkillConfigManager.getUseSetting(hero, this, "fullstackbonus-damage", 1, false);

            long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 1000, false);
            
            if(target.getFireTicks()>0) {
                damage+=d2;
            }
            
            if (target instanceof Player && !(target.equals(dmger))) {
                Player p = (Player) target;
                Hero tHero = plugin.getCharacterManager().getHero((Player)target);
                boolean knockback=false;
                    if(tHero.getPlayer().getHealth()>damage) {
                        //tHero.setHealth(tHero.getHealth()-damage);
                        if(tHero.hasEffect("Scorch1")) {
                            tHero.addEffect(new ExpirableEffect(this, "Scorch2", duration+10000));
                            tHero.removeEffect(tHero.getEffect("Scorch1"));
                        }
                        else if(tHero.hasEffect("Scorch2")) {
                            tHero.removeEffect(tHero.getEffect("Scorch2"));
                            tHero.addEffect(new ExpirableEffect(this, "Scorch3", duration+10000));
                            hero.getPlayer().sendMessage("Your target has reached maximum scorch stacks and is ready to combust!");
                            tHero.getPlayer().getWorld().createExplosion(tHero.getPlayer().getLocation(), 0, false);
                            target.setFireTicks((int)(duration/50.0));
                            knockback=true;
                        }
                        else if(tHero.hasEffect("Scorch3")) {
                            //tHero.removeEffect(tHero.getEffect("Scorch3"));
                            //tHero.addEffect(new ExpirableEffect(this, "Scorch1", duration+10000));
                            damage+=d2;
                        }
                        else {
                            tHero.addEffect(new ExpirableEffect(this, "Scorch1", duration+10000));
                        }
                        
                        hit=damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.PROJECTILE,knockback);
                    }
                    else {
                        tHero.getPlayer().setFireTicks(0);
                        hit=damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.PROJECTILE);
                    }

            } else {

                if(target.getHealth()>(damage)){
                    //target.setHealth(target.getHealth()-damage);
                    target.setFireTicks((int)(duration/50.0));
                    hit=damageEntity(target, dmger, damage, EntityDamageEvent.DamageCause.PROJECTILE,false);
                }
                else {
                    target.setFireTicks(0);
                    hit=damageEntity(target, dmger, damage, EntityDamageEvent.DamageCause.PROJECTILE);
                }
            }
        //fireEffect(hero.getPlayer().getLocation(),target);
        targets.add(target.getEntityId());
        }
        return hit;
    }
    
    
    

}