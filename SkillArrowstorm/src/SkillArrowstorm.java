import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.left2craft.stathandler.StatEffect;
import org.bukkit.configuration.ConfigurationSection;

public class SkillArrowstorm extends ActiveSkill {

    public SkillArrowstorm(Heroes plugin) {
        super(plugin, "Arrowstorm");
        setDescription("Rapidly increases your rate of fire for a few seconds");
        setUsage("/skill arrowstorm");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Arrowstorm" });
        
        setTypes(SkillType.DAMAGING, SkillType.BUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        
        String description = getDescription();
        
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
        //node.set("berserk-duration", 10000);
        //node.set("berserk-cooldown", 60000);
        node.set("attack-speed", 200);
        node.set("damage-reduction", 50);
        
        node.set(SkillSetting.COOLDOWN.node(),60000);
        node.set(SkillSetting.DURATION.node(),10000);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false));
        duration = duration > 0 ? duration : 0;
        
        double amplifier = (SkillConfigManager.getUseSetting(hero, this, "attack-speed", 200, false));
        amplifier = amplifier > 0 ? amplifier : 0;
        
        double red = (SkillConfigManager.getUseSetting(hero, this, "damage-reduction", 50, false));
        red = red > 0 ? red : 0;
        
        if(amplifier>0) {
            StatEffect eff = new StatEffect("ArrowStormSpeed", duration);
            eff.addStat("attackspeed", amplifier);
            eff.addStat("percentdamage", -red);
            hero.addEffect(eff);
            hero.getPlayer().sendMessage("§7You ready your bow for rapid fire!");
        }
        return SkillResult.NORMAL;
    }
}