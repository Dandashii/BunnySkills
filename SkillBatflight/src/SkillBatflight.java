import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SafeFallEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SkillBatflight extends ActiveSkill {

    public SkillBatflight(Heroes plugin) {
        super(plugin, "Batflight");
        setDescription("You turn into a bat! Keep pressing to flap your wings!");
        setUsage("/skill Batflight");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Batflight" });
        
        
        setTypes(SkillType.KNOWLEDGE);
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
        
        node.set(SkillSetting.DURATION.node(), 3000);
        node.set("strength", 1);
        node.set("real-cooldown", 60000);
        node.set(SkillSetting.COOLDOWN.node(), 300);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 3000, false);
        int strength = SkillConfigManager.getUseSetting(hero, this, "strength", 1, false);
        int cooldown = SkillConfigManager.getUseSetting(hero, this, "real-cooldown", 60000, false);
        
        if((hero.getCooldown("batflightcd") == null || hero.getCooldown("batflightcd") <= System.currentTimeMillis())) {
            hero.addEffect(new com.herocraftonline.heroes.characters.effects.ExpirableEffect(this, "batflight", duration));
            hero.addEffect(new SafeFallEffect(this,duration));
            if (player.isOp()) {
                player.performCommand("disguise bat");
            } else {
                player.setOp(true);
                player.performCommand("disguise bat");
                player.setOp(false);
            }
            hero.setCooldown("batflightcd", cooldown + System.currentTimeMillis());
            final Player wp = player;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    new Runnable() {
                        @Override
                        public void run() {
                            if(plugin.getCharacterManager().getHero(wp).hasEffect("batflight"))
                                plugin.getCharacterManager().getHero(wp).removeEffect(plugin.getCharacterManager().getHero(wp).getEffect("batflight"));
                                
                                if (wp.isOp()) {
                                    wp.performCommand("undisguise");
                                } else {
                                    wp.setOp(true);
                                    wp.performCommand("undisguise");
                                    wp.setOp(false);
                                }
                        }
                    }
            ,((duration+200)/50));
        }
        
        if(hero.hasEffect("batflight")) {
            
            Vector l = player.getLocation().getDirection();
            l.setY(0);
            Vector thrv = l.normalize();
            thrv.setY(0.8);
            l.multiply(strength*0.8);
            
            player.setVelocity(thrv);
            
            
        }
        else if(!(hero.getCooldown("batflightcd") == null || hero.getCooldown("batflightcd") <= System.currentTimeMillis())){
            hero.getPlayer().sendMessage("Batflight is still on cooldown: "+(hero.getCooldown("batflightcd")-System.currentTimeMillis())/1000+"s");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        return SkillResult.NORMAL;
    }
    
}
