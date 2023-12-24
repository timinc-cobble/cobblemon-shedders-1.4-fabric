package us.timinc.mc.cobblemon.shedders

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.util.removeAmountIf
import net.fabricmc.api.ModInitializer
import net.minecraft.item.Items
import us.timinc.mc.cobblemon.shedders.config.Config

object Shedders : ModInitializer {
    const val MOD_ID = "cobblemon_shedders"
    private var config: Config = Config.Builder.load()

    override fun onInitialize() {
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe(Priority.LOWEST) { event ->
            val pokemon = event.pokemon
            val pokemonSpeciesIdentifier = pokemon.species.resourceIdentifier.path
            println(pokemonSpeciesIdentifier)
            if (config.shedders.contains(pokemonSpeciesIdentifier)) {
                val target = config.shedders[pokemonSpeciesIdentifier]!!
                if (PokemonSpecies.getByName(target) == null) {
                    println("Attempted to create a $target as a result of $pokemonSpeciesIdentifier evolving, but $target isn't a valid Pokemon")
                    return@subscribe
                }
                val player = pokemon.getOwnerPlayer() ?: return@subscribe
                if (player.isCreative || player.inventory.containsAny { it.item is PokeBallItem }) {
                    var pokeball = Items.AIR
                    player.inventory.combinedInventory.forEach {
                        it.forEach { itemStack ->
                            if (itemStack.item is PokeBallItem && pokeball == Items.AIR) {
                                pokeball = itemStack.item as PokeBallItem
                            }
                        }
                    }
                    if (!player.isCreative) {
                        player.inventory.removeAmountIf(1) { it.item is PokeBallItem }
                    }
                    if (pokeball == Items.AIR) {
                        pokeball = CobblemonItems.POKE_BALL
                    }
                    val properties = event.evolution.result.copy()
                    properties.species = target
                    val product = pokemon.clone()
                    product.removeHeldItem()
                    properties.apply(product)
                    product.caughtBall = (pokeball as PokeBallItem).pokeBall
                    pokemon.storeCoordinates.get()?.store?.add(product)
                }
            }
        }
    }
}