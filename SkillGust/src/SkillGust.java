import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SkillGust extends ActiveSkill {

    public SkillGust(Heroes plugin) {
        super(plugin, "Gust");
        setDescription("Knocks your enemies back");
        setUsage("/skill Gust");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Gust" });
        
        setTypes(SkillType.TELEPORT, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
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
        node.set("strength", 2);
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        int strength = (int) (SkillConfigManager.getUseSetting(hero, this, "strength", 10, false));
        strength = strength > 0 ? strength : 0;
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 10, false));
        distance = distance > 0 ? distance : 0;
        
        
        
        Vector dir = hero.getPlayer().getLocation().getDirection();
        dir = dir.normalize();
        Location next = hero.getPlayer().getLocation();
        next.setY(next.getY()+1.5);
        double mul = 2;
        
        List<LivingEntity> targets = new ArrayList<>();
        
        do
        {
            next = next.add(dir.getX()*mul, dir.getY()*mul, dir.getZ()*mul);
            next.getWorld().playEffect(next,Effect.BLAZE_SHOOT,50);
            next.getWorld().playEffect(next,Effect.SMOKE,50);
            
            
            for(LivingEntity e:getNearbyLivingEntities(next, (int)(2+Math.round(next.distance(hero.getPlayer().getLocation().getBlock().getLocation()))/4.0))) {
                if(damageCheck(hero.getPlayer(),e)) {
                    addSpellTarget(e,hero);
                    targets.add(e);
                }
            }

        }while(next.distance(hero.getPlayer().getLocation().getBlock().getLocation())<distance);

        for(LivingEntity e:targets) {
                if(e instanceof Player) {
                        //damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        Messaging.send((Player) e, "A vicious shockwave pushes you back!");
                }/*
                if(e instanceof Creature) {
                        damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                }*/
                next.getWorld().playEffect(e.getLocation(),Effect.BLAZE_SHOOT,50);
                next.getWorld().playEffect(e.getLocation(),Effect.SMOKE,50);
                        //Vector thrvv = player.getLocation().subtract(target.getLocation()).toVector().multiply(strength*0.1);
                Vector thrv = e.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(strength*0.9);
                thrv.setY(0.4);
                e.setVelocity(thrv);
        }
        
        return SkillResult.NORMAL;
    }

    public static List<LivingEntity> getNearbyLivingEntities(Location l, int radius){
        int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16))/16;
        List<LivingEntity> radiusEntities=new ArrayList<>();
        int radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if(e.getWorld().equals(l.getWorld())) {
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
    
}