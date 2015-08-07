import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SafeFallEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SkillDragonrush extends ActiveSkill {

    public SkillDragonrush(Heroes plugin) {
        super(plugin, "Dragonrush");
        setDescription("Soars into the air, knocking back and stunning nearby enemies");
        setUsage("/skill Dragonrush");
        
        setArgumentRange(0, 1);
        setIdentifiers("skill Dragonrush");
        
        setTypes(SkillType.DARK, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
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
        
        node.set(SkillSetting.MAX_DISTANCE.node(), 25);
        node.set(SkillSetting.RADIUS.node(), 5);
        node.set(SkillSetting.DURATION.node(), 2000);
        node.set(SkillSetting.DAMAGE.node(), 0);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        node.set("strength", 100);
        node.set("strength-increase", 0);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
            damage = damage > 0 ? damage : 0;
        double strength = (int) (SkillConfigManager.getUseSetting(hero, this, "strength", 100, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "strength-increase", 0.0, false) * hero.getSkillLevel(this)));
            strength = strength > 0 ? strength : 0;
            strength /= 100;
        int duration = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 1, false));
            duration = duration > 0 ? duration : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 1, false));
            radius = radius > 0 ? radius : 0;
        
        hero.getPlayer().setVelocity(hero.getPlayer().getVelocity().setY(2));
        
        hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.SMOKE, 1);
        
        final ActiveSkill fthis = this;
        final int fradius = radius;
        final double fstrength = strength;
        final int fduration = duration;
        final int fdamage = damage;
        final Player fplayer = player;
        final List<Entity> flist = player.getNearbyEntities(radius,5,radius);
        final Location floc = player.getLocation();
        
        final Hero fhero = hero;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                fhero.getPlayer().setVelocity(fhero.getPlayer().getVelocity().setY(-6));
                fhero.addEffect(new SafeFallEffect(fthis,2000));
                fhero.getPlayer().getWorld().playEffect(fhero.getPlayer().getLocation(), Effect.SMOKE, 1);
                //fhero.getPlayer().getWorld().playEffect(fhero.getPlayer().getLocation(), Effect.POTION_BREAK, 245);
            }
        }, 10);
        
        double strong=0;
        if(hero.hasEffect("Dragon's Strength")) {
            strong=1;
        }
        final double str = strong;
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
        public void run() {
                for(Entity e: flist) {
                    if(e instanceof LivingEntity) {
                        if(damageCheck(fhero.getPlayer(),(LivingEntity)e)) {
                            damageEntity(((LivingEntity)e), fplayer, fdamage, DamageCause.MAGIC);
                        }
                    }
                    if(e instanceof Player) {
                        final Player pl = (Player) e;
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if(damageCheck(fhero.getPlayer(),pl)) {
                                    plugin.getCharacterManager().getHero(pl).addEffect(new StunEffect(fthis,fduration));
                                }
                            }
                        }, 10);
                    }
                    Vector thrv = e.getLocation().subtract(floc).toVector().normalize().multiply(fstrength-0.3+str).setY(0.3);
                    e.setVelocity(thrv);
                    addSpellTarget(e, fhero);
                }
                fhero.getPlayer().getWorld().createExplosion(fhero.getPlayer().getLocation(), 0, false);
            }
        }, 16);
        
        
        return SkillResult.NORMAL;
    }
}