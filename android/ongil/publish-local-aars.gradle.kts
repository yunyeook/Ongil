// publish-local-aars.gradle.kts
tasks.register("publishLocalAars") {
    doLast {
        val localMavenRepo = file("$rootDir/local-maven-repo")
        
        // T-Map SDK 발행
        publishAar(
            aarFile = file("common/libs/tmap-sdk-3.0.aar"),
            group = "com.skt.tmap",
            artifact = "tmap-sdk",
            version = "3.0",
            repoDir = localMavenRepo
        )
        
        // VSM T-Map SDK 발행
        publishAar(
            aarFile = file("common/libs/vsm-tmap-sdk-v2-android-1.7.45.aar"),
            group = "com.skt.tmap",
            artifact = "vsm-tmap-sdk",
            version = "1.7.45",
            repoDir = localMavenRepo
        )
    }
}

fun publishAar(aarFile: File, group: String, artifact: String, version: String, repoDir: File) {
    val groupPath = group.replace('.', '/')
    val targetDir = File(repoDir, "$groupPath/$artifact/$version")
    targetDir.mkdirs()
    
    // AAR 파일 복사
    aarFile.copyTo(File(targetDir, "$artifact-$version.aar"), overwrite = true)
    
    // POM 파일 생성
    val pomFile = File(targetDir, "$artifact-$version.pom")
    pomFile.writeText("""
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
            <modelVersion>4.0.0</modelVersion>
            <groupId>$group</groupId>
            <artifactId>$artifact</artifactId>
            <version>$version</version>
            <packaging>aar</packaging>
        </project>
    """.trimIndent())
}