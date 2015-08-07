import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterDamageManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.left2craft.combatbase.CombatBase;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.server.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class SkillEnchantedArrows extends PassiveSkill {

    public SkillEnchantedArrows(Heroes plugin) {
        super(plugin, "EnchantedArrows");
        setDescription("Your successful bow shots give you mana. You have reduced damage in close combat and summon wolves when you shoot enemies");
        setTypes(SkillType.UNBINDABLE, SkillType.BUFF);
        setEffectTypes(new EffectType[] { EffectType.BENEFICIAL, EffectType.PHYSICAL });
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        
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
        
        //node.set("melee-reduction", 50.0);
        node.set("melee-range", 3.0);
        node.set("mana-increase", 3);
        node.set("wolf-hp", 300);
        node.set("arrow-power", 1.0);
        node.set("attack-speed", 1500);
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    private static List<Material> clickableblocks = Arrays.asList(
        Material.ANVIL,
        Material.WORKBENCH,
        Material.DISPENSER,
        Material.BEACON,
        Material.HOPPER,
        Material.HOPPER_MINECART,
        Material.NOTE_BLOCK,
        Material.BED,
        Material.FURNACE,
        Material.ENCHANTMENT_TABLE,
        Material.TRAPPED_CHEST,
        Material.TRAP_DOOR,
        Material.DIODE,
        Material.DIODE_BLOCK_OFF,
        Material.DIODE_BLOCK_ON,
        Material.REDSTONE_COMPARATOR,
        Material.REDSTONE_COMPARATOR_OFF,
        Material.REDSTONE_COMPARATOR_ON,
        Material.FENCE_GATE,
        Material.WOODEN_DOOR,
        Material.WOOD_BUTTON,
        Material.WOOD_DOOR,
        Material.IRON_DOOR,
        Material.IRON_DOOR_BLOCK,
        Material.CAULDRON,
        Material.ENDER_PORTAL_FRAME,
        Material.ENDER_CHEST,
        Material.BED_BLOCK,
        Material.DROPPER,
        Material.LEVER,
        Material.CAKE,
        Material.CAKE_BLOCK,
        Material.BREWING_STAND,
        Material.COMMAND,
        Material.BURNING_FURNACE,
        Material.STORAGE_MINECART,
        Material.CHEST
    );
    
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler(priority=EventPriority.MONITOR)
        public void PlayerUseClassicBow(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("EnchantedArrows")) {
                return;
            }
            
            if ( ((event.getAction() == Action.RIGHT_CLICK_AIR) || (event.getAction() == Action.RIGHT_CLICK_BLOCK)) && 
            (player.getItemInHand().getType() == Material.BOW) /*&& (player.getInventory().contains(Material.ARROW))*/) {
                if ((event.getClickedBlock() != null) && (clickableblocks.contains(event.getClickedBlock().getType()))) {
                  return;
                }
                
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
                //if(hero.getCooldown("EnchInstantArrows")!=null) {
                //    if(hero.getCooldown("EnchInstantArrows")>System.currentTimeMillis()) {
                //        return;
                //    }
                //}
                //long cooldown = (long)SkillConfigManager.getUseSetting(hero, skill, "attack-speed", 1500, false);
                //cooldown=Math.round(cooldown/(1+CombatBase.plugin.getStatHandler(player).getStat("attackspeed")/100.0));
                //hero.setCooldown("EnchInstantArrows", System.currentTimeMillis()+cooldown);
                
                if(!CombatBase.plugin.getStatHandler(player).isAttackReady()) {
                    return;
                }
                
                long cooldown = (long)SkillConfigManager.getUseSetting(hero, skill, "attack-speed", 1500, false);
                
                CombatBase.plugin.getStatHandler(player).setAttackCooldown(cooldown);
                
                
                
                
                
                
                //player.getItemInHand().setDurability((short)0);
                
                
                Arrow aro = (Arrow)player.launchProjectile(Arrow.class);
                aro.setShooter(player);
                
                double powerSetting = (double)SkillConfigManager.getUseSetting(hero, skill, "arrow-power", 1.0, false);
                aro.setVelocity(aro.getVelocity().multiply(powerSetting));
                
                org.bukkit.event.entity.EntityShootBowEvent shootEvent = new org.bukkit.event.entity.EntityShootBowEvent(player, event.getItem(), aro, 1);
                Bukkit.getPluginManager().callEvent(shootEvent);
                
                //if(shootEvent.isCancelled()) {
                //    aro.remove();
                //    return;
                //}
                
                EntityArrow arro = ((CraftArrow)aro).getHandle();
                Map<Enchantment, Integer> aroEnchs = player.getItemInHand().getEnchantments();

                if ((aroEnchs.containsKey(Enchantment.ARROW_DAMAGE)) && (((Integer)aroEnchs.get(Enchantment.ARROW_DAMAGE)).intValue() > 0)) {
                  arro.b(arro.c() + ((Integer)aroEnchs.get(Enchantment.ARROW_DAMAGE)).intValue() * 0.5D + 0.5D);
                }
                if ((aroEnchs.containsKey(Enchantment.ARROW_KNOCKBACK)) && (((Integer)aroEnchs.get(Enchantment.ARROW_KNOCKBACK)).intValue() > 0)) {
                  arro.a(((Integer)aroEnchs.get(Enchantment.ARROW_KNOCKBACK)).intValue());
                }
                if ((aroEnchs.containsKey(Enchantment.KNOCKBACK)) && (((Integer)aroEnchs.get(Enchantment.KNOCKBACK)).intValue() > 0)) {
                  arro.a(((Integer)aroEnchs.get(Enchantment.KNOCKBACK)).intValue());
                }
                if (((aroEnchs.containsKey(Enchantment.ARROW_FIRE)) && (((Integer)aroEnchs.get(Enchantment.ARROW_FIRE)).intValue() > 0)) || (
                  (aroEnchs.containsKey(Enchantment.FIRE_ASPECT)) && (((Integer)aroEnchs.get(Enchantment.FIRE_ASPECT)).intValue() > 0))) {
                  arro.setOnFire(100);
                }

                //int fStack = player.getInventory().first(Material.ARROW);
                //ItemStack its = player.getInventory().getItem(fStack);
                //if ((aroEnchs.containsKey(Enchantment.ARROW_INFINITE)) || (player.getGameMode() == GameMode.CREATIVE)) {
                  aro.setMetadata("infi_arrow", new FixedMetadataValue(plugin, Boolean.valueOf(true)));
                  
                  //int adamage = (int)Math.round(hero.getHeroClass().getProjectileDamage(CharacterDamageManager.ProjectileType.ARROW)+hero.getLevel()*hero.getHeroClass().getProjDamageLevel(CharacterDamageManager.ProjectileType.ARROW));
                  
                  //aro.setMetadata("datdamage", new FixedMetadataValue(plugin, Integer.valueOf(adamage)));
                  
                //} else {
                  //if (its.getAmount() > 1) {
                  //      its.setAmount(its.getAmount() - 1);
                  //  }
                  //else if (its.getAmount() <= 1) {
                  //  player.getInventory().clear(fStack);
                  //}
                  //aro.setMetadata("arrow_norm", new FixedMetadataValue(plugin, Boolean.valueOf(true)));
                //}
                  
            }

        }
        
        @EventHandler
        public void ArrowPickup(PlayerPickupItemEvent e) {
            if (e.getItem().hasMetadata("infi_arrow")) {
                e.setCancelled(true);
                e.getItem().remove();
            }
        }
        
        
        @EventHandler(priority=EventPriority.MONITOR)
        public void onHeroBow(com.herocraftonline.heroes.api.events.WeaponDamageEvent event) {
            if(!event.isProjectile()) {
                return;
            }
            if(event.getAttackerEntity() instanceof Arrow) {
                Arrow ar = (Arrow)event.getAttackerEntity();
                /*if (ar.hasMetadata("datdamage")) {
                    if(ar.getShooter() instanceof Player) {
                        if(((Player)ar.getShooter()).getName().equalsIgnoreCase("firebunny36") || ((Player)ar.getShooter()).getName().equalsIgnoreCase("keimu")) {
                            ((Player)ar.getShooter()).sendMessage("a1::"+event.getDamage());
                        }
                    }
                    ar.setDamage(ar.getMetadata("datdamage").get(0).asInt());
                    if(ar.getShooter() instanceof Player) {
                        if(((Player)ar.getShooter()).getName().equalsIgnoreCase("firebunny36") || ((Player)ar.getShooter()).getName().equalsIgnoreCase("keimu")) {
                            ((Player)ar.getShooter()).sendMessage("a2::"+event.getDamage());
                        }
                    }
                    if(event.getEntity() instanceof LivingEntity) {
                        ((LivingEntity)event.getEntity()).setNoDamageTicks(0);
                    }
                }*/
                if(ar.getShooter() instanceof Player) {
                    Hero hero = plugin.getCharacterManager().getHero((Player)ar.getShooter());
                    if (!hero.hasEffect("EnchantedArrows")) {
                        return;
                    }
                    int adamage = (int)Math.round(hero.getHeroClass().getProjectileDamage(CharacterDamageManager.ProjectileType.ARROW)+hero.getLevel()*hero.getHeroClass().getProjDamageLevel(CharacterDamageManager.ProjectileType.ARROW));
                    event.setDamage(adamage);
                }
            }
        }
        
        // Set arrow damage based on meta damage
        @EventHandler(priority=EventPriority.HIGHEST)
        public void onEDBEarrow(EntityDamageByEntityEvent e) {
            if(e.getDamager() instanceof Arrow) {
                Arrow ar = (Arrow)e.getDamager();
                /*if (ar.hasMetadata("datdamage")) {
                    if(ar.getShooter() instanceof Player) {
                        if(((Player)ar.getShooter()).getName().equalsIgnoreCase("firebunny36") || ((Player)ar.getShooter()).getName().equalsIgnoreCase("keimu")) {
                            ((Player)ar.getShooter()).sendMessage("b1::"+e.getDamage());
                        }
                    }
                    ar.setDamage(ar.getMetadata("datdamage").get(0).asInt());
                    if(ar.getShooter() instanceof Player) {
                        if(((Player)ar.getShooter()).getName().equalsIgnoreCase("firebunny36") || ((Player)ar.getShooter()).getName().equalsIgnoreCase("keimu")) {
                            ((Player)ar.getShooter()).sendMessage("b2::"+e.getDamage());
                        }
                    }
                }*/
                if(ar.getShooter() instanceof Player) {
                    Hero hero = plugin.getCharacterManager().getHero((Player)ar.getShooter());
                    if (!hero.hasEffect("EnchantedArrows")) {
                        return;
                    }
                    int adamage = (int)Math.round(hero.getHeroClass().getProjectileDamage(CharacterDamageManager.ProjectileType.ARROW)+hero.getLevel()*hero.getHeroClass().getProjDamageLevel(CharacterDamageManager.ProjectileType.ARROW));
                    e.setDamage(adamage);
                }
            }
        }

        @EventHandler (priority = EventPriority.MONITOR)
        public void onTarget(EntityTargetEvent event) {
            if(!(event.getEntity() instanceof Wolf)) {
                return;
            }
            if(!(event.getTarget() instanceof Player)) {
                return;
            }
            if(((Wolf)event.getEntity()).getOwner()==null) {
                return;
            }
            if( ((Wolf)event.getEntity()).getOwner().getName().equalsIgnoreCase(((Player)event.getTarget()).getName()) ) {
                event.setCancelled(true);
            }
        }
        
        @EventHandler(priority=EventPriority.MONITOR)
        public void onEntityDamage(EntityDamageEvent event) {
            
            if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity) || !(event instanceof EntityDamageByEntityEvent)) {
                return;
            }
            
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            
            try{
            if(edby.getEntity() instanceof Wolf) {
                Wolf w = (Wolf)edby.getEntity();
                if(w.getOwner()!=null) {
                    if(edby.getDamager() instanceof Arrow) {
                        if(w.getOwner().equals(((Arrow)edby.getDamager()).getShooter())) {
                            event.setCancelled(true);
                            edby.getDamager().remove();
                            return;
                        }
                    }
                }
            }
            }catch(Exception we){}
            
            if (!(edby.getDamager() instanceof Arrow)) {
                return;
            }

            Arrow arrow = (Arrow) edby.getDamager();
            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }
            
            Player player = (Player) arrow.getShooter();
            Hero hero = plugin.getCharacterManager().getHero(player);
                
            //double distance=(double)SkillConfigManager.getUseSetting(hero, skill, "melee-range", 3.0, false);
            //distance=distance*distance;
            //double reduction=(double)SkillConfigManager.getUseSetting(hero, skill, "melee-reduction", 50.0, false);
            int mana=(int)SkillConfigManager.getUseSetting(hero, skill, "mana-increase", 3, false);
            
            
            if (hero.hasEffect("EnchantedArrows")) {
                //if(player.getLocation().distanceSquared(edby.getEntity().getLocation())<distance) {
                //    edby.setDamage((int)Math.round((1-(reduction/100.0))*edby.getDamage()));
                //}
                hero.setMana(hero.getMana()+mana);
                boolean haswolf = false;
                for(Entity en:hero.getPlayer().getNearbyEntities(40,10,40)) {
                    if(en instanceof Wolf) {
                        try {
                            if(((Wolf)en).getOwner().getName().equalsIgnoreCase(hero.getName())) {
                                haswolf=true;
                                ((Wolf)en).setTarget((LivingEntity)event.getEntity());
                            }
                        }catch(Exception exd){}
                    }
                }
                if(!haswolf) {
                    if(!hero.hasEffect("WoofWolf")) {
                        int wolfhp=(int)SkillConfigManager.getUseSetting(hero, skill, "wolf-hp", 300, false);
                        Wolf a = (Wolf) hero.getPlayer().getWorld().spawn(edby.getEntity().getLocation(),Wolf.class);
                        a.setMaxHealth(wolfhp);
                        a.setHealth(wolfhp);
                        a.setOwner(hero.getPlayer());
                        a.setTamed(true);
                        a.setTarget((LivingEntity)edby.getEntity());
                        final Wolf fa = a;
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("Heroes"), new Runnable() {
                            @Override
                            public void run() {
                                if(fa!=null) {
                                    fa.remove();
                                }
                            }
                        }, (30*20)); //30s duration

                        hero.addEffect(new ExpirableEffect(null, "WoofWolf", 30000));// 5s cooldown
                    }
                }
            }
        }
    }
    
}