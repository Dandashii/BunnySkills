import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillDeepfreeze extends TargettedSkill implements Listener{

    public SkillDeepfreeze(Heroes plugin) {
        super(plugin, "Deepfreeze");
        setDescription("Freezes the target in place for $1s.");
        setUsage("/skill deepfreeze");
        setArgumentRange(0, 0);
        setIdentifiers("skill deepfreeze");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "");
        
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
        node.set(SkillSetting.DURATION.node(), 5000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.DAMAGE.node(), 0);
        node.set("damage-increase", 0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (!(target instanceof Player)) {
            return SkillResult.INVALID_TARGET;
        }
        if (((Player) target).equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        Player tPlayer = (Player) target;
        if (!damageCheck(player, tPlayer)) {
            Messaging.send(player, "You can't freeze that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        Hero tHero = plugin.getCharacterManager().getHero(tPlayer);
        broadcastExecuteText(hero, target);
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        if (duration > 0) {
            tHero.addEffect(new SlowEffect(this, duration, 5, false, null, null, hero));
            tHero.addEffect(new StunEffect(this, duration));
            //tHero.addEffect(new ImpStunEffect(this, duration));
            
            tHero.addEffect(new ExpirableEffect(this,plugin,"frozen",duration));
            
            
            //tHero.addEffect(new FreezeEffect(this, duration));
            
            Location l1 = tHero.getPlayer().getLocation().getBlock().getLocation();
            
            l1.setY(l1.getBlockY());
            
            //l1=l1.add(2, 0, 2);
            
            Location l2 = l1.getBlock().getLocation();
            l2.setY(l2.getBlockY()+1);
            
            if(l1.getBlock().getType()==Material.AIR) {
                l1.getBlock().setType(Material.ICE);
            }
            if(l2.getBlock().getType()==Material.AIR) {
                l2.getBlock().setType(Material.ICE);
            }
            
            final Location fl1=l1,fl2=l2;
            
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run(){
                    if(fl1.getBlock().getType()==Material.ICE) {
                        fl1.getBlock().setType(Material.AIR);
                    }
                    if(fl2.getBlock().getType()==Material.ICE) {
                        fl2.getBlock().setType(Material.AIR);
                    }
            }},(long)(duration/50.0));
            
        }
        if (damage > 0) {
            damageEntity(tPlayer, player, damage, DamageCause.MAGIC);
            //tPlayer.damage(damage, player);
        }
        return SkillResult.NORMAL;
    }
    
    /*@EventHandler(priority=EventPriority.HIGHEST)
    public void onSkillUse(SkillUseEvent event) {
        if(event.getHero().hasEffect("frozen") || event.getHero().hasEffect("berserkerstun")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(plugin.getCharacterManager().getHero(event.getPlayer()).hasEffect("frozen") || plugin.getCharacterManager().getHero(event.getPlayer()).hasEffect("berserkerstun")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler (priority=EventPriority.HIGHEST)
    public void onHeroWepDamage(com.herocraftonline.heroes.api.events.WeaponDamageEvent event) {
        if(event.getDamager().hasEffect("frozen") || event.getDamager().hasEffect("berserkerstun")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event)
    {
       // if(Math.abs(event.getFrom().getX()-event.getTo().getX())<0.01) {
         //   if(Math.abs(event.getFrom().getZ()-event.getTo().getZ())<0.01) {
          //      if(Math.abs(event.getFrom().getY()-event.getTo().getY())<0.01) {
           //         return;
          //      }
         //   }
        //}
        
        if(plugin.getCharacterManager().getHero(event.getPlayer()).hasEffect("frozen") || plugin.getCharacterManager().getHero(event.getPlayer()).hasEffect("berserkerstun")) {
            //event.setCancelled(true);
            event.setTo(event.getFrom());
        }
    }*/
    
    
    /*
    public class ImpStunEffect extends StunEffect {
        public ImpStunEffect(Skill skill,Long duration) {
            super(skill, duration);
            
            int tickDuration = (int) (duration / 1000) * 20;
            addMobEffect(2, tickDuration, 4, false);
            addMobEffect(8, tickDuration, -4, false);
        }
    }*/
    /*
    public class FreezeEffect extends PeriodicExpirableEffect
    {
        public FreezeEffect(Skill skill, long duration)
        {
            super(skill, "Frozen", 100L, duration);
            types.add(EffectType.STUN);
            types.add(EffectType.HARMFUL);
            types.add(EffectType.PHYSICAL);
            types.add(EffectType.DISABLE);
            addMobEffect(9, (int)(duration / 1000L) * 20, 127, false);
            addMobEffect(2, (int)(duration / 1000L) * 20, 4, false);
            addMobEffect(8, (int)(duration / 1000L) * 20, -4, false);
        }

        @Override
        public void applyToHero(Hero hero)
        {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            loc = hero.getPlayer().getLocation();
            broadcast(player.getLocation(), "$1 is frozen!", new Object[] {
                player.getDisplayName()
            });
        }

        @Override
        public void removeFromHero(Hero hero)
        {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), "$1 is no longer frozen!", new Object[] {
                player.getDisplayName()
            });
        }

        @Override
        public void tickHero(Hero hero)
        {
            Location location = hero.getPlayer().getLocation();
            if(location == null) {
                return;
            }
            if(location.getX() != loc.getX() || location.getY() != loc.getY() || location.getZ() != loc.getZ())
            {
                loc.setYaw(location.getYaw());
                loc.setPitch(location.getPitch());
                hero.getPlayer().teleport(loc);
            }
        }
        
        

        @Override
        public void tickMonster(Monster monster1)
        {
        }

        private static final long period = 100L;
        private final String stunApplyText = "$1 is frozen!";
        private final String stunExpireText = "$1 is no longer frozen!";
        private Location loc;
    }*/
    
}