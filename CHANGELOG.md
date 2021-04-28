# Changelog

All notable changes to this library will be documented here.

### [0.5.0](https://github.com/getdreams/dreams-android-sdk/compare/0.4.2...0.5.0) (2021-04-28)

### Features
* Share functionality, that can be triggered using following JS code
* `var payload = {"text":"Text to share", "url": "url"}; if (window.JSBridge) { window.JSBridge.share(payload); }`

### [0.4.2](https://github.com/getdreams/dreams-android-sdk/compare/0.4.1...0.4.2) (2021-03-16)


### Bug Fixes

* always have the dreams web view intercept requests ([6b70d98](https://github.com/getdreams/dreams-android-sdk/commit/6b70d98fdf165166d94a2e8cc9bb01bad729c24a))

### [0.4.1](https://github.com/getdreams/dreams-android-sdk/compare/0.4.0...0.4.1) (2021-02-25)


### Features

* default dreams view to focusable in touch mode ([1e186d4](https://github.com/getdreams/dreams-android-sdk/commit/1e186d42e3cd95ed84a962b1c73916e20ed1ac07))

## [0.4.0](https://github.com/getdreams/dreams-android-sdk/compare/0.3.0...0.4.0) (2021-02-03)


### ⚠ BREAKING CHANGES

* DreamsView.launch() now requires a locale to be sent.

### Refactors

* require a locale when launching dreams ([b215dea](https://github.com/getdreams/dreams-android-sdk/commit/b215deaae14e34967b7e7284c14bf2fb2c3c434d))

## [0.3.0](https://github.com/getdreams/dreams-android-sdk/compare/0.2.2...0.3.0) (2021-01-27)


### ⚠ BREAKING CHANGES

* `DreamsView.launch` no longer takes a location.

### Features

* allow callers of launch to react to errors ([d42c81f](https://github.com/getdreams/dreams-android-sdk/commit/d42c81f7ebefcbe725b48e0a41bd4afcb44b7c62))


### Refactors

* removed unused parameter from launch ([88c65eb](https://github.com/getdreams/dreams-android-sdk/commit/88c65eb2bc45f7badb06cf16a9976c3ab0b510a4))

## [0.2.2](https://github.com/getdreams/dreams-android-sdk/compare/0.2.1...0.2.2) (2021-01-15)

### ⚠ Breaking Changes

* change how the singleton instance is configured. Instead of using multiple params, change to use a single object containing all the data needed for configuring the dreams instance.
* rename `RequestInterface.open` to `RequestInterface.launch`. Instead of taking a token directly, you now need to wrap it in a `Credentials` object.
* replace `IdTokenExpired` with `CredentialsExpired` event.

## [0.2.1](https://github.com/getdreams/dreams-android-sdk/compare/0.2.0...0.2.1) (2021-01-13)

### Bug Fixes

* update init post url
* change names in init payload
* set cookie from init call
