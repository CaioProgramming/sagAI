package com.ilustris.sagai.core.network.body

data class FreepikRequest(
    val prompt: String,
    val negative_prompt: String,
    val style: ImageStyling,
    val seed: Int = 42,
    val num_images: Int = 1,
    val filter_nsfw: Boolean = true,
)

data class ImageStyling(
    val style: String,
    val effects: Effects,
)

data class Effects(
    val color: String,
    val lightning: String,
    val framing: String,
) {
    constructor(
        color: ColorPreset,
        lightning: LightningPreset,
        framing: FramingPreset,
    ) : this(
        color = color.key,
        lightning = lightning.key,
        framing = framing.key,
    )
}

enum class StylePreset(
    val key: String,
) {
    PHOTO("photo"),
    DIGITAL_ART("digital-art"),
    THREE_D("3d"),
    PAINTING("painting"),
    LOW_POLY("low-poly"),
    PIXEL_ART("pixel-art"),
    ANIME("anime"),
    CYBERPUNK("cyberpunk"),
    COMIC("comic"),
    VINTAGE("vintage"),
    CARTOON("cartoon"),
    VECTOR("vector"),
    STUDIO_SHOT("studio-shot"),
    DARK("dark"),
    SKETCH("sketch"),
    MOCKUP("mockup"),
    PONE_2000("2000s-pone"),
    VIBE_70("70s-vibe"),
    WATERCOLOR("watercolor"),
    ARTNOVEAU("art-nouveau"),
    ORIGAMI("origami"),
    SURREAL("surreal"),
    FANTASY("fantasy"),
    TRADITIONAL_JAPAN("traditional-japan"),
}

enum class ColorPreset(
    val key: String,
) {
    BW("b&w"),
    PASTEL("pastel"),
    SEPIA("sepia"),
    DRAMATIC("dramatic"),
    VIBRANT("vibrant"),
    ORANGE_TEAL("orange&teal"),
    FILM_FILTER("film-filter"),
    SPLIT("split"),
    ELECTRIC("electric"),
    PASTEL_PINK("pastel-pink"),
    GOLD_GLOW("gold-glow"),
    AUTUMN("autumn"),
    MUTED_GREEN("muted-green"),
    DEEP_TEAL("deep-teal"),
    DUOTONE("duotone"),
    TERRACOTTA_TEAL("terracotta&teal"),
    RED_BLUE("red&blue"),
    COLD_NEON("cold-neon"),
    BURGUNDY_BLUE("burgundy&blue"),
}

enum class LightningPreset(
    val key: String,
) {
    STUDIO("studio"),
    WARM("warm"),
    CINEMATIC("cinematic"),
    VOLUMETRIC("volumetric"),
    GOLDEN_HOUR("golden-hour"),
    LONG_EXPOSURE("long-exposure"),
    COLD("cold"),
    IRIDESCENT("iridescent"),
    DRAMATIC("dramatic"),
    HARDLIGHT("hardlight"),
    REDSCALE("redscale"),
    INDOOR_LIGHT("indoor-light"),
}

enum class FramingPreset(
    val key: String,
) {
    PORTRAIT("portrait"),
    MACRO("macro"),
    PANORAMIC("panoramic"),
    AERIAL_VIEW("aerial-view"),
    CLOSE_UP("close-up"),
    CINEMATIC("cinematic"),
    HIGH_ANGLE("high-angle"),
    LOW_ANGLE("low-angle"),
    SYMMETRY("symmetry"),
    FISH_EYE("fish-eye"),
    FIRST_PERSON("first-person"),
}
