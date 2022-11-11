package one.devos.yiski.tiktok

enum class Voices(val code: String, val desc: String) {
    // Disney Voices
    GHOST_FACE("en_us_ghostface", "Ghost Face (no profanity)"),
    CHEWBACCA("en_us_chewbacca", "Chewbacca (non-intelligible)"),
    C3PO("en_us_c3po", "C3PO (no profanity)"),
    STITCH("en_us_stitch", "Stitch (no profanity)"),
    // Stormtrooper
    DEFAULT("en_us_stormtrooper", "Stormtrooper (no profanity)"),
    ROCKET("en_us_rocket", "Rocket (no profanity)"),

    // English Voices
    AU_FEMALE("en_au_001", "English AU - Female"),
    AU_MALE("en_au_002", "English AU - Male"),
    UK_MALE1("en_uk_001", "English UK - Male 1"),
    UK_MALE2("en_uk_003", "English UK - Male 2"),
    US_FEMALE1("en_us_001", "English US - Female (Int. 1)"),
    US_FEMALE2("en_us_002", "English US - Female (Int. 2)"),
    US_MALE1("en_us_006", "English US - Male 1"),
    US_MALE2("en_us_007", "English US - Male 2"),
    US_MALE3("en_us_009", "English US - Male 3"),
    US_MALE4("en_us_010", "English US - Male 4"),

    // Europe Voices
    //FR_MALE1("fr_001", "French - Male 1"),
    //FR_MALE2("fr_002", "French - Male 2"),
    //DE_MALE1("de_001", "German - Female"),
    //DE_MALE2("de_002", "German - Male"),
    //ES_MALE("es_002", "Spanish - Male"),

    // America Voices
    //ES_MX_MALE("es_mx_002", "Spanish MX - Male"),
    //BR_FEMALE1("br_001", "Portuguese BR - Female 1"),
    //BR_FEMALE2("br_003", "Portuguese BR - Female 2"),
    //BR_FEMALE3("br_004", "Portuguese BR - Female 3"),
    //BR_MALE("br_005", "Portuguese BR - Male"),

    // Asia Voices
    //ID_FEMALE("id_001", "Indonesian - Female"),
    //JP_FEMALE1("jp_001", "Japanese - Female 1"),
    //JP_FEMALE2("jp_003", "Japanese - Female 2"),
    //JP_FEMALE3("jp_005", "Japanese - Female 3"),
    //JP_MALE("jp_006", "Japanese - Male"),
    //KR_MALE1("kr_002", "Korean - Male 1"),
    //KR_FEMALE("kr_003", "Korean - Female"),
    //KR_MALE2("kr_004", "Korean - Male 1"),

    // Singing Voices
    //ALTO("en_female_f08_salut_damour", "Alto"),
    //TENOR("en_male_m03_lobby", "Tenor"),
    //WARM_BREEZE("en_female_f08_warmy_breeze", "Warmy Breeze"),
    //SUNSHINE_SOON("en_male_m03_sunshine_soon", "Sunshine Soon"),

    // Other
    NARRATOR("en_male_narration", "Narrator"),
    WACKY("en_male_funny", "Wacky"),
    PEACEFUL("en_female_emotional", "Peaceful")
}