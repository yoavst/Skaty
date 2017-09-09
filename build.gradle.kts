buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath(kotlinModule(module = "gradle-plugin", version = "1.1.4-3"))
    }
}

apply {
    plugin("kotlin")
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlinModule(module ="stdlib", version = "1.1.4-3"))
    compile(kotlinModule(module ="reflect", version = "1.1.4-3"))

    compile("org.slf4j:slf4j-api:1.7.24")
    compile("io.github.microutils:kotlin-logging:1.4.3")
    testCompile("org.slf4j:slf4j-simple:1.7.24")

    compile("org.pcap4j:pcap4j-core:1.+")
    compile("org.pcap4j:pcap4j-packetfactory-static:1.+")
}