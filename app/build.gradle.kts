plugins {
    id("gnucash.kotlin-common-conventions")
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.10.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.http4k:http4k-client-jetty")
    testImplementation("com.ubertob.pesticide:pesticide-core:1.6.6")
    testImplementation("org.jsoup:jsoup:1.12.1")
}
