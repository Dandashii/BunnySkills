import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySkeleton;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class SkillRaisezombie extends ActiveSkill implements Listener {
    public SkillRaisezombie(Heroes plugin) {
        super(plugin, "Raisezombie");
        setDescription("$1% chance to spawn 1 Zombie, $2% for 2, and $3% for 3.");
        setUsage("/skill Raisezombie");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill Raisezombie"});
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int chance2x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-2x-per-level", 0.0, false) * hero.getSkillLevel(this));
        int chance3x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-3x-per-level", 0.0, false) * hero.getSkillLevel(this));
        String description = getDescription().replace("$1", (100 - (chance2x + chance3x)) + "").replace("$2", chance2x + "").replace("$3", chance3x + "");
        
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
        node.set("max-per-spawn", 2);
        node.set("spawn-chance", 0.12);
        
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        cc.put(player.getName(),0);
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, 20).getRelative(
                        BlockFace.UP);
        
        if (hero.getPlayer().getFoodLevel()>15) {
            hero.getPlayer().setFoodLevel(15);
        }
        
        spawnZombie(wTargetBlock.getLocation(), player,5);
        
        return SkillResult.NORMAL;
    }
    
    
    public void spawnZombie(Location loc, Player player, int speed) {
        
        int max = SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero(player), this, "max-per-spawn", 2, false);
        
        if(cc.get(player.getName())>max) {
            return;
        }
        else {
            cc.put(player.getName(), cc.get(player.getName())+1);
        }
        
        LivingEntity le = player.getWorld().spawnCreature(loc,
                CreatureType.ZOMBIE);
        le.getWorld().playEffect(le.getLocation(), Effect.SMOKE, 3);
        lel.put(le.getEntityId(),player.getName());
        le.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 15000, speed));
        
        final LivingEntity fle = le;
        
        final Player fplayer = player;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(fle!=null && !fle.isDead()) {
                    summonSkele(fle.getLocation(),fplayer.getName());
                    fle.remove();
                    lel.remove(fle.getEntityId());
                }
                
                }
            }, (15*20));
    }
    
    //static Map<LivingEntity,String> lel = new HashMap<>();
    static Map<Integer,String> lel = new HashMap<>();
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEDeath(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity le = (LivingEntity)event.getEntity();
        if(!lel.containsKey(le.getEntityId())) {
            return;
        }
        if(event.getDamage()>=le.getHealth()) {
            event.setCancelled(true);
            event.setDamage(0);
            if (le.getType() == EntityType.ZOMBIE) {
                summonSkele(le.getLocation(),lel.get(le.getEntityId()));
            }
            lel.remove(le.getEntityId());
            le.remove();
        }
    }
    
    
    public void summonSkele(Location loc, String player) {
        // ghast sound
        /*if(cc.get(player) >0) {
            cc.put(player, cc.get(player)-1);
        }*/
        
        LivingEntity le1;
        le1 = loc.getWorld().spawnCreature(loc,
                    CreatureType.SKELETON);
        le1.getWorld().playEffect(le1.getLocation(), Effect.SMOKE, 3);
        
        
        //((CraftSkeleton)le1).getHandle().getEquipment()[0]=new net.minecraft.server.ItemStack(net.minecraft.server.Item.BOW);
        changeIntoNormal((Skeleton)le1,false);
        
        lel.put(le1.getEntityId(),player);
        le1.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 15000, 1));
        
        final LivingEntity fle = le1;
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(fle!=null) {
                    fle.remove();
                    lel.remove(fle.getEntityId());
                }
                
                }
            }, (15*20));
    }
    
    
    public void changeIntoNormal(Skeleton skeleton, boolean giveRandomEnchantments){
        EntitySkeleton ent = ((CraftSkeleton)skeleton).getHandle();
        try {
            Method be = EntitySkeleton.class.getDeclaredMethod("bE");
            be.setAccessible(true);
            be.invoke(ent);
            if (giveRandomEnchantments){
                Method bf = EntityLiving.class.getDeclaredMethod("bF");
                bf.setAccessible(true);
                bf.invoke(ent);
            }
            /*Field selector = EntitySkeleton.class.getDeclaredField("goalSelector");
            selector.setAccessible(true);
            Field d = EntitySkeleton.class.getDeclaredField("d");
            d.setAccessible(true);
            PathfinderGoalSelector goals = (PathfinderGoalSelector) selector.get(ent);
            goals.a(4, (PathfinderGoal) d.get(ent));*/
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }
    }
    
    private Map<String,Integer> cc=new HashMap<>();
    
    @EventHandler
    public void onEdby(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if(!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        if(!lel.containsKey(event.getDamager().getEntityId())) {
            return;
        }
        if(event.isCancelled()) {
            return;
        }
        
        int cz=cc.get(lel.get(event.getDamager().getEntityId()));
        cz++;
        
        double chance = SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero(Bukkit.getPlayerExact(lel.get(event.getDamager().getEntityId()))), this, "spawn-chance", 0.12, false);
        
        if(Math.random()>(chance/(cz*1.0))) {
            spawnZombie(event.getEntity().getLocation(),Bukkit.getPlayerExact(lel.get(event.getDamager().getEntityId())),1);
        }
    }
    
    	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTarget(EntityTargetEvent event) {
            
            if(event.getEntity() instanceof LivingEntity) {
                if(!lel.containsKey(event.getEntity().getEntityId())) {
                    return;
                }
            }
            else {
                return;
            }
            
            if(event.getTarget() instanceof LivingEntity) {
                if (lel.containsKey(event.getTarget().getEntityId())) {
                    if (lel.get(event.getTarget().getEntityId()).equals(lel.get(event.getEntity().getEntityId()))) {
                        event.setCancelled(true);
                    }
                }
            }
            
            
            if (!event.isCancelled()) {
                if (event.getTarget() instanceof Player) {
                    Player player = (Player) event.getTarget();
                    if (!damageCheck(Bukkit.getPlayerExact(lel.get(event.getEntity().getEntityId())),player)) {
                        if(((LivingEntity)event.getEntity()).getType()==EntityType.ZOMBIE || ((LivingEntity)event.getEntity()).getType()==EntityType.SKELETON) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
	}

}