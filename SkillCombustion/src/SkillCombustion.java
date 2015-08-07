import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SkillCombustion extends ActiveSkill {
    public SkillCombustion(Heroes plugin) {
        super(plugin, "Combustion");
        setDescription("Combust targets around you dealing damage based on how much scorch has stacked");
        setUsage("/skill Combustion");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Combustion" });
        
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
        node.set(SkillSetting.MANA.node(),5);
        node.set(SkillSetting.COOLDOWN.node(),100);
        //node.set(SkillSetting.RADIUS.node(),25);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int dmg = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 1, false);
        
        for(Entity target:hero.getPlayer().getNearbyEntities(radius,5,radius)) {
            if(target instanceof LivingEntity) {
                if(damageCheck(hero.getPlayer(), (LivingEntity)target)) {
                    addSpellTarget(target, hero);

                    int damage = dmg;
                    if(target instanceof Player) {
                        Hero tHero = plugin.getCharacterManager().getHero((Player)target);
                                    if(tHero.hasEffect("Scorch1")) {
                                        damage=(int)Math.round(damage/3.0);
                                    }
                                    else if(tHero.hasEffect("Scorch2")) {
                                        damage=(int)Math.round(damage/2.0);
                                    }
                                    else if(tHero.hasEffect("Scorch3")) {
                                        damage=damage*1;
                                    }
                                    else {
                                        damage=2;
                                    }
                            damageEntity(tHero.getPlayer(), hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                            target.setVelocity(new Vector(0,1,0));
                            target.getWorld().createExplosion(target.getLocation(), 0, false);
                    }
                    else if (target instanceof LivingEntity) {
                        if(target.getFireTicks()>0) {
                            damageEntity((LivingEntity)target, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                            target.setVelocity(new Vector(0,1,0));
                            target.getWorld().createExplosion(target.getLocation(), 0, false);
                        }
                    }
                }
            }
        }
        return SkillResult.NORMAL;
    }
    
    

}