import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;


public class SoulShardEffect extends ExpirableEffect {
    int souls=0;
    int maxsouls;
    
    public SoulShardEffect(Skill skill, Heroes plugin, String name, long duration, int maxs) {
        super(skill,plugin,name,duration);
        souls=1;
        maxsouls=maxs;
    }
    
    public void addSouls(int a, Hero hero) {
        if((souls+a)>maxsouls) {
            souls=maxsouls;
        }
        else {
            souls+=a;
        }
        if(souls<=0) {
            souls=0;
        }
        
        this.reapplyToHero(hero);
        // RE APPLY PLS
    }
    
    public int getSouls() {
        return souls;
    }
}
