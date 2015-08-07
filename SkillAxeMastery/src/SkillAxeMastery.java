import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SkillAxeMastery extends PassiveSkill implements Listener {
    
    public SkillAxeMastery(Heroes plugin) {
        super(plugin, "AxeMastery");
        setDescription("Mastery of axe throwing");
        setTypes(SkillType.UNBINDABLE);
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    static List<ItemStack> islist = new ArrayList<>();
    static List<Item> ilist = new ArrayList<>();
    
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event)
    {
        ItemStack is = event.getItem().getItemStack();
        Item i = event.getItem();
        
        if(islist.contains(is) || ilist.contains(i))
        {
            //System.out.println("denied pickup");
            event.setCancelled(true);
            event.getPlayer().getWorld().playEffect(event.getPlayer().getLocation(), Effect.BLAZE_SHOOT, 10);
        }
    }
    
    /*
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        try {
            String[] split = event.getMessage().split(" ");
            if (split.length < 1) { return;}

            String cmp="";
            for(int i=0;i<split.length;i++) {
                cmp+=split[i].trim()+" ";
            }
            cmp=cmp.trim();
            
            
            if(cmp.equalsIgnoreCase("/bzi")) {
                Heroes he = (Heroes)Bukkit.getPluginManager().getPlugin("Heroes");
                he.getCharacterManager().getHero(player).clearEffects();
                
                player.sendMessage("bzi clear effects");
                event.setCancelled(true);
            }
            
        }
        catch(Exception e)
        {System.out.println("axemastery CAUGHT ERROR (107): "+e.getMessage()+" Stack:");}
    }*/
    
    
    
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
        
        node.set("axe-cooldown", 600);
        node.set("axe-damage-wood", 1);
        node.set("axe-damage-stone", 2);
        node.set("axe-damage-iron", 3);
        node.set("axe-damage-gold", 3);
        node.set("axe-damage-diamond", 4);
        //node.set("axe-health-per-hit", 0);
        node.set("axe-range", 30);
        
        node.set("berserk-axe-cooldown", 300);
        node.set("berserk-axe-damage-bonus", 1);
        node.set("berserk-melee-axe-damage-bonus", 1);
        node.set("berserk-axe-range", 30);
        //node.set("berserk-axe-health-per-hit", 0);
        
        node.set("berserk-axe-fire", 1);
        //node.set("berserk-axe-ignite", 0);
        
        node.set("stun-duration", 3000);
        node.set("stun-chance", 15);
        node.set("stun-cooldown", 15000);
        node.set("stun-damage", 2);
        // stun heal?
        
        node.set("slow-duration", 8000);
        node.set("slow-amplifier", 2);
        
        node.set("non-fire-dmg-reduce", 20);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        
        Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
        
        if(!hero.getHeroClass().getName().equalsIgnoreCase("berserker")) {
            return;
        }
        
        if( event.getAction().equals(Action.LEFT_CLICK_BLOCK) || 
            event.getAction().equals(Action.LEFT_CLICK_AIR)) {
            
            if(event.getItem()==null) {
                    return;
                }
            if(event.getItem().getType()==null) {
                    return;
                }
            if(event.getItem().getType()==Material.AIR) {
                    return;
                }
            if(!event.hasItem()) {
                    return;
                }
            
            
            Material weapon;
            
            if(event.getItem().getType()!=Material.WOOD_AXE &&
               event.getItem().getType()!=Material.STONE_AXE &&
               event.getItem().getType()!=Material.IRON_AXE &&
               event.getItem().getType()!=Material.GOLD_AXE &&
               event.getItem().getType()!=Material.DIAMOND_AXE) {return;}
            else {weapon=event.getItem().getType();}
            
            if(!hero.hasEffect("stun-axes") && hero.getHeroClass().getName().equalsIgnoreCase("berserker") && (hero.getCooldown("AxeMastery") == null || hero.getCooldown("AxeMastery") <= System.currentTimeMillis())) {
                if(hero.hasEffectType(EffectType.STUN)) {
                    return;
                }
                
                
                long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, "axe-cooldown", 600, false));
                        cooldown = cooldown > 0 ? cooldown : 0;
                long bcooldown = (long) (SkillConfigManager.getUseSetting(hero, this, "berserk-axe-cooldown", 300, false));
                        bcooldown = bcooldown > 0 ? bcooldown : 0;
                
                if(hero.hasEffect("berserk")) {
                    hero.setCooldown("AxeMastery", bcooldown + System.currentTimeMillis());
                }
                else {
                    hero.setCooldown("AxeMastery", cooldown + System.currentTimeMillis());
                }
                
                int damage=0;
                if(weapon==Material.WOOD_AXE) {
                    damage = SkillConfigManager.getUseSetting(hero, this, "axe-damage-wood", 1, false);
                }
                if(weapon==Material.STONE_AXE) {
                    damage = SkillConfigManager.getUseSetting(hero, this, "axe-damage-stone", 1, false);
                }
                if(weapon==Material.IRON_AXE) {
                    damage = SkillConfigManager.getUseSetting(hero, this, "axe-damage-iron", 1, false);
                }
                if(weapon==Material.GOLD_AXE) {
                    damage = SkillConfigManager.getUseSetting(hero, this, "axe-damage-gold", 1, false);
                }
                if(weapon==Material.DIAMOND_AXE) {
                    damage = SkillConfigManager.getUseSetting(hero, this, "axe-damage-diamond", 1, false);
                }
                
                if(hero.hasEffect("berserk")) {
                    damage+=SkillConfigManager.getUseSetting(hero, this, "berserk-axe-damage-bonus", 1, false);
                }
                
                
                int distance;
                if(hero.hasEffect("berserk")) {
                    distance = SkillConfigManager.getUseSetting(hero, this, "berserk-axe-range", 30, false);
                }
                else {
                    distance = SkillConfigManager.getUseSetting(hero, this, "axe-range", 30, false);
                }
                
                Vector dir = hero.getPlayer().getLocation().getDirection();
                dir = dir.normalize();
                Location next = hero.getPlayer().getLocation();
                next.setY(next.getY()+1.5);
                double mul = 2;

                ItemStack it = new org.bukkit.inventory.ItemStack(weapon);
                Item i = next.getWorld().dropItem(next, it);
                
                int fire = (int) (SkillConfigManager.getUseSetting(hero, this, "berserk-axe-fire", 1, false));
                int nofire = (int) (SkillConfigManager.getUseSetting(hero, this, "non-fire-dmg-reduce", 20, false));
                /*if(hero.hasEffect("berserk")) {
                    if(fire==1) {
                        i.setFireTicks(60);
                    }
                }*/
                if(hero.getCooldown("berserker-stun-cd") == null || hero.getCooldown("berserker-stun-cd") <= System.currentTimeMillis()) {
                    damage-=nofire;
                    if(damage<0) {
                        damage=0;
                    }
                }
                else {
                    if(fire==1) {
                        i.setFireTicks(60);
                    }
                }
                
                ilist.add(i);
                islist.add(it);
                hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this,"axethrowing"));
                
                
                //run recursive method
                forward(next,hero,dir,mul,damage,distance, i, 0);
            }
            
        }
    }
    
    public void forward(Location next, final Hero hero, final Vector dir, final double mul, final int damage, final double distance, Item i, final int c) {
        if(!hero.getPlayer().isOnline() || hero.getPlayer().isDead()) {
                    i.remove();
                    ilist.remove(i);
                    islist.remove(i.getItemStack());
            return;
        }
        
        Vector idir = dir;
        double sy = idir.getY();
                i.setVelocity(idir.multiply(1.5).setY(sy+0.0));
                
                List<LivingEntity> targets = new ArrayList<>();
                
                next = next.add(dir.getX()*mul, dir.getY()*mul, dir.getZ()*mul);
                //next = i.getLocation();
                
                //next.getWorld().playEffect(next, Effect.POTION_BREAK, 612);

                for(LivingEntity e:getNearbyLivingEntities(next, 2.9)) {
                    if(damageCheck(hero.getPlayer(),e)) {
                        //addSpellTarget(e,hero);
                        // auto attack spell, not spell damage
                        targets.add(e);
                    }
                }

                for(LivingEntity e:targets) {
                    
                    
                    if(e instanceof Player) {
                        if(damageCheck(hero.getPlayer(), (Player)e)) {
// ---------------------------------------------------------------------------------------------------------------------------------------------
                            Hero tHero = plugin.getCharacterManager().getHero((Player)e);
                            
                            if(hero.hasEffect("slowing-axes")) {
                                long duration = SkillConfigManager.getUseSetting(hero, this, "slow-duration", 8000, false);
                                int amplifier = SkillConfigManager.getUseSetting(hero, this, "slow-amplifier", 2, false);
                                SlowEffect slowAxeEffect = new SlowEffect(this, duration, amplifier, false, "", "", hero);
                                tHero.addEffect(slowAxeEffect);
                                tHero.getPlayer().getWorld().playEffect(tHero.getPlayer().getLocation(), org.bukkit.Effect.POTION_BREAK, 23);
                                hero.removeEffect(hero.getEffect("slowing-axes"));
                            }
                            
                            //addSpellTarget((Player)e, hero);
                            // WEAPON event not skill event, we want this to be auto attack not spell
                            
                            //damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC, false);
                            damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.PROJECTILE, false);
//----------------------------------------------------------------------------------------------------------------------------------------------
                        }
                        
                        //e.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    }
                    if(e instanceof Creature) {
                        if(damageCheck(hero.getPlayer(), (Creature)e)) {
                            //addSpellTarget((Creature)e, hero);
                            // autoattack damage, not spell
                            
                            //damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                            damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.PROJECTILE);
                        }
                        //next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    }
                }
                
                
                final Item fi = i;
            if(next.getWorld().equals(hero.getPlayer().getWorld())) {
                //if(next.distance(hero.getPlayer().getLocation().getBlock().getLocation())<distance) {
                    if(c<20) {
                        final Location nxt = next;
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
                            @Override
                            public void run(){ 
                                Vector vdi = hero.getPlayer().getLocation().getDirection().normalize();
                                
                                //for(int i=0;i<c;i++) {
                                    vdi.multiply(1.5);
                                //}
                                vdi.setY(dir.getY());
                                vdi = vdi.setY(vdi.getY()-0.12);
                                
                                forward(nxt,hero,vdi,mul,damage,distance, fi, c+1); 
                                //forward(nxt,hero,dir,mul,damage,distance, fi, c+1);
                            }},(long)(2));
                    }
                    else {
                        i.remove();
                        ilist.remove(i);
                        islist.remove(i.getItemStack());
                        hero.removeEffect(hero.getEffect("axethrowing"));
                    }
            }
            else {
                i.remove();
                ilist.remove(i);
                islist.remove(i.getItemStack());
                hero.removeEffect(hero.getEffect("axethrowing"));
            }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        
        Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
        
        if(!hero.getHeroClass().getName().equalsIgnoreCase("berserker")) {
            return;
        }
        
        Location loc = event.getFrom();
        loc.setY(loc.getY()-0.4);
        
        if(hero.hasEffect("berserk")) {
            event.getPlayer().getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES,50);
        }
    }
    
    @EventHandler //(priority=EventPriority.HIGHEST)
    public void onHeroWepDamage(com.herocraftonline.heroes.api.events.WeaponDamageEvent event) {
        
        if(event.isCancelled() || !(event.getEntity() instanceof LivingEntity) || event.getDamage()==0) {
            return;
        }
        
        if(!(event.getDamager().getEntity() instanceof Player)) {
            return;
        }
        
        Hero hero = plugin.getCharacterManager().getHero((Player)event.getDamager().getEntity());
        
        if(!hero.hasEffect("AxeMastery")) {
            return;
        }
        
        CharacterTemplate tChar = plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
        if(tChar.getEntity().getNoDamageTicks() > tChar.getEntity().getMaximumNoDamageTicks()/2.0) {
            return;
        }
        
        if(hero.getPlayer().getItemInHand().getType()!=Material.WOOD_AXE &&
           hero.getPlayer().getItemInHand().getType()!=Material.STONE_AXE &&
           hero.getPlayer().getItemInHand().getType()!=Material.IRON_AXE &&
           hero.getPlayer().getItemInHand().getType()!=Material.GOLD_AXE &&
           hero.getPlayer().getItemInHand().getType()!=Material.DIAMOND_AXE) {return;}
        
        
        if(hero.hasEffect("stun-axes")) {
            if (hero.getCooldown("berserker-stun-cd") == null || hero.getCooldown("berserker-stun-cd") <= System.currentTimeMillis()) {
                if(SkillConfigManager.getUseSetting(hero, this, "stun-chance", 10, false)<=(Math.random()*100)) {
                    
                    long stuncd = SkillConfigManager.getUseSetting(hero, this, "stun-cooldown", 15000, false);
                    long duration = SkillConfigManager.getUseSetting(hero, this, "stun-duration", 3000, false);
                    int damage = SkillConfigManager.getUseSetting(hero, this, "stun-damage", 2, false);
                    
                    hero.setCooldown("berserker-stun-cd", stuncd + System.currentTimeMillis());
                    
                    tChar.addEffect(new SlowEffect(this, duration, 5, false, null, null, hero));
                    tChar.addEffect(new StunEffect(this, duration));
                    
                    if(hero.hasEffect("berserk")) {
                        damage+=SkillConfigManager.getUseSetting(hero, this, "berserk-melee-axe-damage-bonus", 1, false);
                    }
                    
                    addSpellTarget(event.getEntity(), hero);
                     
                    damageEntity(tChar.getEntity(), hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC, false);
                    tChar.getEntity().getWorld().playEffect(tChar.getEntity().getLocation(), Effect.POTION_BREAK, 245); 
                }
            }
            else {
                event.setDamage(0);
            }
        }
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
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<LivingEntity> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if(e.getLocation().getWorld().equals(l.getWorld())) {
                            if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                                if(e instanceof LivingEntity) {
                                    radiusEntities.add((LivingEntity)e);
                                }
                            }
                        }
                    }
                }
            }
        return radiusEntities;
    }
    
}