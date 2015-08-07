import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class SkillCraneStance extends ActiveSkill {

    public SkillCraneStance(Heroes plugin) {
        super(plugin, "CraneStance");
        setDescription("Activates crane stance. Your attacks will cost hunger but heal you");
        setUsage("/skill CraneStance");
        
        setArgumentRange(0, 1);
        setIdentifiers("skill CraneStance");
        
        setTypes(SkillType.EARTH);
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
        
        node.set(SkillSetting.COOLDOWN.node(), 5000);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        final Hero fhero = hero;
        
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this,"CraneStanceEffect"));
        if(hero.hasEffect("DragonStanceEffect")) {
            hero.removeEffect(hero.getEffect("DragonStanceEffect"));
        }
        
        long cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 5000, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        hero.setCooldown("CraneStance", cooldown + System.currentTimeMillis());
        hero.setCooldown("DragonStance", cooldown + System.currentTimeMillis());
        hero.setCooldown("StanceDance", cooldown + System.currentTimeMillis());
        
        
        fhero.getPlayer().getWorld().playEffect(fhero.getPlayer().getLocation(), Effect.POTION_BREAK, 612);   
        for(int i=1; i<4; i++) {
            final int ii=i;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                    fhero.getPlayer().getWorld().playEffect(new Location(fhero.getPlayer().getWorld(),fhero.getPlayer().getLocation().getX(),fhero.getPlayer().getLocation().getY()+(ii-1),fhero.getPlayer().getLocation().getZ()), Effect.POTION_BREAK, 612);
                }
            }, i);
        }
        
        return SkillResult.NORMAL;
    }
}