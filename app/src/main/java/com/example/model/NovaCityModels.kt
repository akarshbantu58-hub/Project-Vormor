package com.example.model

import androidx.annotation.StringRes
import com.example.R

enum class EngineStatus {
    LOADING,
    READY,
    ERROR
}

enum class TimeOfDayPreset(
    val index: Int,
    @StringRes val labelRes: Int
) {
    MORNING(0, R.string.time_morning),
    NOON(1, R.string.time_noon),
    SUNSET(2, R.string.time_sunset),
    NIGHT(3, R.string.time_night);

    fun next(): TimeOfDayPreset = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromIndex(index: Int): TimeOfDayPreset =
            entries.firstOrNull { it.index == index } ?: SUNSET
    }
}

enum class CameraZoomMode(
    val index: Int,
    @StringRes val labelRes: Int
) {
    CLOSE(0, R.string.hud_cam_close),
    STANDARD(1, R.string.hud_cam_mid),
    WIDE(2, R.string.hud_cam_wide);

    fun next(): CameraZoomMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromIndex(index: Int): CameraZoomMode =
            entries.firstOrNull { it.index == index } ?: CLOSE
    }
}

data class UserSettings(
    val cameraSensitivity: Float = 1.0f,
    val audioEnabled: Boolean = true,
    val highResRender: Boolean = true
)

data class GameUiState(
    val engineStatus: EngineStatus = EngineStatus.LOADING,
    val engineErrorMessage: String? = null,
    val showSplash: Boolean = true,
    val showDirectorySheet: Boolean = false,
    val playerX: Float = 0f,
    val playerY: Float = 0.16f,
    val playerZ: Float = 10f,
    val cameraYaw: Float = 0f,
    val credits: Int = 1250,
    val stamina: Int = 100,
    val zoneLabel: String = "Horizon Ave — Downtown",
    val interactPrompt: String? = null,
    val isSeated: Boolean = false,
    val isDriving: Boolean = false,
    val vehicleSpeedKmh: Int = 0,
    val isSprinting: Boolean = false,
    val timePreset: TimeOfDayPreset = TimeOfDayPreset.SUNSET,
    val isRaining: Boolean = false,
    val camMode: CameraZoomMode = CameraZoomMode.CLOSE,
    val toastMessage: String? = null
)

enum class LandmarkCategory {
    SKYLINE,
    COMMERCE,
    COASTAL,
    LIVING,
    SECRET
}

data class DistrictLandmark(
    val id: String,
    val name: String,
    val subtitle: String,
    val highlights: String,
    val category: LandmarkCategory,
    val spawnX: Float,
    val spawnY: Float,
    val spawnZ: Float,
    val spawnYaw: Float
)

data class MinimapBlock(
    val x: Float,
    val z: Float,
    val width: Float,
    val depth: Float,
    val kind: MinimapBlockKind
)

enum class MinimapBlockKind {
    ROAD,
    OCEAN,
    PARK,
    BUILDING,
    BACKDROP
}

object NovaCityWorldData {
    val landmarks: List<DistrictLandmark> = listOf(
        DistrictLandmark(
            id = "horizon_skydeck",
            name = "Horizon Tower — Floor 14",
            subtitle = "52m Rooftop Helipad & Glass Skydeck",
            highlights = "Panoramic city & ocean sunset view, rooftop elevator return",
            category = LandmarkCategory.SKYLINE,
            spawnX = 41f,
            spawnY = 52.25f,
            spawnZ = -24f,
            spawnYaw = 3.14f
        ),
        DistrictLandmark(
            id = "horizon_lobby",
            name = "Horizon Tower Lobby",
            subtitle = "Downtown Financial Core",
            highlights = "Enterable lobby, interactive Office PC, Skydeck Elevator",
            category = LandmarkCategory.SKYLINE,
            spawnX = 41f,
            spawnY = 0.16f,
            spawnZ = -8.5f,
            spawnYaw = 0f
        ),
        DistrictLandmark(
            id = "nova_mart",
            name = "Nova Mart & ATM Plaza",
            subtitle = "Horizon Ave North",
            highlights = "Sliding glass doors, Groceries, ATM (+250 VC), Cola Vending",
            category = LandmarkCategory.COMMERCE,
            spawnX = 20f,
            spawnY = 0.16f,
            spawnZ = -8.5f,
            spawnYaw = 0f
        ),
        DistrictLandmark(
            id = "cafe_veyra",
            name = "Café Veyra",
            subtitle = "Neon Vice Corner",
            highlights = "Swing door, sittable café chairs, channel-switching TV, blinds",
            category = LandmarkCategory.COMMERCE,
            spawnX = -20f,
            spawnY = 0.16f,
            spawnZ = 8.5f,
            spawnYaw = 3.14f
        ),
        DistrictLandmark(
            id = "apex_mall",
            name = "Apex Mall",
            subtitle = "Central Retail Galleria",
            highlights = "Double sliding doors, neon plaza, interactive 4K OLED display",
            category = LandmarkCategory.COMMERCE,
            spawnX = 21f,
            spawnY = 0.16f,
            spawnZ = 8.5f,
            spawnYaw = 3.14f
        ),
        DistrictLandmark(
            id = "harbour_pier",
            name = "Veyra Harbour Pier",
            subtitle = "Gerstner Ocean & Yacht Marina",
            highlights = "Real-time wave physics, Fresnel sun glints, bobbing yachts",
            category = LandmarkCategory.COASTAL,
            spawnX = 12f,
            spawnY = 0.78f,
            spawnZ = 75f,
            spawnYaw = 3.14f
        ),
        DistrictLandmark(
            id = "bayfront_apt",
            name = "Bayfront Apt & Garage",
            subtitle = "Waterfront Residential Block",
            highlights = "Enterable apartment, Smart TV, animated rolling garage door",
            category = LandmarkCategory.LIVING,
            spawnX = -32f,
            spawnY = 0.16f,
            spawnZ = 52.5f,
            spawnYaw = 0f
        ),
        DistrictLandmark(
            id = "veyra_villa",
            name = "Veyra Coastal Villa",
            subtitle = "Oceanfront Residence",
            highlights = "Beachside sliding doors, villa lounger, coastal window view",
            category = LandmarkCategory.LIVING,
            spawnX = 30f,
            spawnY = 0.16f,
            spawnZ = 52.5f,
            spawnYaw = 0f
        ),
        DistrictLandmark(
            id = "veyra_clinic",
            name = "Veyra Medical Clinic",
            subtitle = "West Avenue",
            highlights = "Enterable clinic interior, waiting chairs, room light switch",
            category = LandmarkCategory.LIVING,
            spawnX = -41f,
            spawnY = 0.16f,
            spawnZ = 8.5f,
            spawnYaw = 3.14f
        ),
        DistrictLandmark(
            id = "central_park",
            name = "Central Park & Secret Hatch",
            subtitle = "Northwest Greenbelt",
            highlights = "Reflective lake, park bench, hidden Syndicate Stash (+500 VC)",
            category = LandmarkCategory.SECRET,
            spawnX = -38f,
            spawnY = 0.18f,
            spawnZ = -40f,
            spawnYaw = 0.7f
        )
    )

    val minimapBlocks: List<MinimapBlock> = listOf(
        // Roads (East-West)
        MinimapBlock(-110f, -66f, 220f, 12f, MinimapBlockKind.ROAD),
        MinimapBlock(-110f, -6f, 220f, 12f, MinimapBlockKind.ROAD),
        MinimapBlock(-110f, 54f, 220f, 12f, MinimapBlockKind.ROAD),
        // Roads (North-South)
        MinimapBlock(-66f, -100f, 12f, 172f, MinimapBlockKind.ROAD),
        MinimapBlock(-6f, -100f, 12f, 172f, MinimapBlockKind.ROAD),
        MinimapBlock(54f, -100f, 12f, 172f, MinimapBlockKind.ROAD),
        // Ocean
        MinimapBlock(-120f, 72f, 240f, 60f, MinimapBlockKind.OCEAN),
        // Central Park
        MinimapBlock(-51f, -51f, 42f, 42f, MinimapBlockKind.PARK),
        // Enterable buildings
        MinimapBlock(11f, -31f, 18f, 20f, MinimapBlockKind.BUILDING),   // Nova Mart
        MinimapBlock(33f, -49f, 16f, 38f, MinimapBlockKind.BUILDING),   // Horizon Tower
        MinimapBlock(-30f, 11f, 19f, 17f, MinimapBlockKind.BUILDING),   // Cafe Veyra
        MinimapBlock(-49f, 11f, 16f, 17f, MinimapBlockKind.BUILDING),   // Veyra Clinic
        MinimapBlock(-48f, 33f, 34f, 16f, MinimapBlockKind.BUILDING),   // Bayfront Apt
        MinimapBlock(11f, 11f, 20f, 20f, MinimapBlockKind.BUILDING),    // Apex Mall
        MinimapBlock(35f, 20f, 14f, 10f, MinimapBlockKind.BUILDING),    // Fuel Shop
        MinimapBlock(14f, 35f, 32f, 14f, MinimapBlockKind.BUILDING),    // Veyra Villa
        // Skyline backdrop
        MinimapBlock(-99f, -42f, 22f, 24f, MinimapBlockKind.BACKDROP),
        MinimapBlock(-99f, 13f, 22f, 24f, MinimapBlockKind.BACKDROP),
        MinimapBlock(76f, -49f, 24f, 28f, MinimapBlockKind.BACKDROP),
        MinimapBlock(77f, 12f, 22f, 26f, MinimapBlockKind.BACKDROP),
        MinimapBlock(-48f, -98f, 26f, 20f, MinimapBlockKind.BACKDROP),
        MinimapBlock(4f, -99f, 28f, 22f, MinimapBlockKind.BACKDROP),
        MinimapBlock(58f, -98f, 20f, 20f, MinimapBlockKind.BACKDROP)
    )
}
