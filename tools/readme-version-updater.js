const regex = /^(\s+implementation\s+)'com.getdreams:android-sdk:(\d+\.\d+\.\d+)'$/m

module.exports.readVersion = function (contents) {
  return contents.match(regex)[2]
}

module.exports.writeVersion = function (contents, version) {
  return contents.replace(regex, `$1'com.getdreams:android-sdk:${version}'`)
}
