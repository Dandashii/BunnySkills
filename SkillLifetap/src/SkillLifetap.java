import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;

public class SkillLifetap extends ActiveSkill {

    public SkillLifetap(Heroes plugin) {
        super(plugin, "Lifetap");
        setDescription("Consumes some health to restore mana");
        setUsage("/skill Lifetap");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Lifetap" });
        
        setTypes(SkillType.DAMAGING, SkillType.DARK, SkillType.MANA, SkillType.SILENCABLE);
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
        node.set(SkillSetting.COOLDOWN.node(),1000);
        node.set("life-tap",10);
        node.set("mana-given", 5);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int managive = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-given",5, false));
        managive = managive > 0 ? managive : 0;
        
        int hcost = (int) (SkillConfigManager.getUseSetting(hero, this, "life-tap",10, false));
        hcost = hcost > 0 ? hcost : 0;
        
        int tapamt = ((int)Math.round(hero.getPlayer().getHealth()*(hcost/100.0)));
        
        if(hero.getPlayer().getHealth()<=tapamt) {
            return SkillResult.LOW_HEALTH;
        }
        
        if(hero.getMana()>=hero.getMaxMana()) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        hero.setMana(hero.getMana()+managive);
        hero.getPlayer().setHealth(hero.getPlayer().getHealth()-tapamt);
        //hero.syncHealth();
        hero.getPlayer().getWorld().playEffect(hero.getPlayer().getLocation(), Effect.POTION_BREAK, 245); 
        
        return SkillResult.NORMAL;
    }

}