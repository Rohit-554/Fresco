package io.jadu.fresco

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform