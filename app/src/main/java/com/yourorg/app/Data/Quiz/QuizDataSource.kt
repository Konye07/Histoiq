package com.yourorg.app.Data.Quiz

import android.content.Context
import org.json.JSONArray

private fun Context.readAsset(path: String): String =
    assets.open(path).bufferedReader().use { it.readText() }

fun loadHistoricalEvents(context: Context): List<HistoricalEvent> {
    val array = JSONArray(context.readAsset("data/year_guess/historical_events.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        HistoricalEvent(
            id = o.getString("id"),
            type = o.getString("type"),
            event = o.getString("event"),
            correctYear = o.getInt("correctYear"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty"),
            imageAsset = o.optString("imageAsset", "").ifBlank { null }
        )
    }
}

fun loadBattleCasualties(context: Context): List<BattleCasualty> {
    val array = JSONArray(context.readAsset("data/casualties_multiplier/battle_casualties.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        BattleCasualty(
            id = o.getString("id"),
            type = o.getString("type"),
            battle = o.getString("battle"),
            correctCasualties = o.getLong("correctCasualties"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty"),
            imageAsset = o.optString("imageAsset", "").ifBlank { null }
        )
    }
}

fun loadFlagColors(context: Context): List<FlagColor> {
    val array = JSONArray(context.readAsset("data/flag_color_match/flag_colors.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        FlagColor(
            id = o.getString("id"),
            type = o.getString("type"),
            flagName = o.getString("flagName"),
            imageAsset = o.getString("imageAsset"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty")
        )
    }
}

fun loadTerritories(context: Context): List<TerritoryCountry> {
    val array = JSONArray(context.readAsset("data/territory_compare/territories.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        TerritoryCountry(
            id = o.getString("id"),
            type = o.getString("type"),
            country = o.getString("country"),
            era = o.getString("era"),
            areaKm2 = o.getLong("areaKm2"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty")
        )
    }
}

fun loadTrueFalseStatements(context: Context): List<TrueFalseStatement> {
    val array = JSONArray(context.readAsset("data/true_false/statements.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        TrueFalseStatement(
            id = o.getString("id"),
            type = o.getString("type"),
            statement = o.getString("statement"),
            correctAnswer = o.getBoolean("correctAnswer"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty")
        )
    }
}

fun loadBattleLocations(context: Context): List<BattleLocation> {
    val array = JSONArray(context.readAsset("data/blind_map_battle/battles.json"))
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        BattleLocation(
            id = o.getString("id"),
            type = o.getString("type"),
            battle = o.getString("battle"),
            correctLat = o.getDouble("correctLat"),
            correctLng = o.getDouble("correctLng"),
            source = o.getString("source"),
            difficulty = o.getInt("difficulty")
        )
    }
}
