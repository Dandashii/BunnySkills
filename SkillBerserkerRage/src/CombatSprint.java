import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.QuickenEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class CombatSprint extends PeriodicExpirableEffect {

    int amp;
    
    public CombatSprint(Skill sk, Heroes pl, String n, long d, int a) {
        super(sk,pl,n,1000,d);
        amp = a;
    }

    @Override
    public void applyToHero(Hero hero) {
        super.applyToHero(hero);
        hero.addEffect(new QuickenEffect(this.getSkill(), "berserker-sprint", this.getDuration(), amp, null, null));
    }

    @Override
    public void removeFromHero(Hero hero) {
        super.removeFromHero(hero);
        hero.removeEffect(hero.getEffect("berserker-sprint"));
    }
    
    @Override
    public void tickMonster(Monster mnstr) {
        
    }

    @Override
    public void tickHero(Hero hero) {
        if(!hero.isInCombat()) {
            if(hero.hasEffect("berserker-sprint")) {
                hero.removeEffect(hero.getEffect("berserker-sprint"));
            }
        }
        else {
            if(hero.hasEffect("berserker-sprint")) {
                hero.removeEffect(hero.getEffect("berserker-sprint"));
            }
            if(!hero.hasEffectType(EffectType.SLOW)) {
                hero.addEffect(new QuickenEffect(this.getSkill(), "berserker-sprint", this.getDuration(), amp, null, null));
            }
        }
    }
    
}
