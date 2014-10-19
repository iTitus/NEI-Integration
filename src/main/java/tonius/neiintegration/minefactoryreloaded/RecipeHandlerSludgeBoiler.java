package tonius.neiintegration.minefactoryreloaded;

import java.awt.Point;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom.Item;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.tile.machine.TileEntitySludgeBoiler;
import tonius.neiintegration.PositionedFluidTank;
import tonius.neiintegration.RecipeHandlerBase;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import cofh.lib.util.WeightedRandomItemStack;

public class RecipeHandlerSludgeBoiler extends RecipeHandlerBase {
    
    private static final Rectangle SLUDGE = new Rectangle(111, 2, 16, 60);
    private static final Rectangle ENERGY = new Rectangle(129, 2, 8, 60);
    private static final Rectangle WORK = new Rectangle(139, 2, 8, 60);
    private static List<Item> drops;
    private static int totalWeight;
    private static int sludgePerOperation;
    private static int energyPerOperation;
    
    @Override
    public void prepare() {
        drops = MFRRegistry.getSludgeDrops();
        for (Item drop : drops) {
            totalWeight += drop.itemWeight;
        }
        
        TileEntitySludgeBoiler dummy = new TileEntitySludgeBoiler();
        sludgePerOperation = dummy.getWorkMax() * 10;
        energyPerOperation = dummy.getActivationEnergy() * dummy.getWorkMax();
        dummy = null;
    }
    
    public class CachedSludgeBoilerRecipe extends CachedBaseRecipe {
        
        public PositionedFluidTank sludgeInput;
        public PositionedStack output;
        public float chance;
        
        public CachedSludgeBoilerRecipe(ItemStack output, int weight) {
            this.sludgeInput = new PositionedFluidTank(SLUDGE, 4000, FluidRegistry.getFluidStack("sludge", sludgePerOperation));
            this.output = new PositionedStack(output, 48, 24);
            this.chance = (float) weight / (float) totalWeight;
        }
        
        @Override
        public PositionedStack getResult() {
            return this.output;
        }
        
        @Override
        public PositionedFluidTank getFluidTank() {
            return this.sludgeInput;
        }
        
    }
    
    @Override
    public String getRecipeName() {
        return "Sludge Boiler";
    }
    
    @Override
    public String getRecipeID() {
        return "minefactoryreloaded.sludgeboiler";
    }
    
    @Override
    public String getGuiTexture() {
        return MineFactoryReloadedCore.guiFolder + "sludgeboiler.png";
    }
    
    @Override
    public void loadTransferRects() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(76, 25, 22, 15), this.getRecipeID(), new Object[0]));
    }
    
    @Override
    public void drawBackground(int recipe) {
        this.changeToGuiTexture();
        GuiDraw.drawTexturedModalRect(0, 0, 11, 13, 160, 65);
        this.changeToOverlayTexture();
        GuiDraw.drawTexturedModalRect(76, 25, 0, 15, 22, 15);
    }
    
    @Override
    public void drawForeground(int recipe) {
        super.drawForeground(recipe);
        this.changeToGuiTexture();
        GuiDraw.drawTexturedModalRect(111, 2, 176, 0, 16, 60);
        this.drawProgressBar(129, 0, 176, 58, 8, 62, 1.0F, 3);
        this.drawProgressBar(139, 0, 185, 58, 8, 62, 40, 3);
    }
    
    @Override
    public void drawExtras(int recipe) {
        CachedSludgeBoilerRecipe crecipe = (CachedSludgeBoilerRecipe) this.arecipes.get(recipe);
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        GuiDraw.drawStringC(percentFormat.format(crecipe.chance), 57, 44, 0x808080, false);
    }
    
    @Override
    public List<String> provideTooltip(GuiRecipe guiRecipe, List<String> currenttip, CachedBaseRecipe crecipe, Point relMouse) {
        super.provideTooltip(guiRecipe, currenttip, crecipe, relMouse);
        if (ENERGY.contains(relMouse)) {
            currenttip.add(energyPerOperation + " RF");
        }
        return currenttip;
    }
    
    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getRecipeID())) {
            for (Item drop : drops) {
                if (drop instanceof WeightedRandomItemStack) {
                    this.arecipes.add(new CachedSludgeBoilerRecipe(((WeightedRandomItemStack) drop).getStack(), drop.itemWeight));
                }
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (Item drop : drops) {
            if (drop instanceof WeightedRandomItemStack) {
                if (NEIServerUtils.areStacksSameTypeCrafting(((WeightedRandomItemStack) drop).getStack(), result)) {
                    this.arecipes.add(new CachedSludgeBoilerRecipe(result, drop.itemWeight));
                }
            }
        }
    }
    
    @Override
    public void loadUsageRecipes(FluidStack ingredient) {
        if (ingredient.getFluid().getName().equals("sludge")) {
            for (Item drop : drops) {
                if (drop instanceof WeightedRandomItemStack) {
                    this.arecipes.add(new CachedSludgeBoilerRecipe(((WeightedRandomItemStack) drop).getStack(), drop.itemWeight));
                }
            }
        }
    }
    
}