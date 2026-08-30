package com.example.data.model

data class VideoSourcePreset(
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String,
    val category: String,
    val durationText: String,
    val defaultTitle: String,
    val defaultDescription: String
)

object SampleVideoCatalog {
    val presets = listOf(
        VideoSourcePreset(
            title = "Lo-Fi Beats & Ambient Space",
            description = "Chill lo-fi study & focus broadcast stream with ambient background animation.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            category = "Music",
            durationText = "Looping 24/7",
            defaultTitle = "🔴 24/7 Lo-Fi Chill Beats | Relax / Study / Code Stream",
            defaultDescription = "Welcome to our continuous high-quality Lo-Fi live stream. Powered by YT Live Manager RTMPS Server Engine."
        ),
        VideoSourcePreset(
            title = "Nature Horizons: 4K Drone Flight",
            description = "Cinematic aerial views of fjords, alpine lakes, and pine forests.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600",
            category = "Travel & Events",
            durationText = "2 Hours",
            defaultTitle = "4K Relaxing Nature Drone Journey | Alpine Wilderness LIVE",
            defaultDescription = "Live continuous broadcast featuring majestic scenery, peaceful soundscapes, and ultra-high-definition mountain views."
        ),
        VideoSourcePreset(
            title = "Cyberpunk Neo City [60 FPS Ultra]",
            description = "Fast-paced neon futuristic gaming visuals and synthwave soundtrack.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600",
            category = "Gaming",
            durationText = "3 Hours",
            defaultTitle = "Cyberpunk Neo City 2099 - 4K 60FPS Live Showcase",
            defaultDescription = "High-energy cyber aesthetic broadcast streamed with low-latency NVENC hardware acceleration."
        ),
        VideoSourcePreset(
            title = "Deep Space & Aurora Borealis",
            description = "Stargazing time-lapses, northern lights, and celestial nebula motion graphics.",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1531306728370-e2ebd9d7bb99?w=600",
            category = "Science & Technology",
            durationText = "4 Hours",
            defaultTitle = "Deep Space Nebula & Aurora Borealis 24/7 Live Stream",
            defaultDescription = "Experience the cosmic beauty of auroras and constellations in high-definition broadcast."
        )
    )

    val categories = listOf(
        "Gaming",
        "Music",
        "Entertainment",
        "Science & Technology",
        "Travel & Events",
        "Education",
        "How-to & Style",
        "News & Politics",
        "Sports"
    )

    val privacyOptions = listOf("Public", "Unlisted", "Private")

    val resolutionPresets = listOf(
        "1080p60 (6,000 kbps)",
        "1080p30 (4,500 kbps)",
        "720p60 (3,500 kbps)",
        "1440p60 (9,500 kbps)"
    )
}
