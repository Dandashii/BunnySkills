import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvisibleEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.BlockIterator;

public class SkillTimeRift extends ActiveSkill {

    public SkillTimeRift(Heroes plugin) {
        super(plugin, "TimeRift");
        setDescription("Teleports you up to $1 blocks away.");
        setUsage("/skill timerift");
        setArgumentRange(0, 0);
        setIdentifiers("skill timerift");
        setTypes(SkillType.SILENCABLE, SkillType.TELEPORT);
        
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.MAX_DISTANCE.node(), 3);
        node.set(SkillSetting.DAMAGE.node(), 2);
        return node;
    }

    public boolean halfblock(Location loc) {
        loc.add(0,1,0);
        Material mat = loc.getBlock().getType();
        
        if(mat.getId()==44) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        Location loc = player.getLocation();
        if (loc.getBlockY() > loc.getWorld().getMaxHeight() || loc.getBlockY() < 1) {
            Messaging.send(player, "The void prevents you from timerifting!");
            return SkillResult.FAIL;
        }
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 3, false);
        Block prev = null;
        Block b;
        BlockIterator iter;
        try {
            Location loc2 = player.getLocation();
            loc2.setPitch(0);
            
            iter = new org.bukkit.util.BlockIterator(loc2, 0, distance);
            //iter = new BlockIterator(player, distance);
        } catch (IllegalStateException e) {
            Messaging.send(player, "There was an error getting your timerift location!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        while (iter.hasNext()) {
            b = iter.next();
            if (!halfblock(b.getLocation()) && Util.transparentBlocks.contains(b.getType()) && (Util.transparentBlocks.contains(b.getRelative(BlockFace.UP).getType()) || Util.transparentBlocks.contains(b.getRelative(BlockFace.DOWN).getType()))) {
                prev = b;
            } else {
                break;
            }
        }
        
        int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 2, false);
        
        if (prev != null) {
            Location teleport = prev.getLocation().clone();
            // Set the timerift location yaw/pitch to that of the player
            teleport.setPitch(player.getLocation().getPitch());
            teleport.setYaw(player.getLocation().getYaw());
            
            
            for(LivingEntity le: getNearbyLivingEntities(teleport,3)) {
                if (le.getEntityId()!=hero.getPlayer().getEntityId()) {
                    if(le instanceof Player) {
                        if(!((Player)le).equals(hero.getPlayer())) {
                            if(damageCheck(hero.getPlayer(),(Player)le)) {
                                addSpellTarget((Player)le, hero);
                                damageEntity((Player)le,hero.getPlayer(),damage,DamageCause.MAGIC,false);
                            }
                        }
                    }
                    else {
                        if(damageCheck(hero.getPlayer(),le)) {
                            addSpellTarget(le, hero);
                            damageEntity(le,hero.getPlayer(),damage,DamageCause.MAGIC,false);
                        }
                    }
                }
            }
            
            player.teleport(teleport);
            //player.setVelocity(new Vector(0,-5,0));
            
            final Skill voidshift=this;
            final Hero fHero = hero;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    fHero.addEffect(new InvisibleEffect(voidshift,250,"",""));
                }
            }, (2));
            
            return SkillResult.NORMAL;
        } else {
            Messaging.send(player, "No location to timerift to.");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
    }

    public static List<LivingEntity> getNearbyLivingEntities(Location l, double radius) {
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
    
    @Override
    public String getDescription(Hero hero) {
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 3, false);
        return getDescription().replace("$1", distance + "");
    }
    
}