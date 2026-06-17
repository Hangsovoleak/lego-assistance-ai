package com.example.data.model

object PreloadedModels {
    val list = listOf(
        LegoModel(
            id = "mini_car",
            name = "Mini Racing Car",
            description = "A swift, low-riding speedster designed with advanced rear wings and specialized aerodynamic dynamics.",
            difficulty = "Easy",
            estimatedMinutes = 6,
            requiredBricks = listOf(
                RequiredBrick("2x4", "Red", "#E3000B", 2),
                RequiredBrick("2x2", "Blue", "#0055A5", 2),
                RequiredBrick("Wheel", "Black", "#1B1F22", 2)
            ),
            steps = listOf(
                BuildStep(1, "Place two Red 2x4 bricks inline along the ground. This forms the solid chassis of your racer.", "2x4_red", 2),
                BuildStep(2, "Attach the two Black Wheel axles securely underneath each end of the red chassis.", "wheel_black", 2),
                BuildStep(3, "Secure two Blue 2x2 bricks side by side on top of the chassis to make the matching seats and cockpit!", "2x2_blue", 2)
            ),
            educationalFocus = "Friction & Mechanical Motion",
            category = "Vehicles"
        ),
        LegoModel(
            id = "cute_bird",
            name = "Bluebird of Happiness",
            description = "A colorful songbird perched on a golden branch, showcasing outstretched wings ready for flight.",
            difficulty = "Easy",
            estimatedMinutes = 5,
            requiredBricks = listOf(
                RequiredBrick("2x2", "Blue", "#0055A5", 2),
                RequiredBrick("1x4", "Yellow", "#F2CD37", 3)
            ),
            steps = listOf(
                BuildStep(1, "Lay a Yellow 1x4 brick on a flat surface to form the warm tree branch.", "1x4_yellow", 1),
                BuildStep(2, "Stack two Blue 2x2 bricks vertically on the center of the branch for the bird's round body.", "2x2_blue", 2),
                BuildStep(3, "Mount two Yellow 1x4 bricks horizontally on either side of the body as wings catching the wind!", "1x4_yellow", 2)
            ),
            educationalFocus = "Gravity & Mechanical Balance",
            category = "Animals"
        ),
        LegoModel(
            id = "scout_robot",
            name = "Helper Scout Robot",
            description = "A friendly robotic scout equipped with fully rotating mechanical arms and sensory radar.",
            difficulty = "Medium",
            estimatedMinutes = 12,
            requiredBricks = listOf(
                RequiredBrick("2x4", "Red", "#E3000B", 3),
                RequiredBrick("2x2", "Blue", "#0055A5", 3),
                RequiredBrick("1x4", "Yellow", "#F2CD37", 2)
            ),
            steps = listOf(
                BuildStep(1, "Stand two Red 2x4 bricks side by side. These form the robot's durable armored legs.", "2x4_red", 2),
                BuildStep(2, "Mount three Blue 2x2 bricks stacked in the center as the computing core body.", "2x2_blue", 3),
                BuildStep(3, "Secure two Yellow 1x4 bricks on either side of the chest as flexible radar arms.", "1x4_yellow", 2),
                BuildStep(4, "Top off the build with a final Red 2x4 brick as the cognitive scanner head directly on the shoulders!", "2x4_red", 1)
            ),
            educationalFocus = "Symmetry & Engineering Design",
            category = "Sci-Fi"
        ),
        LegoModel(
            id = "cozy_cabin",
            name = "Cozy Swiss Chalet",
            description = "A quaint mountainside home featuring structured log-styled siding and a sharp high-slope roof.",
            difficulty = "Hard",
            estimatedMinutes = 20,
            requiredBricks = listOf(
                RequiredBrick("2x4", "Red", "#E3000B", 4),
                RequiredBrick("2x2", "Blue", "#0055A5", 4),
                RequiredBrick("1x4", "Yellow", "#F2CD37", 4),
                RequiredBrick("2x2", "White", "#F2F3F2", 2)
            ),
            steps = listOf(
                BuildStep(1, "Arrange four Red 2x4 bricks in a hollow square structure to outline the ground foundation.", "2x4_red", 4),
                BuildStep(2, "Stack four Yellow 1x4 bricks in an interlocking log pattern to construct the sturdy wooden walls.", "1x4_yellow", 4),
                BuildStep(3, "Position the White 2x2 bricks in front to form glass bay doors and windows.", "2x2_white", 2),
                BuildStep(4, "Mount four Blue 2x2 bricks overhead, slightly overlapping, to complete the cabin roof and chimney!", "2x2_blue", 4)
            ),
            educationalFocus = "Architecture & Spatial Geometry",
            category = "Buildings"
        )
    )
}
