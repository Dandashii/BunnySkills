import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillBloodSacrifice extends ActiveSkill {

    public SkillBloodSacrifice(Heroes plugin) {
        super(plugin, "BloodSacrifice");
        setDescription("Deals half your current HP in damage to yourself and those around you.");
        setUsage("/skill bloodsacrifice");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill BloodSacrifice" });
        
        setTypes(SkillType.DAMAGING, SkillType.BUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, "scatter", 2, false) -
                (SkillConfigManager.getUseSetting(hero, this, "scatter-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", radius + "").replace("$2", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 100, false)
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
        node.set(SkillSetting.COOLDOWN.node(),45000);
        node.set("sacrifice-percent",50);
        node.set(SkillSetting.RADIUS.node(),8);
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int sac = SkillConfigManager.getUseSetting(hero, this, "sacrifice-percent", 50, false);
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 8, false);
        
        int selfdamage = (int)Math.round(hero.getPlayer().getHealth()*(sac/100.0));
        
        for(Entity e:hero.getPlayer().getNearbyEntities(radius,radius,radius)) {
            if(e instanceof LivingEntity) {
                if(damageCheck(hero.getPlayer(),(LivingEntity)e)) {
                    int damage = (int)Math.round(((LivingEntity)e).getHealth()*(sac/100.0));
                    if(e instanceof Player) {
                        addSpellTarget((Player)e, hero);
                    }
                    if(e instanceof Creature) {
                        damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC,false);
                    }
                    else if(e instanceof Player) {
                        
                        Hero tHero = plugin.getCharacterManager().getHero((Player)e);
                        damageEntity(tHero.getPlayer(), hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC,false);
                    }
                    e.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245); 
                }
            }
        }
        if(hero.getPlayer().getHealth()>selfdamage) {
            //hero.getPlayer().setHealth(hero.getPlayer().getHealth()-selfdamage);
            
            //Skill.damageEntity(hero.getPlayer(), hero.getPlayer(), selfdamage, EntityDamageEvent.DamageCause.MAGIC, false);
            
            hero.getPlayer().setHealth(Math.max(1, hero.getPlayer().getHealth()-selfdamage));
            
            //hero.syncHealth();
        }
        double x=hero.getPlayer().getLocation().getX();
        double y=hero.getPlayer().getLocation().getY();
        double z=hero.getPlayer().getLocation().getZ();
        
        for(int i=0;i<40;i++) {
            int xr = (int)Math.round((Math.random()*radius*2)-radius);
            int yr = (int)Math.round((Math.random()*radius*2)-radius);
            int zr = (int)Math.round((Math.random()*radius*2)-radius);
            
            Location loc = new Location(hero.getPlayer().getWorld(),x+xr,y+yr,z+zr);
            
            if(Math.random()>=0.82) {
                hero.getPlayer().getWorld().playEffect(loc, Effect.POTION_BREAK, 245);
            }
            else {
                hero.getPlayer().getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES,50);
            }
        }
        
        return SkillResult.NORMAL;
    }
}