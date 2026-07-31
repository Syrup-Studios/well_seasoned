plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-neoforge"

stonecutter {
    parameters {
        val loader = node.metadata.project.substringAfterLast('-')
        constants.match(loader, "fabric", "neoforge")
    }
}
