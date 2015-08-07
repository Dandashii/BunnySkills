import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.meta.FireworkMeta;

public class SkillJolt extends TargettedSkill {

    public SkillJolt(Heroes plugin) {
        super(plugin, "Jolt");
        setDescription("You shock your target, dealing $1 damage.");
        setUsage("/skill jolt");
        setArgumentRange(0, 0);
        setIdentifiers("skill jolt");
        setTypes(SkillType.BUFF);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), 30);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        return node;
    }

    @Override
    public void init() {
        super.init();
        setUseText("%hero% casts Jolt!".replace("%hero%", "$1"));
    }
    
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if(target==null) {
            return SkillResult.INVALID_TARGET;
        }
        if(target instanceof Player) {
            if(((Player)target).equals(hero.getPlayer())) {
                return SkillResult.INVALID_TARGET;
            }
        }
        if(!damageCheck(hero.getPlayer(), target)) {
            return SkillResult.INVALID_TARGET;
        }
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 30, false);
        damage += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(this);
        
        damageEntity(target, hero.getPlayer(), damage, DamageCause.MAGIC, false);
        
        Location loc = target.getLocation();
        
        
        //target.getWorld().strikeLightningEffect(loc);
        
        //((CraftPlayer)hero.getPlayer()).getHandle().playerConnection.sendPacket(p1);
        
        fw(hero.getPlayer().getLocation());
        fw(loc);
        
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    public void fw(final Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.STAR).withFade(Color.BLACK).build();
        fwm.addEffects(effect);
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.STAR).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
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
