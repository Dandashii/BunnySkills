import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.meta.FireworkMeta;

public class SkillChainLightning extends TargettedSkill {

    public SkillChainLightning(Heroes plugin) {
        super(plugin, "ChainLightning");
        setDescription("You fire chain lightning at your target, bouncing to other targets");
        setUsage("/skill chainlightning");
        setArgumentRange(0, 0);
        setIdentifiers("skill chainlightning");
        setTypes(SkillType.BUFF);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), 30);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
        node.set("bounce-reduction", 5);
        node.set("bounces", 4);
        node.set("bounce-range", 5);
        return node;
    }

    @Override
    public void init() {
        super.init();
        setUseText("%hero% shoots out chain lightning!".replace("%hero%", "$1"));
    }
    
    public void chainLightning(Hero hero, LivingEntity le, List<LivingEntity> lle, final int range, final int c, final int maxc, final int damage, final int reduction) {
        lle.add(le);
        
        damageEntity(le, hero.getPlayer(), damage-(reduction*c), DamageCause.MAGIC,false);
        
        Location loc = le.getLocation();
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
        fwm.addEffects(effect);
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BURST).withFade(Color.BLACK).build();
        fwm.addEffects(effect2);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        ((CraftWorld)loc.getWorld()).getHandle().broadcastEntityEffect(
                ((CraftFirework)fw).getHandle(),(byte)17);
        fw.remove();
        
        //((CraftWorld)le.getWorld()).getHandle().addParticle("reddust", le.getLocation().getX()+0.5, le.getLocation().getY()+1, le.getLocation().getZ()+0.5, 0.0D, 0.0D, 0.0D);
        
        final Hero fhero = hero;
        
        LivingEntity target2=null;
        for(Entity e:le.getNearbyEntities(range, range, range)) {
            if(e instanceof LivingEntity) {
                if(!lle.contains((LivingEntity)e)) {
                    if(!hero.getEntity().equals((LivingEntity)e)) {
                        if (damageCheck(hero.getPlayer(), (LivingEntity)e)) {
                            target2=(LivingEntity)e;
                        }
                    }
                }
            }
        }
        if(target2==null) {
            return;
        }
        final LivingEntity fle = target2;
        final List<LivingEntity> flle = lle;
        
        if(c<maxc) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
            @Override
            public void run(){ chainLightning(fhero, fle, flle, range, c+1, maxc, damage, reduction); }},(long)(2));
        }
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
        
        int range = SkillConfigManager.getUseSetting(hero, this, "bounce-range", 5, false);
        int bounces = SkillConfigManager.getUseSetting(hero, this, "bounces", 4, false);
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 30, false);
        damage += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(this);
        int reduction = SkillConfigManager.getUseSetting(hero, this, "bounce-reduction", 5, false);
        
        chainLightning(hero, target, new ArrayList<LivingEntity>(), range, 0, bounces, damage, reduction);
        
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
