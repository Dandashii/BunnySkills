import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillDivineProtection extends ActiveSkill implements Listener {

    public SkillDivineProtection(Heroes plugin) {
        super(plugin, "DivineProtection");
        setDescription("You take reduced damage from all attacks, remove all debuffs from yourself, and may not attack for $1 seconds.");
        setUsage("/skill divineprotection");
        setArgumentRange(0, 0);
        setIdentifiers("skill invuln","skill divineprotection");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.FORCE, SkillType.BUFF, SkillType.SILENCABLE, SkillType.COUNTER);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set("reduction", 80);
        node.set(SkillSetting.APPLY_TEXT.node(), "%hero% has become invulnerable!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%hero% is once again vulnerable!");
        return node;
    }

    @Override
    public void init() {
        super.init();

    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);
        int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
        // Remove any harmful effects on the caster
        for (Effect effect : hero.getEffects()) {
            if (effect.isType(EffectType.HARMFUL)) {
                hero.removeEffect(effect);
            }
        }
        //hero.addEffect(new InvulnerabilityEffect(this, duration));
        hero.addEffect(new ExpirableEffect(this, "DivineProtection", duration));
        return SkillResult.NORMAL;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        if(event.getDamage()<=0) {
            return;
        }
        Hero hero = plugin.getCharacterManager().getHero((Player)event.getEntity());
        if(hero.hasEffect("DivineProtection")) {
            event.setDamage((int)Math.round(event.getDamage()*0.01*(100-SkillConfigManager.getUseSetting(hero, this, "reduction", 80, false))));
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onWepDmg(WeaponDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        Hero hero = plugin.getCharacterManager().getHero((Player)event.getEntity());
        if(hero.hasEffect("DivineProtection")) {
            event.setDamage((int)Math.round(event.getDamage()*0.01*(100-SkillConfigManager.getUseSetting(hero, this, "reduction", 80, false))));
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getDamage()<=0) {
            return;
        }
        Player attck;
        if(event.getDamager() instanceof Player) {
            attck=(Player)event.getDamager();
        }
        else if(event.getDamager() instanceof Arrow) {
            LivingEntity le=((Arrow)event.getDamager()).getShooter();
            if(le instanceof Player) {
                attck=(Player)le;
            }
            else {
                return;
            }
        }
        else {
            return;
        }
        
        Hero hero = plugin.getCharacterManager().getHero(attck);
        hero.checkInventory();
        if(hero.hasEffect("DivineProtection")) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public String getDescription(Hero hero) {
        int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 1, false);
        return getDescription().replace("$1", duration / 1000 + "");
    }
}