package magicbees.bees;

import com.google.common.base.Preconditions;
import forestry.api.apiculture.*;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IAlleleEffect;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.core.ModuleCore;
import magicbees.MagicBees;
import magicbees.bees.mutation.MoonPhaseMutationBonus;
import magicbees.bees.mutation.MoonPhaseMutationRestriction;
import magicbees.elec332.corerepack.compat.forestry.ForestryAlleles;
import magicbees.elec332.corerepack.compat.forestry.IIndividualBranch;
import magicbees.elec332.corerepack.compat.forestry.IIndividualDefinition;
import magicbees.elec332.corerepack.compat.forestry.bee.BeeGenomeTemplate;
import magicbees.elec332.corerepack.compat.forestry.bee.IBeeTemplate;
import magicbees.elec332.corerepack.util.MoonPhase;
import magicbees.init.AlleleRegister;
import magicbees.init.ItemRegister;
import magicbees.item.types.*;
import magicbees.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static magicbees.elec332.corerepack.compat.forestry.ForestryAlleles.*;

/**
 * Created by Elec332 on 15-8-2016.
 */
public enum EnumBeeSpecies implements IBeeTemplate {

    MYSTICAL("mysticum", EnumBeeBranches.VEILED, true, new Color(0xAFFFB7)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMundaneMutations();
        }

    },
    SORCEROUS("fascinatio", EnumBeeBranches.VEILED, true, new Color(0xEA9A9A)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_HIGH);
            template.setTemperatureTolerance(TOLERANCE_DOWN_2);
            template.setHumidityTolerance(TOLERANCE_UP_1);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMundaneMutations();
        }

    },
    UNUSUAL("inusitatus", EnumBeeBranches.VEILED, true, new Color(0x72D361)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_2);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMundaneMutations();
        }

    },
    ATTUNED("similis", EnumBeeBranches.VEILED, true, new Color(0x0086A8)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_HIGH);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMundaneMutations();
        }

    },
    ELDRITCH("prodigiosus", EnumBeeBranches.VEILED, true, new Color(0x8D75A0)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOWER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.15f);
        }

        @Override // Done with registerMundaneMutations() below.
        public void registerMutations() {
        }

    },

    ESOTERIC("secretiore", EnumBeeBranches.ARCANE, true, new Color(0x001099)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            // Nope
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Cultivated"), ELDRITCH, 10);
        }

    },
    MYSTERIOUS("mysticus", EnumBeeBranches.ARCANE, true, new Color(0x762bc2)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_2);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ELDRITCH, ESOTERIC, 8);
        }

    },
    ARCANE("arcanus", EnumBeeBranches.ARCANE, true, new Color(0xd242df)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_2);
            template.setFertility(FERTILITY_NORMAL);
            template.setFloweringSpeed(FLOWERING_AVERAGE);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.25f);
            speciesBuilder.addSpecialty(EnumBeeSpecies.getDrop(EnumDropType.ENCHANTED), 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ESOTERIC, MYSTERIOUS, 8).addMutationCondition(
                    new MoonPhaseMutationBonus(MoonPhase.WAXING_CRESCENT, MoonPhase.WAXING_GIBBOUS, 1.2F));
        }

    },

    CHARMED("larvatus", EnumBeeBranches.SUPERNATURAL, true, new Color(0x48EEEC)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OTHERWORLDLY), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Cultivated"), ELDRITCH, 10);
        }

    },
    ENCHANTED("cantatus", EnumBeeBranches.SUPERNATURAL, true, new Color(0x18e726)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OTHERWORLDLY), 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ELDRITCH, CHARMED, 8);
        }

    },
    SUPERNATURAL("coeleste", EnumBeeBranches.SUPERNATURAL, true, new Color(0x005614)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_2);
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OTHERWORLDLY), 0.25f);
            speciesBuilder.addSpecialty(EnumBeeSpecies.getPollen(EnumPollenType.UNUSUAL), 0.08f);
        }

        @Override
        public void registerMutations() {
            registerMutation(CHARMED, ENCHANTED, 8)
                    .addMutationCondition(new magicbees.bees.mutation.MoonPhaseMutationBonus(MoonPhase.WANING_GIBBOUS,
                            MoonPhase.WANING_CRESCENT, 1.2F));
        }

    },

    ETHEREAL("diaphanum", EnumBeeBranches.MAGICAL, true, new Color(0xBA3B3B), new Color(0xEFF8FF)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_SHORTENED);
            template.setFloweringSpeed(FLOWERING_AVERAGE);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OTHERWORLDLY), 0.10f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ARCANE, SUPERNATURAL, 7);
        }

    },
    WATERY("aquatilis", EnumBeeBranches.MAGICAL, true, new Color(0x313C5E)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_UP_1);
            template.setTemperatureTolerance(TOLERANCE_DOWN_2);
            template.setFlowerProvider(FLOWERS_SNOW);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.WATERY), 0.25f);
            speciesBuilder.addSpecialty(new ItemStack(Blocks.ICE), 0.025f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SUPERNATURAL, ETHEREAL, 14).requireResource(Blocks.WATER.getDefaultState());
        }

    },
    EARTHY("fictili", EnumBeeBranches.MAGICAL, true, new Color(0x78822D)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setLifeSpan(LIFESPAN_LONG);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.EARTHY), 0.25f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SUPERNATURAL, ETHEREAL, 14)
                    .requireResource(Blocks.BRICK_BLOCK.getBlockState().getValidStates().toArray(new IBlockState[0]));
        }

    },
    FIREY("ardens", EnumBeeBranches.MAGICAL, true, new Color(0xD35119)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_DOWN_1);
            template.setTemperatureTolerance(TOLERANCE_UP_2);
            template.setFlowerProvider(FLOWERS_CACTI);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FIREY), 0.25f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SUPERNATURAL, ETHEREAL, 14).requireResource(Blocks.LAVA.getDefaultState());
        }

    },
    WINDY("ventosum", EnumBeeBranches.MAGICAL, true, new Color(0xFFFDBA)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_2);
            template.setSpeed(SPEED_FASTER);
            template.setFloweringSpeed(FLOWERING_FASTER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.AIRY), 0.25f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SUPERNATURAL, ETHEREAL, 14).requireResource("treeLeaves");
        }

    },

    PUPIL("disciplina", EnumBeeBranches.SCHOLARLY, true, new Color(0xFFFF00)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOWER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.PAPERY), 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Monastic"), ARCANE, 10);
        }

    },
    SCHOLARLY("studiosis", EnumBeeBranches.SCHOLARLY, false, new Color(0x6E0000)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_LOW);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.PAPERY), 0.25f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(ItemRegister.resourceItem.getStackFromType(EnumResourceType.LORE_FRAGMENT),
                        0.15f); // 0.2
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(ARCANE, PUPIL, 8);
        }

    },
    SAVANT("philologus", EnumBeeBranches.SCHOLARLY, false, new Color(0xFFA042)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_NORMAL);
            template.setFertility(FERTILITY_LOW);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.PAPERY), 0.40f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(ItemRegister.resourceItem.getStackFromType(EnumResourceType.LORE_FRAGMENT),
                        0.4f); // 0.5
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(PUPIL, SCHOLARLY, 6);
        }

    },

    AWARE("sensibilis", EnumBeeBranches.SOUL, false, new Color(0x5E95B5)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, ATTUNED, 10);
        }

    },
    SPIRIT("larva", EnumBeeBranches.SOUL, true, new Color(0xb2964b)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_SHORTENED);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.22f);
            speciesBuilder.addSpecialty(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, AWARE, 8);
            registerMutation(ATTUNED, AWARE, 8);
        }

    },
    SOUL("anima", EnumBeeBranches.SOUL, false, new Color(0x7d591b)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_DOWN_2);
            template.setLifeSpan(LIFESPAN_NORMAL);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.28f);
            speciesBuilder.addSpecialty(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(AWARE, SPIRIT, 7);
        }

    },

    SKULKING("malevolens", EnumBeeBranches.SKULKING, true, new Color(0x524827)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setIsSecret();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Modest"), ELDRITCH, 12);
        }

    },
    GHASTLY("pallens", EnumBeeBranches.SKULKING, false, new Color(0xccccee), new Color(0xbf877c)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
            template.setToleratesRain(TRUE_RECESSIVE);
            template.setFertility(FERTILITY_LOW);
            template.setEffect(AlleleRegister.spawnGhast);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setIsSecret();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.08f);
            speciesBuilder.addSpecialty(new ItemStack(Items.GHAST_TEAR), 0.099f);
        }

        @Override
        public void registerMutations() {
            registerMutation(BATTY, ETHEREAL, 9);
        }

    },
    SPIDERY("araneolus", EnumBeeBranches.SKULKING, true, new Color(0x0888888), new Color(0x222222)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTerritory(TERRITORY_LARGER);
            template.setEffect(AlleleRegister.spawnSpider);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setIsSecret();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.13f);
            speciesBuilder.addProduct(new ItemStack(Items.STRING), 0.08f);
            speciesBuilder.addSpecialty(new ItemStack(Items.SPIDER_EYE), 0.08f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Tropical"), SKULKING, 10);
        }

    },
    SMOULDERING("flagrantia", EnumBeeBranches.SKULKING, false, new Color(0xFFC747), new Color(0xEA8344)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setEffect(AlleleRegister.spawnBlaze);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HELLISH);
            speciesBuilder.setIsSecret();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.BLAZE_ROD), 0.05f);
        }

        @Override
        public void registerMutations() {
            registerMutation(GHASTLY, HATEFUL, 7).restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

    },
    BRAINY("cerebrum", EnumBeeBranches.SKULKING, true, new Color(0x83FF70)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_LOW);
            template.setEffect(AlleleRegister.spawnZombie);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setIsSecret();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addProduct(new ItemStack(Items.ROTTEN_FLESH), 0.06f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(BeeIntegrationInterface.zombieBrain, 0.2f);
            }
        }

        @Override
        public void registerMutations() {
            EnumBeeSpecies second;
            int chance;
            if (EnumBeeBranches.THAUMIC.enabled()) {
                second = PUPIL;
                chance = 9;
            } else {
                second = MUTABLE;
                chance = 14;
            }
            registerMutation(SKULKING, second, chance);
        }

    },

    BIGBAD("magnumalum", EnumBeeBranches.SKULKING, true, new Color(0xA9344B), new Color(0x453536)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setEffect(AlleleRegister.spawnWolf);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setNocturnal();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.18f);
            speciesBuilder.addProduct(new ItemStack(Items.BEEF), 0.12f);
            speciesBuilder.addProduct(new ItemStack(Items.CHICKEN), 0.12f);
            // Is in original... speciesBuilder.addSpecialty(new ItemStack(Items.MELON),
            // 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SKULKING, MYSTERIOUS, 7)
                    .addMutationCondition(new MoonPhaseMutationRestriction(MoonPhase.FULL));
        }

    },

    CHICKEN("pullus", EnumBeeBranches.FLESHY, true, new Color(0xFF0000), new Color(0xD3D3D3)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setEffect(AlleleRegister.spawnChicken);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.23f);
            speciesBuilder.addSpecialty(new ItemStack(Items.FEATHER), 0.08f);
            speciesBuilder.addProduct(new ItemStack(Items.EGG), 0.08f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(BeeIntegrationInterface.nuggetChicken, 0.7f); // 0.9
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Common"), SKULKING, 12)
                    .restrictBiomeType(BiomeDictionary.Type.FOREST);
        }

    },
    BEEF("bubulae", EnumBeeBranches.FLESHY, true, new Color(0xB7B7B7), new Color(0x3F3024)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setEffect(AlleleRegister.spawnCow);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.25f);
            speciesBuilder.addSpecialty(new ItemStack(Items.LEATHER), 0.165f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(BeeIntegrationInterface.nuggetBeef, 0.7f); // 0.9
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Common"), SKULKING, 12)
                    .restrictBiomeType(BiomeDictionary.Type.PLAINS);
        }

    },
    PORK("porcina", EnumBeeBranches.FLESHY, true, new Color(0xF1AEAC), new Color(0xDF847B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setEffect(AlleleRegister.spawnPig);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.CARROT), 0.165f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(BeeIntegrationInterface.nuggetPork, 0.7f); // 0.9
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Common"), SKULKING, 12)
                    .restrictBiomeType(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS);
        }

    },
    BATTY("chiroptera", EnumBeeBranches.FLESHY, true, new Color(0x5B482B), new Color(0x271B0F)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTerritory(TERRITORY_LARGE);
            template.setEffect(AlleleRegister.spawnBats);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.STRING), 0.00001f);
            if (Loader.isModLoaded(ModNames.THAUMCRAFT)) {
                speciesBuilder.addSpecialty(new ItemStack(Items.GUNPOWDER), 0.33f); // 0.4
            }
        }

        @Override
        public void registerMutations() {
            registerMutation(SKULKING, WINDY, 9);
        }

    },
    SHEEPISH("balans", EnumBeeBranches.FLESHY, true, new Color(0xF7F7F7), new Color(0xCACACA)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTemperatureTolerance(TOLERANCE_DOWN_2);
            template.setEffect(AlleleRegister.spawnSheep);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.25f);
            speciesBuilder.addSpecialty(new ItemStack(Blocks.WOOL), 0.16f);
            speciesBuilder.addSpecialty(new ItemStack(Items.WHEAT), 0.24f);

        }

        @Override
        public void registerMutations() {
            registerMutation(PORK, SKULKING, 13).restrictBiomeType(BiomeDictionary.Type.PLAINS);
        }

    },
    HORSE("equus", EnumBeeBranches.FLESHY, true, new Color(0x906330), new Color(0x7B4E1B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_FASTEST);
            template.setEffect(AlleleRegister.spawnHorse);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.25f);
            speciesBuilder.addSpecialty(new ItemStack(Items.LEATHER), 0.24f);
            speciesBuilder.addSpecialty(new ItemStack(Items.APPLE), 0.38f);
        }

        @Override
        public void registerMutations() {
            registerMutation(BEEF, SHEEPISH, 12).restrictBiomeType(BiomeDictionary.Type.PLAINS);
        }

    },
    CATTY("feline", EnumBeeBranches.FLESHY, true, new Color(0xECE684), new Color(0x563C24)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTemperatureTolerance(TOLERANCE_BOTH_3);
            template.setEffect(AlleleRegister.spawnCat);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.25f);
            speciesBuilder.addSpecialty(new ItemStack(Items.FISH), 0.24f);
        }

        @Override
        public void registerMutations() {
            registerMutation(CHICKEN, SPIDERY, 15).restrictBiomeType(BiomeDictionary.Type.JUNGLE);
        }

    },
    TIMELY("gallifreis", EnumBeeBranches.TIME, true, new Color(0xC6AF86)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_ELONGATED);
            template.setEffect(AlleleRegister.effectSlowSpeed);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TEMPORAL), 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Imperial"), ETHEREAL, 8);
        }

    },
    LORDLY("rassilonis", EnumBeeBranches.TIME, false, new Color(0xC6AF86), new Color(0x8E0213)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_LONG);
            template.setNeverSleeps(TRUE_RECESSIVE);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectDrunkard"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TEMPORAL), 0.19f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Imperial"), TIMELY, 8);
        }

    },
    DOCTORAL("medicus qui", EnumBeeBranches.TIME, false, new Color(0xDDE5FC), new Color(0x4B6E8C)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_3);
            template.setTerritory(TERRITORY_LARGE);
            template.setNeverSleeps(TRUE_RECESSIVE);
            template.setLifeSpan(LIFESPAN_LONGEST);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectHeroic"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TEMPORAL), 0.24f);
            speciesBuilder.addSpecialty(new ItemStack(ItemRegister.jellyBaby), 0.078f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TIMELY, LORDLY, 7);
        }

    },
    INFERNAL("infernales", EnumBeeBranches.ABOMINABLE, true, new Color(0xFF1C1C)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOW);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.12f);
        }

        @Override
        public void registerMutations() {

        }

    },
    HATEFUL("odibilis", EnumBeeBranches.ABOMINABLE, false, new Color(0xDB00DB)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOW);
            template.setLifeSpan(LIFESPAN_ELONGATED);
            template.setTerritory(TERRITORY_LARGER);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectMisanthrope"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(INFERNAL, ELDRITCH, 9).restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

    },
    SPITEFUL("maligna", EnumBeeBranches.ABOMINABLE, false, new Color(0x5FCC00)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_LONG);
            template.setSpeed(SPEED_NORMAL);
            template.setTerritory(TERRITORY_LARGER);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectMisanthrope"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.23f);
        }

        @Override
        public void registerMutations() {
            registerMutation(INFERNAL, HATEFUL, 7).restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

    },
    WITHERING("vietus", EnumBeeBranches.ABOMINABLE, false, new Color(0x5B5B5B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTerritory(TERRITORY_LARGEST);
            template.setFertility(FERTILITY_LOW);
            template.setEffect(AlleleRegister.effectWithering);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addSpecialty(ItemRegister.resourceItem.getStackFromType(EnumResourceType.SKULL_CHIP), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Demonic"), SPITEFUL, 6)
                    .restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

    },
    OBLIVION("oblivioni", EnumBeeBranches.EXTRINSIC, false, new Color(0xD5C3E5)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_MAXIMUM);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FORGOTTEN), 0.14f);
        }

        @Override
        public void registerMutations() {
            // Spawns in hives
        }

    },
    NAMELESS("sine nomine", EnumBeeBranches.EXTRINSIC, true, new Color(0x8ca7cb)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_HIGH);
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FORGOTTEN), 0.19f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, OBLIVION, 10);
        }

    },
    ABANDONED("reliquit", EnumBeeBranches.EXTRINSIC, true, new Color(0xc5cb8c)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setLifeSpan(LIFESPAN_ELONGATED);
            template.setFertility(FERTILITY_NORMAL);
            template.setSpeed(SPEED_SLOW);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectRepulsion"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FORGOTTEN), 0.24f);
        }

        @Override
        public void registerMutations() {
            registerMutation(OBLIVION, NAMELESS, 8);
        }

    },
    FORLORN("perditus", EnumBeeBranches.EXTRINSIC, false, new Color(0xcba88c)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_LOW);
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setLifeSpan(LIFESPAN_LONGEST);
            template.setSpeed(SPEED_SLOW);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectRepulsion"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FORGOTTEN), 0.30f);
        }

        @Override
        public void registerMutations() {
            registerMutation(NAMELESS, ABANDONED, 6);
        }

    },
    DRACONIC("draconic", EnumBeeBranches.EXTRINSIC, false, new Color(0x9f56ad), new Color(0x5a3b62)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_LOW);
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setLifeSpan(LIFESPAN_LONGEST);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectMisanthrope"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(ItemRegister.resourceItem.getStackFromType(EnumResourceType.DRAGON_DUST), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Imperial"), ABANDONED, 6)
                    .restrictBiomeType(BiomeDictionary.Type.END);
        }

    },
    MUTABLE("mutable", EnumBeeBranches.TRANSMUTING, false, new Color(0xDBB24C), new Color(0xE0D5A6)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.PARCHED), 0.30f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.10f);
        }

        @Override
        public void registerMutations() {
            registerMutation(UNUSUAL, ELDRITCH, 12);
        }

    },
    TRANSMUTING("transmuting", EnumBeeBranches.TRANSMUTING, false, new Color(0xDBB24C), new Color(0xA2D2D8)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setEffect(AlleleRegister.effectTransmuting);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.PARCHED), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.30f);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.SILKY), 0.05f);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.SIMMERING), 0.05f);
        }

        @Override
        public void registerMutations() {
            registerMutation(UNUSUAL, MUTABLE, 9);
        }

    },
    CRUMBLING("crumbling", EnumBeeBranches.TRANSMUTING, false, new Color(0xDBB24C), new Color(0xDBA4A4)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setEffect(AlleleRegister.effectCrumbling);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.PARCHED), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.30f);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.POWDERY), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.COCOA), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMutation(UNUSUAL, MUTABLE, 9);
        }

    },
    INVISIBLE("invisible", EnumBeeBranches.VEILED, false, new Color(0xffccff), new Color(0xffccff)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_SHORTEST);
            template.setHumidityTolerance(TOLERANCE_UP_1);
            template.setTemperatureTolerance(TOLERANCE_DOWN_2);
            template.setEffect(AlleleRegister.effectInvisibility);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.35f);
        }

        @Override
        public void registerMutations() {
            registerMutation(MYSTICAL, MUTABLE, 15);
        }

    },

    // Resource bees, gems and ores

    IRON("ferrus", EnumBeeBranches.METALLIC, true, new Color(0x686868), new Color(0xE9E9E9)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.IRON, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Common"),
                    EnumBeeSpecies.getForestrySpecies("Industrious"), 12).requireResource(EnumOreResourceType.IRON);
        }

    },
    GOLD("aurum", EnumBeeBranches.METALLIC, false, new Color(0x684B01), new Color(0xFFFF0B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            addOreProduct(EnumOreResourceType.GOLD, speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            EnumBeeSpecies bee1 = LEAD.isActive() ? LEAD : IRON;
            IAlleleBeeSpecies bee2 = EnumBeeSpecies.getForestrySpecies("Imperial"); // todo: EE_minium
            registerMutation(bee2, bee1, 8).requireResource(EnumOreResourceType.GOLD);
        }

    },
    COPPER("aercus", EnumBeeBranches.METALLIC, true, new Color(0x684B01), new Color(0xFFC81A)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.COPPER, speciesBuilder, 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Industrious"),
                    EnumBeeSpecies.getForestrySpecies("Meadows"), 12).requireResource(EnumOreResourceType.COPPER);
        }

    },
    TIN("stannum", EnumBeeBranches.METALLIC, true, new Color(0x3E596D), new Color(0xA6BACB)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.TIN, speciesBuilder, 0.20f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Industrious"),
                    EnumBeeSpecies.getForestrySpecies("Forest"), 12).requireResource(EnumOreResourceType.TIN);
        }

    },
    SILVER("argenteus", EnumBeeBranches.METALLIC, false, new Color(0x747C81), new Color(0x96BFC4)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.SILVER, speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Imperial"), EnumBeeSpecies.getForestrySpecies("Modest"),
                    8).requireResource(EnumOreResourceType.SILVER);
        }

    },
    LEAD("plumbeus", EnumBeeBranches.METALLIC, true, new Color(0x96BFC4), new Color(0x91A9F3)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.LEAD, speciesBuilder, 0.17f);
        }

        @Override
        public void registerMutations() {
            EnumBeeSpecies bee2 = TIN.isActive() ? TIN : (COPPER.isActive() ? COPPER : IRON);
            registerMutation(EnumBeeSpecies.getForestrySpecies("Common"), bee2, 10)
                    .requireResource(EnumOreResourceType.LEAD);
        }

    },
    ALUMINIUM("aluminium", EnumBeeBranches.METALLIC, true, new Color(0xEDEDED), new Color(0x767676)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.ALUMINIUM, speciesBuilder, 0.20f);
        }

        @Override
        public void registerMutations() {
            Set<IBlockState> acceptedBlockStates = new HashSet<>();
            for (String oreDictName : new String[] { "blockAluminium", "blockAluminum" }) {
                for (ItemStack ore : OreDictionary.getOres(oreDictName)) {
                    if (!ore.isEmpty()) {
                        Item oreItem = ore.getItem();
                        Block oreBlock = Block.getBlockFromItem(oreItem);
                        if (oreBlock != Blocks.AIR) {
                            acceptedBlockStates.addAll(oreBlock.getBlockState().getValidStates());
                        }
                    }
                }
            }
            registerMutation(EnumBeeSpecies.getForestrySpecies("Industrious"),
                    EnumBeeSpecies.getForestrySpecies("Cultivated"), 10)
                    .requireResource(acceptedBlockStates.toArray(new IBlockState[0]));
        }

    },
    ARDITE("aurantiaco", EnumBeeBranches.METALLIC, false, new Color(0x720000), new Color(0xFF9E00)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            addOreProduct(EnumOreResourceType.ARDITE, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Industrious"), INFERNAL, 9)
                    .requireResource(EnumOreResourceType.ARDITE);
        }

    },
    COBALT("caeruleo", EnumBeeBranches.METALLIC, false, new Color(0x03265F), new Color(0x59AAEF)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            addOreProduct(EnumOreResourceType.COBALT, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Imperial"), INFERNAL, 11)
                    .requireResource(EnumOreResourceType.COBALT);
        }

    },
    MANYULLYN("manahmanah", EnumBeeBranches.METALLIC, false, new Color(0x481D6D), new Color(0xBD92F1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.setHasEffect();
            addOreProduct(EnumOreResourceType.MANYULLYN, speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ARDITE, COBALT, 9).requireResource(EnumOreResourceType.MANYULLYN);
        }

    },
    OSMIUM("hyacintho", EnumBeeBranches.METALLIC, false, new Color(0x374B5B), new Color(0x6C7B89)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setLifeSpan(LIFESPAN_LONGER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.OSMIUM, speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            IAlleleBeeSpecies bee1 = SILVER.isActive() ? SILVER.getSpecies()
                    : EnumBeeSpecies.getForestrySpecies("Imperial");
            EnumBeeSpecies bee2 = COBALT.isActive() ? COBALT : INFERNAL;
            registerMutation(bee1, bee2, 11).requireResource(EnumOreResourceType.OSMIUM);
        }

    },
    ELECTRUM("electrum", EnumBeeBranches.METALLIC, false, new Color(0xEAF79)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            addOreProduct(EnumOreResourceType.ELECTRUM, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SILVER, GOLD, 10).requireResource(EnumOreResourceType.ELECTRUM);
        }

    },
    PLATINUM("platinum", EnumBeeBranches.METALLIC, false, new Color(0x9EE7F7)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTENether(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.PLATINUM, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(NICKEL, INVAR, 10).requireResource(EnumOreResourceType.PLATINUM);
        }

    },
    NICKEL("nickel", EnumBeeBranches.METALLIC, false, new Color(0xB4C989)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.NICKEL, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(IRON, ESOTERIC, 14).requireResource(EnumOreResourceType.NICKEL);
        }

    },
    INVAR("invar", EnumBeeBranches.METALLIC, false, new Color(0xCDE3A1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTENether(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            addOreProduct(EnumOreResourceType.INVAR, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(IRON, NICKEL, 14).requireResource(EnumOreResourceType.INVAR);
        }

    },
    BRONZE("bronze", EnumBeeBranches.METALLIC, false, new Color(0xB56D07)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.BRONZE, speciesBuilder, 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TIN, COPPER, 12).requireResource(EnumOreResourceType.BRONZE);
        }

    },
    DIAMOND("diamond", EnumBeeBranches.GEM, false, new Color(0x209581), new Color(0x8DF5E3)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.DIAMOND, speciesBuilder, 0.06f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Austere"), GOLD, 7)
                    .requireResource(EnumOreResourceType.DIAMOND);
        }

    },
    EMERALD("prasinus", EnumBeeBranches.GEM, false, new Color(0x005300), new Color(0x17DD62)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.EMERALD, speciesBuilder, 0.04f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Austere"),
                    SILVER.isActive() ? SILVER.getSpecies() : EnumBeeSpecies.getForestrySpecies("Imperial"), 6)
                    .requireResource(EnumOreResourceType.EMERALD);
        }

    },
    APATITE("apatite", EnumBeeBranches.GEM, false, new Color(0x2EA7EC), new Color(0x001D51)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_NORMAL);
            template.setLifeSpan(LIFESPAN_NORMAL);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.APATITE, speciesBuilder, 0.10f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Rural"), COPPER, 12)
                    .requireResource(EnumOreResourceType.APATITE);
        }

    },
    SILICON("siliconisque", EnumBeeBranches.GEM, false, new Color(0xADA2A7), new Color(0x736675)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOW);
            template.setTerritory(TERRITORY_LARGER);
            template.setToleratesRain(TRUE_RECESSIVE);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.SILICON, speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(AE_SKYSTONE.isActive() ? AE_SKYSTONE : EARTHY, IRON, 17);
        }

    },
    CERTUS("alia cristallum", EnumBeeBranches.GEM, true, new Color(0x93C7FF), new Color(0xA6B8C7)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOWER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.CERTUS, speciesBuilder, 0.08f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SILICON.isActive() ? SILICON : IRON, AE_SKYSTONE.isActive() ? AE_SKYSTONE : EARTHY, 17);
        }

    },
    FLUIX("alia cristallum", EnumBeeBranches.GEM, true, new Color(0xFC639E), new Color(0x534797)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOWEST);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            addOreProduct(EnumOreResourceType.FLUIX, speciesBuilder, 0.06f);
        }

        @Override
        public void registerMutations() {
            EnumBeeSpecies beeA = CERTUS.isActive() ? CERTUS : (SILICON.isActive() ? SILICON : IRON);
            EnumBeeSpecies beeB = AE_SKYSTONE.isActive() ? AE_SKYSTONE : EARTHY;
            registerMutation(beeA, beeB, 17);
        }

    },

    // Thermal Expansion
    TE_BLIZZY("blizzard", EnumBeeBranches.ABOMINABLE, false, new Color(0x0073C4), new Color(0xFF7C26)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTEEnd(template);
            template.setFlowerProvider(ForestryAlleles.FLOWERS_SNOW);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectGlacial"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.COLD);
            speciesBuilder.setHumidity(EnumHumidity.NORMAL);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.FROZEN), 0.10f);
            addRequiredOreDictSpeciality("dustBlizz", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Wintry"), SKULKING, 12);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_GELID("cyro", EnumBeeBranches.ABOMINABLE, true, new Color(0x4AAFF7), new Color(0xFF7C26)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            TE_BLIZZY.modifyGenomeTemplate(template);
            template.setEffect(BeeIntegrationInterface.effectSpawnBlizz);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setTemperature(EnumTemperature.COLD);
            speciesBuilder.setHumidity(EnumHumidity.NORMAL);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.FROZEN), 0.10f);
            addRequiredOreDictSpeciality("dustCryotheum", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Icy"), TE_BLIZZY, 8);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_DANTE("inferno", EnumBeeBranches.ABOMINABLE, false, new Color(0xF7AC4A), new Color(0xFF7C26)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTENether(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HELLISH);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.BLAZE_POWDER), 0.05f);
            addRequiredOreDictSpeciality("dustSulfur", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Austere"), SMOULDERING, 12)
                    .restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_PYRO("pyromania", EnumBeeBranches.ABOMINABLE, true, new Color(0xFA930C), new Color(0xFF7C26)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTEEnd(template);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectIgnition"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setTemperature(EnumTemperature.HELLISH);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MOLTEN), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.BLAZE_POWDER), 0.05f);
            addRequiredOreDictSpeciality("dustPyrotheum", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TE_DANTE, TE_COAL, 8).restrictBiomeType(BiomeDictionary.Type.NETHER);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_SHOCKING("horrendum", EnumBeeBranches.ABOMINABLE, false, new Color(0xC5FF26), new Color(0xF8EE00)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTEEnd(template);
            template.setSpeed(SPEED_FAST);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.NORMAL);
            speciesBuilder.setHumidity(EnumHumidity.NORMAL);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.AIRY), 0.16f);
            addRequiredOreDictSpeciality("dustBlitz", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SMOULDERING, WINDY, 12);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_AMPED("concitatus", EnumBeeBranches.ABOMINABLE, false, new Color(0x8AFFFF), new Color(0xECE670)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            TE_SHOCKING.modifyGenomeTemplate(template);
            template.setEffect(BeeIntegrationInterface.effectSpawnBlitz);
            template.setSpeed(AlleleRegister.speedBlinding);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setTemperature(EnumTemperature.NORMAL);
            speciesBuilder.setHumidity(EnumHumidity.NORMAL);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.AIRY), 0.29f);
            addRequiredOreDictSpeciality("dustAerotheum", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TE_SHOCKING, WINDY, 8);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_GROUNDED("tellus", EnumBeeBranches.ABOMINABLE, true, new Color(0xCEC1C1), new Color(0x826767)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTEEnd(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.EARTHY), 0.16f);
            addRequiredOreDictSpeciality("dustBasalz", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SMOULDERING, EARTHY, 12);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_ROCKING("saxsous", EnumBeeBranches.ABOMINABLE, true, new Color(0x980000), new Color(0xAB9D9B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            TE_GROUNDED.modifyGenomeTemplate(template);
            template.setSpeed(SPEED_FASTER);
            template.setEffect(BeeIntegrationInterface.effectSpawnBasalz);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.EARTHY), 0.29f);
            addRequiredOreDictSpeciality("dustPetrotheum", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TE_GROUNDED, EARTHY, 9);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THERMALFOUNDATION);
        }

    },
    TE_COAL("carbonis", EnumBeeBranches.THERMAL, false, new Color(0x2E2D2D)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.HONEY), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TE_CARBON), 0.5f);
            speciesBuilder.addSpecialty(new ItemStack(Items.COAL), 0.05f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SPITEFUL, TIN, 12).requireResource(Blocks.COAL_ORE.getDefaultState());
        }

    },
    TE_DESTABILIZED("electric", EnumBeeBranches.THERMAL, false, new Color(0x5E0203)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TE_DESTABILIZED), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.REDSTONE), 0.05f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Industrious"), SPITEFUL, 12)
                    .requireResource(Blocks.REDSTONE_ORE.getDefaultState());
        }

    },
    TE_LUX("lux", EnumBeeBranches.THERMAL, false, new Color(0xF1FA89)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTENether(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OCCULT), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TE_LUX), 0.10f);
            speciesBuilder.addSpecialty(new ItemStack(Items.GLOWSTONE_DUST), 0.05f);
        }

        @Override
        public void registerMutations() {
            registerMutation(SMOULDERING, INFERNAL, 12).requireResource(Blocks.GLOWSTONE.getDefaultState());
        }

    },
    TE_WINSOME("cuniculus", EnumBeeBranches.THERMAL, false, new Color(0x096B67)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.FURTIVE), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TE_ENDEARING), 0.10f);
            addRequiredOreDictSpeciality("dustPlatinum", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(PLATINUM, OBLIVION, 12);
        }

    },
    TE_ENDEARING("cognito", EnumBeeBranches.THERMAL, true, new Color(0x069E97)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTEEnd(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.MYSTERIOUS), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TE_ENDEARING), 0.05f);
            speciesBuilder.addSpecialty(new ItemStack(Items.ENDER_PEARL), 0.05f);
            addRequiredOreDictSpeciality("nuggetEnderium", speciesBuilder, 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TE_WINSOME, TE_COAL, 8).requireResource("blockEnderium");
        }

    },

    // Redstone Arsenal
    RSA_FLUXED("Thermametallic electroflux", EnumBeeBranches.THERMAL, false, new Color(0x9E060D)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BeeIntegrationInterface.getTemplateTENether(template);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.HONEY), 0.10f);
            speciesBuilder.addSpecialty(BeeIntegrationInterface.itemRSAFluxedElectrumNugget.copy(), 0.09f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ELECTRUM, TE_DESTABILIZED, 10).requireResource("blockElectrumFlux");
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.RSA);
        }

    },

    // Botania
    BOT_ROOTED("truncus", EnumBeeBranches.BOTANICAL, true, new Color(0x00A800)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.10f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EnumBeeSpecies.getForestrySpecies("Forest"), ELDRITCH, 15)
                    .requireResource(BeeIntegrationInterface.livingWood);
        }

    },
    BOT_BOTANIC("botanica", EnumBeeBranches.BOTANICAL, true, new Color(0x94C661)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFlowerProvider(BeeIntegrationInterface.flowersBotania);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.10f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.05f);
            BeeIntegrationInterface.addPetals(speciesBuilder, 0.01f);
        }

        @Override
        public void registerMutations() {

        }

    },
    BOT_BLOSSOM("viridis", EnumBeeBranches.BOTANICAL, false, new Color(0xA4C193)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BOT_BOTANIC.modifyGenomeTemplate(template);
            template.setFertility(FERTILITY_MAXIMUM);
            template.setFloweringSpeed(FLOWERING_FASTER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.20f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.05f);
            BeeIntegrationInterface.addPetals(speciesBuilder, 0.04f);
        }

        @Override
        public void registerMutations() {
            registerMutation(BOT_BOTANIC, EARTHY, 12);
        }

    },
    BOT_FLORAL("florens", EnumBeeBranches.BOTANICAL, true, new Color(0x29D81A)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BOT_BLOSSOM.modifyGenomeTemplate(template);
            template.setFertility(FERTILITY_HIGH);
            template.setFloweringSpeed(FLOWERING_MAXIMUM);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.MUNDANE), 0.25f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.05f);
            BeeIntegrationInterface.addPetals(speciesBuilder, 0.16f);
        }

        @Override
        public void registerMutations() {
            registerMutation(BOT_BOTANIC, BOT_BLOSSOM, 8);
        }

    },
    BOT_VAZBEE("vazbii", EnumBeeBranches.BOTANICAL, false, new Color(0xff6b9c)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BOT_FLORAL.modifyGenomeTemplate(template);
            template.setFertility(FERTILITY_LOW);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.05f);
            speciesBuilder.addProduct(new ItemStack(Items.DYE, 1, 9), 0.20f);
            speciesBuilder.addProduct(new ItemStack(Blocks.WOOL, 1, 9), 0.02f);
            speciesBuilder.addProduct(new ItemStack(Blocks.RED_FLOWER), 0.06f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TRANSMUTED), 0.15f);
            if (BeeIntegrationInterface.itemPastureSeed == null) {
                return;
            }
            for (int i = 0; i < BeeIntegrationInterface.seedTypes; i++) {
                speciesBuilder.addSpecialty(new ItemStack(BeeIntegrationInterface.itemPastureSeed, 1, i), 0.04f);
            }
        }

        @Override
        public void registerMutations() {
        }

    },
    BOT_SOMNOLENT("soporatus", EnumBeeBranches.BOTANICAL, true, new Color(0x2978C6)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFlowerProvider(BeeIntegrationInterface.flowersBotania);
            template.setTemperatureTolerance(TOLERANCE_UP_2);
            template.setNeverSleeps(TRUE_RECESSIVE);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setSpeed(SPEED_SLOWEST);
            template.setEffect(AlleleRegister.effectSlowSpeed);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setNocturnal();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.WATERY), 0.08f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.15f);
        }

        @Override
        public void registerMutations() {
            registerMutation(BOT_ROOTED, WATERY, 16).requireNight();
        }

    },
    BOT_DREAMING("somnior", EnumBeeBranches.BOTANICAL, true, new Color(0x123456)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BOT_SOMNOLENT.modifyGenomeTemplate(template);
            template.setSpeed(SPEED_NORMAL);
            template.setTerritory(TERRITORY_LARGER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setNocturnal();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.WATERY), 0.16f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.33f);
        }

        @Override
        public void registerMutations() {
            registerMutation(WINDY, BOT_SOMNOLENT, 8).requireNight();
        }

    },
    BOT_ALFHEIM("alfheimis", EnumBeeBranches.BOTANICAL, false, new Color(-1), new Color(-1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            BOT_DREAMING.modifyGenomeTemplate(template);
            template.setEffect(BeeIntegrationInterface.effectDreaming);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            Utils.setSecret(speciesBuilder);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.OTHERWORLDLY), 0.28f);
        }

        @Override
        public void registerMutations() {

        }

    },

    // TC6
    TC_AIR("aether", EnumBeeBranches.THAUMIC, true, new Color(0xD9D636)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_FASTEST);
            template.setLifeSpan(LIFESPAN_SHORTENED);
            template.setTerritory(TERRITORY_LARGEST);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setFloweringSpeed(FLOWERING_MAXIMUM);
            template.setEffect(AlleleRegister.effectMoveSpeed);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_AIR), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.AIR), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(WINDY, WINDY, 8).requireResource(BeeIntegrationInterface.blockTCAirShard.getBlockState()
                    .getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_FIRE("praefervidus", EnumBeeBranches.THAUMIC, true, new Color(0xE50B0B)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_FASTER);
            template.setLifeSpan(LIFESPAN_NORMAL);
            template.setTemperatureTolerance(TOLERANCE_DOWN_3);
            template.setHumidityTolerance(TOLERANCE_UP_2);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setEffect((IAlleleEffect) getForestryAllele("effectIgnition"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_FIRE), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.FIRE), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(FIREY, FIREY, 8).requireResource(BeeIntegrationInterface.blockTCFireShard.getBlockState()
                    .getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_WATER("umidus", EnumBeeBranches.THAUMIC, true, new Color(0x36CFD9)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_FAST);
            template.setLifeSpan(LIFESPAN_LONG);
            template.setHumidityTolerance(TOLERANCE_DOWN_2);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTerritory(TERRITORY_LARGE);
            template.setEffect(AlleleRegister.effectCleansing);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.setHumidity(EnumHumidity.DAMP);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_WATER), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.WATER), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(WATERY, WATERY, 8).requireResource(BeeIntegrationInterface.blockTCWaterShard
                    .getBlockState().getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_EARTH("sordida", EnumBeeBranches.THAUMIC, true, new Color(0x005100)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOW);
            template.setLifeSpan(LIFESPAN_LONGER);
            template.setHumidityTolerance(TOLERANCE_DOWN_1);
            template.setToleratesRain(TRUE_RECESSIVE);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setFloweringSpeed(FLOWERING_FASTEST);
            template.setEffect(AlleleRegister.effectDigSpeed);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_EARTH), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.EARTH), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EARTHY, EARTHY, 8).requireResource(BeeIntegrationInterface.blockTCEarthShard
                    .getBlockState().getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_ORDER("ordinatus", EnumBeeBranches.THAUMIC, true, new Color(0xaa32fc)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_NORMAL);
            template.setLifeSpan(LIFESPAN_NORMAL);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_ORDER), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.ORDER), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, ARCANE, 8).requireResource(BeeIntegrationInterface.blockTCOrderShard
                    .getBlockState().getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_ENTROPY("tenebrarum", EnumBeeBranches.THAUMIC, false, new Color(0xCCCCCC)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFertility(FERTILITY_NORMAL);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TC_ENTROPY), 0.2f);
            speciesBuilder.addSpecialty(ItemRegister.propolisItem.getStackFromType(EnumPropolisType.ENTROPY), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ARCANE, SUPERNATURAL, 8).requireResource(BeeIntegrationInterface.blockTCEntropyShard
                    .getBlockState().getValidStates().toArray(new IBlockState[0]));
        }

    },
    TC_VIS("arcanus saecula", EnumBeeBranches.THAUMIC, false, new Color(0x004c99), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setSpeed(SPEED_SLOW);
            template.setFloweringSpeed(FLOWERING_SLOWER);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.1f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, INFERNAL, 9).addMutationCondition(
                    new MoonPhaseMutationRestriction(MoonPhase.WAXING_HALF, MoonPhase.WANING_HALF));
        }

    },
    TC_REJUVENATING("arcanus vitae", EnumBeeBranches.THAUMIC, false, new Color(0x91D0D9), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setEffect(BeeIntegrationInterface.effectVisRecharge);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.18f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ATTUNED, TC_VIS, 8);
        }

    },
    TC_EMPOWERING("tractus", EnumBeeBranches.THAUMIC, true, new Color(0x96FFBC), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setEffect(BeeIntegrationInterface.effectNodeEmpower);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.14f);
            speciesBuilder.addProduct(
                    new ItemStack(Utils.getApicultureItems().pollenCluster, 1, EnumPollenCluster.CRYSTALLINE.ordinal()),
                    0.2f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TC_VIS, TC_REJUVENATING, 5)
                    .addMutationCondition(new MoonPhaseMutationBonus(MoonPhase.FULL, MoonPhase.FULL, 1.322f));
        }

    },
    TC_NEXUS("nexi", EnumBeeBranches.THAUMIC, false, new Color(0x15AFAF), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setHumidityTolerance(TOLERANCE_BOTH_1);
            template.setTemperatureTolerance(TOLERANCE_BOTH_1);
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setEffect(BeeIntegrationInterface.effectNodeRepair);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setHasEffect();
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.25f);
            speciesBuilder.addProduct(
                    new ItemStack(Utils.getApicultureItems().pollenCluster, 1, EnumPollenCluster.CRYSTALLINE.ordinal()),
                    0.2f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TEMPORAL), 0.12f);

        }

        @Override
        public void registerMutations() {
            registerMutation(TC_REJUVENATING, TC_EMPOWERING, 9).restrictBiomeType(BiomeDictionary.Type.MAGICAL)
                    .addMutationCondition(BeeIntegrationInterface.TCVisMutationRequirement.apply(350));
        }

    },
    TC_TAINT("arcanus labe", EnumBeeBranches.THAUMIC, false, new Color(0x91376A), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setEffect(BeeIntegrationInterface.effectNodeConversionTaint);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.14f); // 0.18
            speciesBuilder.addProduct(
                    new ItemStack(Utils.getApicultureItems().propolis, 1, EnumPropolis.STICKY.ordinal()), 0.213f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TRANSMUTING, TC_EMPOWERING, 11)
                    .addMutationCondition(new MoonPhaseMutationRestriction(MoonPhase.NEW, MoonPhase.NEW));
        }

    },
    TC_PURE("arcanus puritatem", EnumBeeBranches.THAUMIC, false, new Color(0xE23F65), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setFloweringSpeed(FLOWERING_AVERAGE);
            template.setEffect(BeeIntegrationInterface.effectNodeConversionPure);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.16f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.SOUL), 0.19f);
        }

        @Override
        public void registerMutations() {
            registerMutation(TRANSMUTING, TC_REJUVENATING, 8).restrictBiomeType(BiomeDictionary.Type.MAGICAL)
                    .addMutationCondition(new MoonPhaseMutationRestriction(MoonPhase.NEW, MoonPhase.NEW));
        }

    },
    TC_HUNGRY("omnique", EnumBeeBranches.THAUMIC, false, new Color(0xDCA5E2), new Color(0x675ED1)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setFlowerProvider(BeeIntegrationInterface.flowerAuraNode);

            template.setTerritory(TERRITORY_LARGEST);
            template.setEffect(BeeIntegrationInterface.effectNodeConversionHungry);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.INTELLECT), 0.28f);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.TEMPORAL), 0.195f);
        }

        @Override
        public void registerMutations() {
            // registerMutation(BIGBAD, TC_VIS, 15); Original
            registerMutation(BIGBAD, TC_TAINT, 15);
        }

    },
    TC_WISPY("umbrabilis", EnumBeeBranches.THAUMIC, false, new Color(0x9cb8d5), new Color(0xe15236)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_FAST);
            template.setFloweringSpeed(FLOWERING_FASTER);
            template.setNeverSleeps(TRUE_RECESSIVE);
            template.setToleratesRain(TRUE_RECESSIVE);
            template.setFlowerProvider(BeeIntegrationInterface.flowersThaumcraft);
            template.setEffect(BeeIntegrationInterface.effectSpawnWhisp);
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) { // TODO; more fitting drops
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.SILKY), 0.22f);
            speciesBuilder.addSpecialty(ModuleCore.getItems().craftingMaterial.getSilkWisp().copy(), 0.4f);
        }

        @Override
        public void registerMutations() {
            registerMutation(ETHEREAL, GHASTLY, 9).addMutationCondition(
                    new MoonPhaseMutationRestriction(MoonPhase.WANING_CRESCENT, MoonPhase.WAXING_CRESCENT));
        }

    },
    TC_VOID("obscurus", EnumBeeBranches.METALLIC, false, new Color(0x180A29), new Color(0x4B2A74)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setSpeed(SPEED_SLOWEST);
            template.setNeverSleeps(TRUE_RECESSIVE);
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setTemperatureTolerance(TOLERANCE_UP_1);
            template.setFlowerProvider(FLOWERS_VANILLA);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectGlacial"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.ICY);
            speciesBuilder.addProduct(EnumBeeSpecies.getForestryComb(EnumHoneyComb.HONEY), 0.1f);
            speciesBuilder.addSpecialty(BeeIntegrationInterface.voidMetalNugget, 0.155f);
        }

        @Override
        public void registerMutations() {
            registerMutation(IRON, TC_TAINT, 4).restrictBiomeType(BiomeDictionary.Type.MAGICAL).requireNight();
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.THAUMCRAFT);
        }
    },

    // AE2
    AE_SKYSTONE("terra astris", EnumBeeBranches.TRANSMUTING, true, new Color(0x4B8381), new Color(0x252929)) {
        @Override
        public void modifyGenomeTemplate(BeeGenomeTemplate template) {
            template.setCaveDwelling(TRUE_RECESSIVE);
            template.setFertility(FERTILITY_LOW);
            template.setLifeSpan(LIFESPAN_SHORTER);
            template.setHumidityTolerance(TOLERANCE_NONE);
            template.setTemperatureTolerance(TOLERANCE_NONE);
            template.setEffect((IAlleleEffect) EnumBeeSpecies.getForestryAllele("effectIgnition"));
        }

        @Override
        public void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder) {
            speciesBuilder.setTemperature(EnumTemperature.HOT);
            speciesBuilder.setHumidity(EnumHumidity.ARID);
            speciesBuilder.addProduct(EnumBeeSpecies.getComb(EnumCombType.EARTHY), 0.19f);
            speciesBuilder.addSpecialty(new ItemStack(BeeIntegrationInterface.aeSkyStone.getBlock()), 0.02f);
        }

        @Override
        public void registerMutations() {
            registerMutation(EARTHY, WINDY, 20).requireResource(BeeIntegrationInterface.aeSkyStone);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && Loader.isModLoaded(ModNames.AE2);
        }

    };

    EnumBeeSpecies(String binominalName, IMagicBeesBranch branch, boolean dominant, Color primaryColor) {
        this(binominalName, branch, dominant, primaryColor, branch.getSecondaryColor());
    }

    EnumBeeSpecies(String binomialName, IMagicBeesBranch branch, boolean dominant, Color primaryColor,
            Color secondaryColor) {
        this.branch = branch;
        this.primaryColor = primaryColor.getRGB();
        this.secondaryColor = secondaryColor.getRGB();
        this.binominalName = binomialName;
        this.dominant = dominant;
        String nme;
        String[] spl = name().split("_");
        if (spl.length == 1) {
            nme = WordUtils.capitalize(spl[0].toLowerCase(Locale.ENGLISH));
        } else if (spl.length == 2) {
            nme = spl[0] + WordUtils.capitalize(spl[1].toLowerCase(Locale.ENGLISH));
        } else {
            throw new RuntimeException();
        }
        this.uid = MagicBees.modid + ".species" + nme;
        this.unlocalisedName = uid;
        this.enabledOverride = true;
    }

    private final IMagicBeesBranch branch;
    private final int primaryColor, secondaryColor;
    private final String binominalName, uid, unlocalisedName;
    private final boolean dominant;
    private boolean enabledOverride;

    private IIndividualDefinition<IBeeGenome, IBee, EnumBeeType> individual;

    @Override
    public int getPrimaryColor() {
        return primaryColor;
    }

    @Override
    public int getSecondaryColor() {
        return secondaryColor;
    }

    @Nonnull
    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public boolean isDominant() {
        return dominant;
    }

    @Nonnull
    @Override
    public String getUnlocalizedName() {
        return unlocalisedName;
    }

    @Nonnull
    @Override
    public String getBinominalName() {
        return this.binominalName;
    }

    @Nonnull
    @Override
    public IIndividualBranch<BeeGenomeTemplate> getIndividualBranch() {
        return branch;
    }

    @Override
    public abstract void modifyGenomeTemplate(BeeGenomeTemplate template);

    @Override
    public abstract void setSpeciesProperties(IAlleleBeeSpeciesBuilder speciesBuilder);

    @Override
    public abstract void registerMutations();

    private static ItemStack getComb(EnumCombType combType) {
        return ItemRegister.combItem.getStackFromType(combType);
    }

    private static ItemStack getDrop(EnumDropType drop) {
        return ItemRegister.dropItem.getStackFromType(drop);
    }

    private static ItemStack getPollen(EnumPollenType pollen) {
        return ItemRegister.pollenItem.getStackFromType(pollen);
    }

    private static ItemStack getForestryComb(EnumHoneyComb type) {
        return new ItemStack(Utils.getApicultureItems().beeComb, 1, type.ordinal());
    }

    protected final void addRequiredOreDictSpeciality(String resource, IAlleleBeeSpeciesBuilder speciesBuilder,
            float speciality) {
        List<ItemStack> l = OreDictionary.getOres(resource);
        for (ItemStack stack : l) {
            if (!stack.isEmpty()) {
                speciesBuilder.addSpecialty(stack, speciality);
                return;
            }
        }
        this.enabledOverride = false;
    }

    protected final void addOreProduct(EnumOreResourceType resource, IAlleleBeeSpeciesBuilder speciesBuilder,
            float speciality) {
        speciesBuilder.addProduct(getForestryComb(EnumHoneyComb.HONEY), 0.10f);
        if (this.enabledOverride = resource.enabled()) {
            speciesBuilder.addSpecialty(resource.getStack(), speciality);
        }
    }

    protected final MagicBeesMutationBuilder registerMutation(EnumBeeSpecies bee1, EnumBeeSpecies bee2, int chance) {
        if (!bee1.isActive()) {
            MagicBees.logger.info("Species " + bee1 + " is not active, not registering mutation for bee: " + this);
        }
        return registerMutation(bee1.getSpecies(), bee2, chance);
    }

    protected final MagicBeesMutationBuilder registerMutation(IAlleleBeeSpecies bee1, EnumBeeSpecies bee2, int chance) {
        if (!bee2.isActive()) {
            MagicBees.logger.info("Species " + bee2 + " is not active, not registering mutation for bee: " + this);
        }
        return registerMutation(bee1, bee2.getSpecies(), chance);
    }

    protected final MagicBeesMutationBuilder registerMutation(IAlleleBeeSpecies bee1, IAlleleBeeSpecies bee2,
            int chance) {
        Preconditions.checkNotNull(bee1);
        Preconditions.checkNotNull(bee2);
        MagicBeesMutationBuilder beeMutation = new MagicBeesMutationBuilder(bee1, bee2,
                this.getIndividualDefinition().getAlleles(), chance) {

            @Override
            public MagicBeesMutationBuilder requireResource(EnumOreResourceType ore) {
                if (!ore.blockExists()) {
                    enabledOverride = false;
                }
                return super.requireResource(ore);
            }

        };
        Preconditions.checkNotNull(BeeManager.beeRoot).registerMutation(beeMutation);
        return beeMutation;
        // return
        // Preconditions.checkNotNull(forestry.api.apiculture.BeeManager.beeMutationFactory).createMutation(bee1,
        // bee2, this.getIndividualDefinition().getAlleles(), chance);
    }

    public IAlleleBeeSpecies getSpecies() {
        return (IAlleleBeeSpecies) getIndividualDefinition().getAlleles()[EnumBeeChromosome.SPECIES.ordinal()];
    }

    @Override
    public boolean isActive() {
        return branch.enabled() && enabledOverride;
    }

    @Override
    public void setIndividualDefinition(IIndividualDefinition<IBeeGenome, IBee, EnumBeeType> iIndividualDefinition) {
        individual = iIndividualDefinition;
    }

    @Override
    public IIndividualDefinition<IBeeGenome, IBee, EnumBeeType> getIndividualDefinition() {
        return individual;
    }

    protected void registerMundaneMutations() {
        String[] forestryMundane = new String[] { "Forest", "Meadows", "Modest", "Wintry", "Tropical", "Marshy" };
        for (String s : forestryMundane) {
            forestry.api.apiculture.BeeManager.beeMutationFactory.createMutation(getSpecies(), getForestrySpecies(s),
                    getForestrySpeciesTemplate("Common"), 15);
        }
        forestry.api.apiculture.BeeManager.beeMutationFactory.createMutation(getSpecies(), getForestrySpecies("Common"),
                getForestrySpeciesTemplate("Cultivated"), 12);
        forestry.api.apiculture.BeeManager.beeMutationFactory.createMutation(getSpecies(),
                getForestrySpecies("Cultivated"), ELDRITCH.getAlleles(), 12);
    }

    public static IAllele[] getForestrySpeciesTemplate(String speciesName) {
        return forestry.api.apiculture.BeeManager.beeRoot.getTemplate("forestry.species" + speciesName);
    }

    public static IAlleleBeeSpecies getForestrySpecies(String name) {
        return (IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele("forestry.species" + name);
    }

    public static IAllele getForestryAllele(String name) {
        return AlleleManager.alleleRegistry.getAllele("forestry." + name);
    }

}
