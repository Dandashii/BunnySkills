import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;

public class SkillBerserkerRage extends ActiveSkill {

    public SkillBerserkerRage(Heroes plugin) {
        super(plugin, "BerserkerRage");
        setDescription("Swaps you between melee and ranged axes. Melee axes have a chance to stun and while that stun is on cooldown your ranged axes will go on fire and deal bonus damage.");
        setUsage("/skill berserkerrage");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill BerserkerRage" });
        
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
        node.set(SkillSetting.COOLDOWN.node(),1000);
        node.set("speed-amplifier",1);
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        if(hero.hasEffect("stun-axes")) {
            hero.removeEffect(hero.getEffect("stun-axes"));
            hero.getPlayer().sendMessage("§7You hold your axes up, ready to §4throw§7!");
            if(hero.hasEffect("berserker-speed")) {
                hero.removeEffect(hero.getEffect("berserker-speed"));
            }
        }
        else {
            hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this, "stun-axes"));
            hero.getPlayer().sendMessage("§7You grip your axes firmly, ready for §4melee§7 combat!");
            int amp = (int) (SkillConfigManager.getUseSetting(hero, this, "speed-amplifier", 1, false));
            amp = amp > 0 ? amp : 0;
            //hero.addEffect(new com.herocraftonline.heroes.characters.effects.common.QuickenEffect(this, "berserker-speed", 600000, amp, null, null));
            hero.addEffect(new CombatSprint(this, plugin, "berserker-speed", 600000, amp));
        }
        
        return SkillResult.NORMAL;
    }
}