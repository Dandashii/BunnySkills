import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class SkillFlashyclothes extends ActiveSkill {
    public SkillFlashyclothes(Heroes plugin) {
        super(plugin, "Flashyclothes");
        setDescription("Random clothes colours.");
        setUsage("/skill Flashyclothes");
        
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill Flashyclothes" });
        
        setTypes(SkillType.DAMAGING);
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
        
        return node;
    }

    @Override
    public void init() {
        super.init();
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        
	PlayerInventory playerInv = hero.getPlayer().getInventory();
        
        int col = (int)(Math.random()*16777216);
        
        if(playerInv.getHelmet()!=null)
        if(playerInv.getHelmet().getType()==Material.LEATHER_HELMET) {
            playerInv.setHelmet(setColor(new ItemStack(Material.LEATHER_HELMET, 1, (short) 1),col));
        }
        
        col = (int)(Math.random()*16777216);
        if(playerInv.getChestplate()!=null)
        if(playerInv.getChestplate().getType()==Material.LEATHER_CHESTPLATE) {
            playerInv.setChestplate(setColor(new ItemStack(Material.LEATHER_CHESTPLATE, 1, (short) 1),col));
        }
        
        col = (int)(Math.random()*16777216);
        if(playerInv.getBoots()!=null)
        if(playerInv.getBoots().getType()==Material.LEATHER_BOOTS) {
            playerInv.setBoots(setColor(new ItemStack(Material.LEATHER_BOOTS, 1, (short) 1),col));
        }
        
        col = (int)(Math.random()*16777216);
        if(playerInv.getLeggings()!=null)
        if(playerInv.getLeggings().getType()==Material.LEATHER_LEGGINGS) {
            playerInv.setLeggings(setColor(new ItemStack(Material.LEATHER_LEGGINGS, 1, (short) 1),col));
        }
        
        
        
        return SkillResult.NORMAL;
    }
    
    public ItemStack setColor(ItemStack item, int color){
        CraftItemStack craftStack = null;
        net.minecraft.server.ItemStack itemStack = null;
        if (item instanceof CraftItemStack) {
            craftStack = (CraftItemStack) item;
            
            itemStack = CraftItemStack.asNMSCopy(craftStack);
            
        }
        else if (item instanceof ItemStack) {
            craftStack = new CraftItemStack(item);
            itemStack = craftStack.getHandle();
        }
        NBTTagCompound tag = itemStack.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
            tag.setCompound("display", new NBTTagCompound());
            itemStack.tag = tag;
        }

        tag = itemStack.tag.getCompound("display");
        tag.setInt("color", color);
        itemStack.tag.setCompound("display", tag);
        return craftStack;
    }

}