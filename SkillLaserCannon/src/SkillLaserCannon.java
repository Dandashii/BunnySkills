import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.*;
import com.herocraftonline.heroes.util.Util;
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

public class SkillLaserCannon extends ActiveSkill {
    
    public SkillLaserCannon(Heroes plugin) {
        super(plugin, "LaserCannon");
        asd=plugin;
        setDescription("You charge your laser cannon and fire it in a straight line in front of you dealing $1 damage.");
        setUsage("/skill LaserCannon");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill LaserCannon" });
        
        setTypes(SkillType.DAMAGING, SkillType.DEBUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        
        int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 30, false));
        
        String description = getDescription().replace("$1", damage + "");
        
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
        node.set(SkillSetting.DAMAGE.node(), 5);
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
    public SkillResult use(Hero hero, String[] args) {
        
        targets.clear();
        
        Location nxt=hero.getPlayer().getLocation();
        nxt= nxt.add(0,1.4,0);
        wave(this,hero,nxt,0,hero.getPlayer().getLocation().getDirection());
        
        
        return SkillResult.NORMAL;
    }
    
    public void wave(Skill laser, Hero hero, Location nxt, int c, final Vector thrv) {
        nxt=nxt.add(thrv);
        //nxt.getWorld().playEffect(nxt, Effect.POTION_BREAK, 245);
        nxt.getWorld().playEffect(nxt,Effect.ENDER_SIGNAL,50);
        c++;
        
        
        
        
        
        
        int distance = SkillConfigManager.getUseSetting(hero, laser, SkillSetting.MAX_DISTANCE, 30, false);
        if(c>distance) {
            return;
        }
        if(!Util.transparentBlocks.contains(nxt.getBlock().getType())) {
            return;
        }
        
        /*
        for(LivingEntity target:getNearbyLivingEntities(nxt,2)) {
            if(!targets.contains(target)) {
                lasercannonTarget(hero, target);
            }
        }*/
        
        
        //final Location fnxt = nxt;
        //final Hero fhero = hero;
        //final int fc = c;
        
        delex(this, nxt,c, hero);
        
        slowdamage(nxt, hero);
        
        wave(laser,hero,nxt,c,thrv);
        /*
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(asd, new Runnable(){                
            @Override public void run(){ wave(fhero,fnxt,fc,thrv); }},(long)(1));*/
        
    }
    
    public void slowdamage(Location nxt, Hero fhero) {
        for(LivingEntity target:getNearbyLivingEntities(nxt,3)) {
            if(!targets.contains(target.getEntityId())) {
                if(Skill.damageCheck(fhero.getPlayer(), target)) {
                    
                    Skill.damageEntity(target, fhero.getPlayer(), 10, EntityDamageEvent.DamageCause.MAGIC, false);
                    if(target instanceof Player) {
                        plugin.getCharacterManager().getHero((Player)target).addEffect(new com.herocraftonline.heroes.characters.effects.common.SlowEffect(this, 1500, 2, false, null, null, fhero));
                    }
                    
                }
            }
        }
    }
    
    public void delex(final Skill laser, Location nxt, int c, final Hero fhero) {
        final Location fnxt = nxt.clone();
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(asd, new Runnable(){                
            @Override public void run(){
                fnxt.getWorld().createExplosion(fnxt, 0, false);
                for(LivingEntity target:getNearbyLivingEntities(fnxt,3)) {
                    if(!targets.contains(target.getEntityId())) {
                        lasercannonTarget(laser, fhero, target);
                    }
                }
            }},(long)(22+c/4));
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius) {
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                            if(e instanceof LivingEntity) {
                                radiusEntities.add((LivingEntity)e);
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
    static Heroes asd;
    List<Integer> targets = new ArrayList<>();
    
    public void lasercannonTarget(Skill laser, Hero hero, LivingEntity target) {
        
        if(damageCheck(hero.getPlayer(), target)) {
            Player dmger = hero.getPlayer();

            addSpellTarget(target, hero);
            
            int damage = SkillConfigManager.getUseSetting(hero, laser, SkillSetting.DAMAGE, 1, false);

            if(target.getFireTicks()>0) {
                damage+=1;
            }
            
            boolean knockback=true;
            
            if (target instanceof Player && !(target.equals(dmger))) {
                Player p = (Player) target;
                damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.MAGIC,knockback);
            } else {
                damageEntity(target, dmger, damage, EntityDamageEvent.DamageCause.MAGIC,knockback);
            }
            targets.add(target.getEntityId());
        }
        
    }
    
    
    

}