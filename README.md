# Dreams Android SDK

![Dreams](docs/assets/dreams.jpg)

## Requirements

* Minimum Android SDK version: 21
* Java source and target compatibility version: 1.8

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
    implementation 'com.getdreams:android-sdk:0.3.0'
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

Before using any other part of the SDK you must call `Dreams.configure()`. This can be done in `Application.onCreate()`.

```kotlin
class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Dreams.configure(Dreams.Configuration(clientId = "clientId", baseUrl = "https://getdreams.io"))
    }
}
```

To show Dreams simply add `DreamsView` to a layout, and then call `DreamsView.launch()`.
To handle possible errors during launch you can send a `OnLaunchCompletion` to `DreamsView.launch()` that will be invoked with the result of the launch.

```xml
<com.getdreams.views.DreamsView
    android:id="@+id/dreams"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
val dreamsView: DreamsView = findViewById<DreamsView>(R.id.dreams)
dreamsView.launch(Dreams.Credentials(idToken = "user token")) { result ->
    when (result) {
        is Result.Success -> {
            // Dreams was launched successfully
        }
        is Result.Failure -> {
            when (val err = result.error) {
                is LaunchError.InvalidCredentials -> {
                    // The provided credentials were invalid
                }
                is LaunchError.HttpError -> {
                    // The server returned a HTTP error code
                }
                is LaunchError.UnexpectedError -> {
                    // Something went wrong when trying to open a connection to Dreams
                }
            }
        }
    }
}
```

### Events

In order to listen for events from Dreams you need to register a listener on the DreamsView.

```kotlin
dreamsView.registerEventListener { event ->
    when (event) {
        is Event.Telemetry -> { /* Use telemetry event */ }
    }
}
```

#### Token renewal

When a token requires renewal a `CredentialsExpired` event will be sent, to set a new token you need to call
 `DreamsView.updateCredentials` with the request id from the event and the new token.

 ```kotlin
dreamsView.registerEventListener { event ->
    when (event) {
        is Event.CredentialsExpired -> {
            val newToken = getValidToken()
            dreamsView.updateCredentials(requestId = event.requestId, credentials = Credentials(idToken = newToken))
        }
    }
}
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

Simply clone the repo and open the project in Android Studio 4.0 or later.

### Preparing a Release

Simplest way is to use [standard-version](https://github.com/conventional-changelog/standard-version#cli-usage), with it you just need to run:

```shell script
npx standard-version
```

Or manually update the versions in [sdk/build.gradle](./sdk/build.gradle) and [README.md](./README.md). Then make a release tag.

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

To publish a new package to GitHub you can run:

```shell script
./gradlew sdk:publishReleasePublicationToGitHubPackagesRepository
```
