import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SkillThrowaxe extends ActiveSkill implements Listener {
    public SkillThrowaxe(Heroes plugin) {
        super(plugin, "Throwaxe");
        setDescription("You hurl your axe forwards and await its return");
        setUsage("/skill Throwaxe");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Throwaxe" });
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        setTypes(SkillType.DAMAGING, SkillType.DEBUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        
        String description = getDescription().replace("$1", distance + "");
        
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 10);
        node.set(SkillSetting.DAMAGE.node(), 1);
        //node.set(SkillSetting.DURATION.node(), 3000);
        node.set(SkillSetting.MANA.node(),5);
        node.set(SkillSetting.COOLDOWN.node(),100);
        
        
        return node;
    }

    @Override
    public void init() {
        super.init();
        
    }
    
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1, false);
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 1, false);
        //long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 3000, false);
        
        
        
        Vector dir = hero.getPlayer().getLocation().getDirection();
        dir = dir.normalize();
        Location next = hero.getPlayer().getLocation();
        next.setY(next.getY()+1.5);
        double mul = 2;
        
        ItemStack it = new org.bukkit.inventory.ItemStack(Material.STONE_AXE);
        Item i = next.getWorld().dropItem(next, it);
            ilist.add(i);
            islist.add(it);
            hero.addEffect(new com.herocraftonline.heroes.characters.effects.Effect(this,"throwing"));
        
        //run recursive method
        forward(next,hero,dir,mul,damage,distance, i);
        
        return SkillResult.NORMAL;
    }
    
    public void forward(Location next, final Hero hero, final Vector dir, final double mul, final int damage, final double distance, Item i) {
        if(!hero.getPlayer().isOnline() || hero.getPlayer().isDead()) {
                    ilist.remove(i);
                    islist.remove(i.getItemStack());
                    i.remove();
            return;
        }
        
        Vector idir = dir;
                i.setVelocity(idir.multiply(1.5).setY(idir.getY()+0.1));
                
                List<LivingEntity> targets = new ArrayList<>();
                
                
                next = next.add(dir.getX()*mul, dir.getY()*mul, dir.getZ()*mul);
                //next = i.getLocation();
                
                //next.getWorld().playEffect(next, Effect.POTION_BREAK, 612);

                for(LivingEntity e:getNearbyLivingEntities(next, 2.9)) {
                    if(damageCheck(hero.getPlayer(),e)) {
                        addSpellTarget(e,hero);
                        targets.add(e);
                    }
                }

                for(LivingEntity e:targets) {
                    if(e instanceof Player) {
                        if(damageCheck(hero.getPlayer(), (Player)e)) {
                            damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        }
                            next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    }
                    if(e instanceof Creature) {
                        if(damageCheck(hero.getPlayer(), (Creature)e)) {
                            damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        }
                        next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    }
                }
                
                
                final Item fi = i;
                if(next.distance(hero.getPlayer().getLocation().getBlock().getLocation())<distance) {
                    final Location nxt = next;
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
                        @Override
                        public void run(){ 
                            forward(nxt,hero,dir,mul,damage,distance, fi); }},(long)(2));
                }
                else {
                    next.add(0,0.1,0);
                    final Location nxt = next;
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
                        @Override
                        public void run(){ back(nxt,hero,hero.getPlayer().getLocation().add(0,1,0).subtract(fi.getLocation()).toVector().normalize(),mul,damage,distance, fi); }},(long)(2));
                }
    }
    
    public void back(Location next, final Hero hero, final Vector dir, final double mul, final int damage, final double distance, Item i) {
        if(!hero.getPlayer().isOnline() || hero.getPlayer().isDead() || i.getTicksLived()>(distance*4)) {
                i.remove();
                    ilist.remove(i);
                    islist.remove(i.getItemStack());
                    hero.removeEffect(hero.getEffect("throwing"));
                return;
        }
        Vector idir = dir;
                i.setVelocity(idir.multiply(1.5).setY(idir.getY()+0.1));
        
                List<LivingEntity> targets = new ArrayList<>();
                
                next = next.add(dir.getX()*mul, dir.getY()*mul, dir.getZ()*mul);
                //next = i.getLocation();
                
                //next.getWorld().playEffect(next, Effect.POTION_BREAK, 612);

                for(LivingEntity e:getNearbyLivingEntities(next, 2)) {
                    if(damageCheck(hero.getPlayer(),e)) {
                        addSpellTarget(e,hero);
                        targets.add(e);
                    }
                }

                for(LivingEntity e:targets) {
                    if(e instanceof Player) {
                        if(damageCheck(hero.getPlayer(), (Player)e)) {
                            damageEntity((Player)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        }
                            next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    }
                    if(e instanceof Creature) {
                        if(damageCheck(hero.getPlayer(), (Creature)e)) {
                            damageEntity((Creature)e, hero.getPlayer(), damage, EntityDamageEvent.DamageCause.MAGIC);
                        }
                        next.getWorld().playEffect(e.getLocation(), Effect.POTION_BREAK, 245);
                    System.out.println("BackDamage");
                    }
                }
                System.out.println("checkedAOE");
                if(!i.getWorld().equals(hero.getPlayer().getWorld()) || i.getLocation().distance(hero.getPlayer().getLocation())>(distance*2)) {
                    i.remove();
                    return;
                }
                if(next.distance(hero.getPlayer().getLocation().getBlock().getLocation())>2.05 && !i.isDead()) {
                    System.out.println("distchk");
                    final Location nxt = next;
                    final Item fi = i;
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){                
                        @Override
                        public void run(){ back(nxt,hero,hero.getPlayer().getLocation().subtract(fi.getLocation()).toVector().normalize(),mul,damage,distance, fi); }},(long)(2));
                }
                else {
                    i.remove();
                    ilist.remove(i);
                    islist.remove(i.getItemStack());
                    hero.removeEffect(hero.getEffect("throwing"));
                }
    }
    
    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius){
        int chunkRadius = radius < 16 ? 1 : ((int)(radius - (radius % 16))/16);
        List<LivingEntity> radiusEntities=new ArrayList<>();
        double radiussq = radius*radius;
            for (int chX = 0 -chunkRadius; chX <= chunkRadius; chX ++){
                for (int chZ = 0 -chunkRadius; chZ <= chunkRadius; chZ++){
                    int x=(int) l.getX(),y=(int) l.getY(),z=(int) l.getZ();
                    for (Entity e : new Location(l.getWorld(),x+(chX*16),y,z+(chZ*16)).getChunk().getEntities()){
                        if (e.getLocation().distanceSquared(l) <= radiussq && e.getLocation().getBlock() != l.getBlock()) {
                            if(e instanceof LivingEntity) {
                                radiusEntities.add((LivingEntity)e);
                            }
                        }
                    }
                }
            }
        return radiusEntities;
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
            System.out.println("denied pickup");
            event.setCancelled(true);
            event.getPlayer().getWorld().playEffect(event.getPlayer().getLocation(), Effect.BLAZE_SHOOT, 10);
        }
        
        if(plugin.getCharacterManager().getHero(event.getPlayer()).hasEffect("throwing")) {
                    i.remove();
                    ilist.remove(i);
                    islist.remove(i.getItemStack());
                    plugin.getCharacterManager().getHero(event.getPlayer()).removeEffect(plugin.getCharacterManager().getHero(event.getPlayer()).getEffect("throwing"));
        }
    }
    
}