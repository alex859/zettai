plugins {
    id("gnucash.kotlin-common-conventions")
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.10.0.0"))
    implementation("org.http4k:http4k-core")
}
