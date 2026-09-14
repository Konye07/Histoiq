package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * Question banks now live in Supabase (year_guess_items, casualties_multiplier_items, etc. — see
 * CLAUDE.md), fetched by the exact id set [get_or_create_daily_quiz] assigned for the day. Order is
 * not preserved by the `isIn` filter, so callers that need the RPC's order (e.g. Territory Compare's
 * pre-shuffled queue) should re-sort client-side by the original id list.
 */
suspend fun loadHistoricalEvents(ids: List<String>): List<HistoricalEvent> =
    SupabaseClient.client.postgrest.from("year_guess_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()

suspend fun loadBattleCasualties(ids: List<String>): List<BattleCasualty> =
    SupabaseClient.client.postgrest.from("casualties_multiplier_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()

suspend fun loadFlagColors(ids: List<String>): List<FlagColor> =
    SupabaseClient.client.postgrest.from("flag_color_match_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()

suspend fun loadTerritories(ids: List<String>): List<TerritoryCountry> =
    SupabaseClient.client.postgrest.from("territory_compare_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()

suspend fun loadTrueFalseStatements(ids: List<String>): List<TrueFalseStatement> =
    SupabaseClient.client.postgrest.from("true_false_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()

suspend fun loadBattleLocations(ids: List<String>): List<BattleLocation> =
    SupabaseClient.client.postgrest.from("blind_map_battle_items")
        .select { filter { isIn("id", ids) } }
        .decodeList()
