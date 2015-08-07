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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillGodlyWrath extends ActiveSkill {

    public SkillGodlyWrath(Heroes plugin) {
        super(plugin, "GodlyWrath");
        setDescription("You strike down all nearby enemies, dealing $1 damage.");
        setUsage("/skill godlywrath");
        setArgumentRange(0, 0);
        setIdentifiers("skill godlywrath");
        setTypes(SkillType.BUFF);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), 30);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        node.set(SkillSetting.RADIUS.node(), 10);
        node.set(SkillSetting.RADIUS_INCREASE.node(), 0);
        return node;
    }

    @Override
    public void init() {
        super.init();
        setUseText("%hero% casts GodlyWrath!".replace("%hero%", "$1"));
    }
    
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 30, false);
        damage += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
        radius += SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        
        for(Entity le:hero.getPlayer().getNearbyEntities(radius, radius, radius)) {
            if(le instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)le;
                if(damageCheck(hero.getPlayer(), target)) {
                    damageEntity(target, hero.getPlayer(), damage, DamageCause.MAGIC, false);
                    target.getWorld().strikeLightningEffect(target.getLocation());
                }
            }
        }
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    @Override
    public String getDescription(Hero hero) {
        int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
        int period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, false);
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
        damage = damage * duration / period;
        return getDescription().replace("$1", damage + "").replace("$2", duration / 1000 + "");
    }
}
