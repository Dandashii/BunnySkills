import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SkillRingOfFrost extends ActiveSkill implements Listener {

    public SkillRingOfFrost(Heroes plugin) {
        super(plugin, "RingOfFrost");
        setDescription("Places a ring of ice for $1s. Any enemy walking through the ring will be frozen for $2s.");
        setUsage("/skill ringoffrost");
        setArgumentRange(0, 0);
        setIdentifiers("skill ringoffrost");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        long durationfreeze = (long) (SkillConfigManager.getUseSetting(hero, this, "durationfreeze", 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "durationfreeze-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        durationfreeze = durationfreeze > 0 ? durationfreeze : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", durationfreeze + "");
        
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
        node.set("durationfreeze-increase", 0);
        node.set("durationfreeze", 2);
        node.set(SkillSetting.DAMAGE.node(), 0);
        node.set(SkillSetting.RADIUS.node(), 2);
        node.set("damage-increase", 0);
        return node;
    }
    private Map<Location, String> allringblocks = new HashMap<>();
    
    @Override
    public SkillResult use(Hero hero, String args[]) {
        Player player = hero.getPlayer();
        
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        
        int radius = (int) SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 2, false);
        
        if (duration > 0) {
            
            Map<Location,String> ringblocks = new HashMap<>();
            Location o = player.getLocation().getBlock().getLocation();
            
            for(int i=-5;i<=5;i++) {
                int x1=i;
                int z1=(int)Math.round(Math.sqrt(radius*radius-x1*x1));
                
                //System.out.println(x1+", "+z1);
                
                int x2=z1;
                int z2=x1;
                
                Location co = o.getBlock().getLocation();
                co.setX(co.getX()+x1);
                co.setZ(co.getZ()+z1);
                if(!ringblocks.containsKey(co)) {
                    ringblocks.put(new Location(co.getWorld(),co.getX(),co.getY(),co.getZ()),player.getName());
                    allringblocks.put(new Location(co.getWorld(),co.getX(),co.getY(),co.getZ()),player.getName());
                    ringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()+1,co.getZ()),player.getName());
                    allringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()+1,co.getZ()),player.getName());
                    ringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()-1,co.getZ()),player.getName());
                    allringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()-1,co.getZ()),player.getName());
                    ringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()-2,co.getZ()),player.getName());
                    allringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()-2,co.getZ()),player.getName());
                    ringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()+2,co.getZ()),player.getName());
                    allringblocks.put(new Location(co.getWorld(),co.getX(),co.getY()+2,co.getZ()),player.getName());
                }
                
                Location sco = o.getBlock().getLocation();
                sco.setX(sco.getX()+x2);
                sco.setZ(sco.getZ()+z2);
                if(!ringblocks.containsKey(sco)) {
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                }
                
                
                sco = o.getBlock().getLocation();
                sco.setX(sco.getX()-x2);
                sco.setZ(sco.getZ()+z2);
                if(!ringblocks.containsKey(sco)) {
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                }
                
                sco = o.getBlock().getLocation();
                sco.setX(sco.getX()+x1);
                sco.setZ(sco.getZ()-z1);
                if(!ringblocks.containsKey(sco)) {
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY(),sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-1,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()-2,sco.getZ()),player.getName());
                    ringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                    allringblocks.put(new Location(sco.getWorld(),sco.getX(),sco.getY()+2,sco.getZ()),player.getName());
                }
            }
            final Map<Location,String> iceblocks = ringblocks;
            
            /*
            for(Location lx:iceblocks.keySet()) {
                if (lx.getBlock().getType()!=Material.AIR) lx.getBlock().setType(Material.ICE);
                System.out.println(lx.getX()+" "+lx.getY()+" "+lx.getZ());
            }*/
                
            pulseEffect(duration, iceblocks);
            
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run(){
                for(Location ll:iceblocks.keySet()) {
                    for(Iterator<Map.Entry<Location, String>> it = allringblocks.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<Location, String> entry = it.next();
                        if(entry.getKey().equals(ll)) {
                            it.remove();
                        }
                    }
                }
            }},(long)(duration/50.0));
            
        }
        return SkillResult.NORMAL;
    }
    
    public void pulseEffect(final long duration, final Map<Location,String> iceblocks) {
        for(int i=500;i<duration;i+=500) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run(){
                    for(Location ll:iceblocks.keySet()) {
                        ll.getWorld().playEffect(ll,Effect.SMOKE,50);
                        //ll.getWorld().playEffect(ll, org.bukkit.Effect.POTION_BREAK, 23);
                    }
            }},(long)(i/50.0));
        }
        
        for(int i=0;i<duration;i+=2000) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                @Override
                public void run(){
                    for(Location ll:iceblocks.keySet()) {
                        ll.getWorld().playEffect(ll, org.bukkit.Effect.POTION_BREAK, 23);
                    }
            }},(long)(i/50.0));
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent evt) {
        if(!allringblocks.containsKey(evt.getTo().getBlock().getLocation())) {
            return;
        }
        if(allringblocks.containsKey(evt.getFrom().getBlock().getLocation())) {
            return;
        }
        
        //System.out.println("1 its included ;Size:" + allringblocks.size());
        if(evt.isCancelled()) {
            return;
        }
        if(!damageCheck(Bukkit.getPlayerExact(allringblocks.get(evt.getTo().getBlock().getLocation())),evt.getPlayer())) {
            return;
        }
        //System.out.println("2 damagecheck passed");
        
        Hero hero = plugin.getCharacterManager().getHero(Bukkit.getPlayerExact(allringblocks.get(evt.getTo().getBlock().getLocation())));
        Hero tHero = plugin.getCharacterManager().getHero(evt.getPlayer());
        
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, "durationfreeze", 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "durationfreeze-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        
        final Hero fHero = hero;
        final long fduration = duration;
        final Hero ftHero = tHero;
        final Skill rof = this;
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run(){
            if (fduration > 0) {
                
                ftHero.addEffect(new SlowEffect(rof, fduration, 5, false, null, null, fHero));
                ftHero.addEffect(new StunEffect(rof, fduration));
                //ftHero.addEffect(new ImpStunEffect(rof, fduration));
                ftHero.addEffect(new ExpirableEffect(rof,plugin,"frozen",fduration));
                
                //ftHero.addEffect(new FreezeEffect(rof, fduration));
                
                
                Location l1 = ftHero.getPlayer().getLocation().getBlock().getLocation();
                
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
                }},(long)(fduration/50.0));
            }
        }},(long)(4.0));
    }
    
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