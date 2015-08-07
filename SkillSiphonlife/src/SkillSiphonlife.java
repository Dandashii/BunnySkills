import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.left2craft.combatbase.event.HealEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillSiphonlife extends TargettedSkill {
    public SkillSiphonlife(Heroes plugin) {
        super(plugin, "Siphonlife");
        setDescription("Drain life from your target");
        setUsage("/skill Siphonlife");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Siphonlife" });
        
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
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 1);
        node.set(SkillSetting.MANA.node(),5);
        node.set(SkillSetting.COOLDOWN.node(),100);
        node.set("heal-amount",1);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if(target == null ) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        if (!damageCheck(hero.getPlayer(), target)) {
            Messaging.send(hero.getPlayer(), "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        
        if(target==null) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        Player dmger = hero.getPlayer();

        addSpellTarget(target, hero);
        int heal = SkillConfigManager.getUseSetting(hero, this, "heal-amount", 1, false);

        if (target instanceof Player) {
            if(!target.equals(dmger)) {
                Player p = (Player) target;
                Hero tHero = plugin.getCharacterManager().getHero((Player)target);
                
                if(tHero.getPlayer().getHealth()>=damage) {
                    tHero.getPlayer().setHealth(tHero.getPlayer().getHealth()-damage);
                }
                else {
                    damageEntity(p, dmger, damage, EntityDamageEvent.DamageCause.VOID);
                }
                
                HealEvent healevent = new HealEvent(hero.getPlayer(), hero.getPlayer(),(int)Math.round((heal/100.0)*hero.getPlayer().getMaxHealth()));
                Bukkit.getPluginManager().callEvent(healevent);
                if(!healevent.isCancelled()) {
                    hero.getPlayer().setHealth((int)Math.min(hero.getPlayer().getMaxHealth(), hero.getPlayer().getHealth()+healevent.getHealing()));
                }
                else {
                    return SkillResult.INVALID_TARGET_NO_MSG;
                }

                if (hero.getPlayer().getFoodLevel()>15) {
                    hero.getPlayer().setFoodLevel(15);
                }
                //hero.getPlayer().syncHealth();
                //tHero.getPlayer().syncHealth();
            }
        } else if (target instanceof Creature) {
            Creature c = (Creature) target;
            if(c.getHealth()>=(damage)) {
                c.setHealth(c.getHealth()-damage);
            }
            else {
                damageEntity(c, dmger, damage, EntityDamageEvent.DamageCause.MAGIC);
            }
                
            //hero.getPlayer().setHealth((int)(hero.getHealth()+(heal/100.0)*hero.getHealth()));
            
            HealEvent healevent = new HealEvent(hero.getPlayer(), hero.getPlayer(),(int)Math.round((heal/100.0)*hero.getPlayer().getMaxHealth()));
            Bukkit.getPluginManager().callEvent(healevent);
            if(!healevent.isCancelled()) {
                hero.getPlayer().setHealth((int)Math.min(hero.getPlayer().getMaxHealth(), hero.getPlayer().getHealth()+healevent.getHealing()));
            }
            else {
                return SkillResult.INVALID_TARGET_NO_MSG;
            }

            if (hero.getPlayer().getFoodLevel()>15) {
                hero.getPlayer().setFoodLevel(15);
            }
            //hero.getPlayer().syncHealth();
        }
        else {
            return SkillResult.INVALID_TARGET;
        }
        
        target.getWorld().playEffect(target.getLocation(), Effect.POTION_BREAK, 353);
        
        return SkillResult.NORMAL;
    }
    
    

}