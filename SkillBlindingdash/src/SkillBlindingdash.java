import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.List;
import net.minecraft.server.MobEffect;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SkillBlindingdash extends ActiveSkill {

    public SkillBlindingdash(Heroes plugin) {
        super(plugin, "Blindingdash");
        setDescription("Your erratic movements strike fear into the hearts of your enemies, blinding them multiple times");
        setUsage("/skill Blindingdash");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Blindingdash" });
        
        
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
        
        node.set(SkillSetting.DURATION.node(), 300);
        node.set("speed", 2);
        node.set("repetitions", 10);
        node.set(SkillSetting.COOLDOWN.node(), 20000);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        final long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 300, false);
        int rep = SkillConfigManager.getUseSetting(hero, this, "repetitions", 10, false);
        int speed = SkillConfigManager.getUseSetting(hero, this, "speed", 2, false);
        
        hero.addEffect(new com.herocraftonline.heroes.characters.effects.common.QuickenEffect(this,"Blindingdash",rep*duration,speed,"",""));
        
        final Player pla = player;
        
        for(int i=0;i<rep;i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    new Runnable() {
                        @Override
                        public void run() {
                            Hero me = plugin.getCharacterManager().getHero(pla);
                            
                            final List<Entity> flist = me.getPlayer().getNearbyEntities(20,8,20);
                            for(Entity e:flist) {
                                if(e instanceof Player) {
                                    Player asd = (Player)e;
                                    if(damageCheck(me.getPlayer(),asd)) {
                                        Hero nearher = plugin.getCharacterManager().getHero(asd);
                                        nearher.addEffect(new com.herocraftonline.heroes.characters.effects.common.BlindEffect(plugin.getSkillManager().getSkill("Blindingdash"), duration, "", ""));
                                        CraftPlayer p = (CraftPlayer) nearher.getPlayer();
                                        p.getHandle().addEffect(new MobEffect(15, (int)Math.round(duration), 1));
                                    }
                                }
                            }
                        }
                    }
            ,Math.round((i*duration+Math.round(i*0.7*duration))/50.0));
        }
        
        return SkillResult.NORMAL;
    }
}
