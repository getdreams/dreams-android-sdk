# Dreams Android SDK

## Installation

Register the github maven repository by adding it to your project build.gradle.

```groovy
allprojects {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/getdreams/dreams-android-sdk")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Add the library to your module dependencies.

```groovy
dependencies {
    implementation 'com.getdreams:android-sdk:0.1.0'
}
```

If you do not want to use the github repository you can download the package and put it in `<module>/libs`.
Make sure you include AARs from the libs directory:

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
}
```

### Github Authentication

For detailed docs see [this](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages#authenticating-to-github-packages).

You can save the username and token in your system gradle properties located in `$GRADLE_USER_HOME/gradle.properties`.

```properties
gpr.user=user
gpr.key=token
```

Or you can set the environmental variables:

```shell script
GITHUB_USERNAME="user"
GITHUB_TOKEN="token"
```

## Usage

Before using any other part of the SDK you must call `Dreams.setup()`. This can be done in `Application.onCreate()`.

```kotlin
class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Dreams.setup(clientId = "your client id", baseUrl = "url")
    }
}
```

To show Dreams simply add `DreamsView` to a layout, and then call `DreamsView.open()`.

```xml
<com.getdreams.views.DreamsView
    android:id="@+id/dreams"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
val dreamsView: DreamsView = findViewById<DreamsView>(R.id.dreams)
dreamsView.open(accessToken = "user token", location = Location.Home, locale = null)
```

## Documentation

You can generate documentation by running the relevant Dokka task.

```shell script
./gradlew sdk:dokkaHtml sdk:dokkaJavadoc
```

## Testing

### Unit tests

```shell script
./gradlew sdk:test
```

### Device tests

```shell script
./gradlew sdk:connectedCheck
```

## Development

Simply clone the repo and open the project in Android Studio.

### Publishing

To publish the library to your local maven, simply run:

```shell script
./gradlew sdk:publishToMavenLocal
```

This will publish aar, html-doc, javadoc, and sources to the local maven.

To publish to you need to add signing key details when running the publishing task(s).

```properties
signing.gnupg.keyName=<key>
signing.gnupg.passphrase=<pass>
```
