# Dreams Android SDK

## Installation

Add the library to your gradle dependencies.

```gradle
dependencies {
    implementation 'com.getdreams:android-sdk:<version>'
}
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
