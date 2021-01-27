# Changelog

All notable changes to this library will be documented here.

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
